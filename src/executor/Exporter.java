package executor;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import pojos.AsmNode;
import utils.Arithmetic;
import utils.FileUtils;
import utils.Logs;
import utils.MyStr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Exporter {

    public static ArrayList<String> savedGraph = new ArrayList<>();
    public static ArrayList<String> savedAsm = new ArrayList<>();
    public static ArrayList<String> savedOriginAsm = new ArrayList<>();

    public static void add(String str) {
        savedGraph.add(str);
    }

    public static void addAsm(String str) {
        savedAsm.add(str);
    }

    public static void addOriginAsm(String str) {
        savedOriginAsm.add(str);
    }

    public static void exportDot(String outFile) {
        HashMap<String, Boolean> relationships = new HashMap<>();
        for (String s : savedGraph) {
            String[] arr = s.split("\\,");
            relationships.put(arr[0] + "->" + arr[1], true);
        }
        Map<String, Boolean> result = relationships.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        MyStr graph = new MyStr("digraph G {\n");
        for (String key : result.keySet()) {
            String[] arr = key.split("->");
            graph.append("\"" + arr[0] + "\" -> \"" + arr[1] + "\"\n");
        }
        graph.append("}");
        FileUtils.write(outFile, graph.value());
    }

    public static void exportAsm(String outFile) {
        Logs.infoLn("-> Exporting to .asm file ...");
        MyStr result = new MyStr();
        for (String s : savedAsm) result.append(s + "\n");
        FileUtils.write(outFile, result.value());
    }

    public static void exportOriginAsm(String outFile) {
        Logs.infoLn("-> Exporting to .capstone-asm file ...");
        MyStr result = new MyStr();
        for (String s : savedOriginAsm) result.append(s + "\n");
        FileUtils.write(outFile, result.value());
    }

    public static void exportNeo4j(HashMap<String, Integer> labelToIndex, ArrayList<AsmNode> asmNodes) {
        Logs.infoLn("-> Exporting graph to Neo4j ...");
        HashMap<String, Boolean> nodesMap = new HashMap<>();
        HashMap<String, Boolean> relationships = new HashMap<>();

        for (String s : savedGraph) {
            String[] arr = s.split("\\,");
            nodesMap.put(arr[0], true);
            nodesMap.put(arr[1], true);
            if (arr.length == 3) {
                relationships.put(arr[0] + "->" + arr[1], true);
            } else {
                relationships.put(arr[0] + "-" + arr[1], true);
            }
        }
        Driver driver = GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic(Configs.neo4jUser, Configs.neo4jPassword));
        Session session = driver.session();
        session.run("MATCH (a)-[r]->(b) DELETE r,a,b");
        for (String k : nodesMap.keySet()) {
            session.run("CREATE (n:Node {label: '" + k + "', opcode:'" +
                    asmNodes.get(labelToIndex.get(k)).getOpcode() + "'})");
        }

        for (String pair : relationships.keySet()) {
            int type = pair.contains("->") ? 1 : 0;
            String deli = (type == 1) ? "->" : "-";
            String from = pair.split(deli)[0];
            String to = pair.split(deli)[1];
            session.run("MATCH (a:Node),(b:Node) WHERE a.label = '" + from + "' AND b.label = '" +
                    to + "' CREATE (a)-[" + (type == 1 ? "r:JUMP_TO" : "r:G") + "]->(b)");
        }
        session.close();
        driver.close();
    }
}
