package utils;

import interfaces.OnResult;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        InputStream in = getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String resource;
        while ((resource = br.readLine()) != null) {
            filenames.add(resource);
        }
        return filenames;
    }

    public static InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);
        return in == null ? FileUtils.class.getResourceAsStream(resource) : in;
    }

    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }


    public static void listFiles(String path, OnResult onResult) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    onResult.onSuccess(file.getName());
                }
            }
        } else {
            Logs.infoLn("List files is null");
        }
    }

    public static List<String> listFiles(String path) {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void append(String path, Object data) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }
            Files.write(Paths.get(path), data.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            Logs.infoLn(e.getMessage());
        }
    }

    public static File getFileResource(String path) {
        try {
            return new File(ClassLoader.getSystemResource(path).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void write(String path, String message) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            bw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readResource(String path, OnResult onResult) {
        InputStream fileInputStream = ClassLoader.getSystemResourceAsStream(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) onResult.onSuccess(line);
            br.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readToString(String path) {
        StringBuilder str = new StringBuilder();
        File fileDir = new File(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) str.append(line).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    public static String readResourceToString(String path) {
        StringBuilder str = new StringBuilder();
        InputStream fileInputStream = ClassLoader.getSystemResourceAsStream(path);
        assert fileInputStream != null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) str.append(line).append("\n");
            br.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    public static void read(String path, OnResult dataTask) {
        File fileDir = new File(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                dataTask.onSuccess(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendFile(String path, String data) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isExist(String path) {
        return new File(path).exists();
    }

    public static boolean isDir(String path) {
        return new File(path).isDirectory();
    }

    public static boolean delete(String path) {
        return new File(path).delete();
    }

    public static byte[] readBytesFromFile(String path) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;
        try {
            File file = new File(path);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
}