package external.testing;

import com.google.gson.Gson;
import emulator.semantics.Environment;
import emulator.semantics.Memory;
import executor.Configs;
import executor.DBDriver;
import external.handler.APIStub;
import pojos.BitVec;
import utils.Arithmetic;

import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class StubTest {
    public class TestCases {
        List<TestCase> testcases;
    }

    public class TestCase {
        public int id;
        public Map<Integer, String> registers_input;
        public Map<String, String> memory_input;
    }

    public static List<Environment> readJson(String jsonFile) {
        List<Environment> tcs = new ArrayList<>();
        try {
            // create Gson instance
            Gson gson = new Gson();
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(jsonFile));

            // convert JSON file to map
            TestCases map = gson.fromJson(reader, TestCases.class);
            String tcid;
            // print map entries
            for (TestCase tc : map.testcases) {
                Memory.loadMemory();
                Environment env = new Environment();

                env.register.set('0', Arithmetic.fromHexStr(tc.registers_input.get(0)));
                env.register.set('1', Arithmetic.fromHexStr(tc.registers_input.get(1)));
                env.register.set('2', Arithmetic.fromHexStr(tc.registers_input.get(2)));
                env.register.set('3', Arithmetic.fromHexStr(tc.registers_input.get(3)));
                env.register.set('4', Arithmetic.fromHexStr(tc.registers_input.get(4)));
                env.register.set('5', Arithmetic.fromHexStr(tc.registers_input.get(5)));
                env.register.set('6', Arithmetic.fromHexStr(tc.registers_input.get(6)));
                env.register.set('7', Arithmetic.fromHexStr(tc.registers_input.get(7)));
                env.register.set('8', Arithmetic.fromHexStr(tc.registers_input.get(8)));
                env.register.set('9', Arithmetic.fromHexStr(tc.registers_input.get(9)));
                env.register.set('x', Arithmetic.fromHexStr(tc.registers_input.get(10)));
                env.register.set('e', Arithmetic.fromHexStr(tc.registers_input.get(11)));
                env.register.set('t', Arithmetic.fromHexStr(tc.registers_input.get(12)));
                env.register.set('s', Arithmetic.fromHexStr(tc.registers_input.get(13)));
                env.register.set('l', Arithmetic.fromHexStr(tc.registers_input.get(14)));
                env.register.set('p', Arithmetic.fromHexStr(tc.registers_input.get(15)));

                for (String add : tc.memory_input.keySet()) {
                    env.memory.put(add, Arithmetic.fromHexStr(tc.memory_input.get(add)));
                }
                //System.out.println(env);
                tcs.add(env);
            }

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tcs;
    }

    public static void run(String function, Environment testcase) {
        // query function name
        List<String> list = Arrays.stream(APIStub.class.getMethods()).map(s -> s.getName()).collect(Collectors.toList());
//        for (String methodName: list) {
//            System.out.println(methodName);
//            try {
//                Method stubMethod = APIStub.class.getMethod(methodName, Environment.class);
//                stubMethod.setAccessible(true);
//                Environment tc = new Environment();
//                System.out.println(tc);
//                stubMethod.invoke(null, tc);
//
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//            break;
//        }
        try {
            Method stubMethod = APIStub.class.getMethod(function, Environment.class);
            stubMethod.setAccessible(true);
            Environment tcEnv = testcase;
            //System.out.println(tc);
            stubMethod.invoke(null, tcEnv);
            System.out.println(tcEnv.register.get('0'));

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        List<Environment> envs = StubTest.readJson("./resources/connect_test.json");
        for (Environment testcase : envs) {
            StubTest.run("connect", testcase);
            break;
        }
    }
}
