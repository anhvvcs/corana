package utils;

import executor.Configs;
import external.jni.CStruct;

import java.lang.reflect.*;

import java.util.Random;

public class TypeRandom<T> {
    static Random random;
    private T[] elements;

    public static Object rand(String typeName) {
        random = new Random();
       // typeName = typeName.substring(typeName.lastIndexOf(".") + 1,typeName.length());
        try {
            if (typeName.equals("int")) {
                return randInt();
            } else if (typeName.equals("byte")) {
                return (byte) randInt();
            } else if (typeName.equals("char")) {
                return (char) randInt();
            } else if (typeName.equals("short")) {
                return (short) randInt();
            } else if (typeName.substring(typeName.lastIndexOf(".") + 1,typeName.length()).equals("String")) {
                return (String) randString();
            } else if (typeName.contains("[")) {
                if (typeName.contains("String")) {
                    return (String[]) generateRandomStrArr(10);
                } else if (typeName.contains("int")) {
                    return generateIntArray(3);
                }
                else return generateRandomArray(getPrimitiveType(typeName), 10);
            } else if (typeName.contains("CStruct")) {
                Class cstruct = getStruct(typeName);
                //return (CStruct) randCStruct(cstruct);
                return createAndFill(cstruct);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return 0;
    }

//    public static Object rand(Type type) {
//        random = new Random();
//        try {
//            if (type.toString().equals("int")) {
//                return randInt();
//            } else if (type.toString().equals("char")) {
//                return (char) randInt();
//            } else if (type.toString().equals("String")) {
//                return (String) randString();
//            } else if (type.toString().contains("[")) {
//                return (Object[]) generateRandomArray(type, 10);
//            } else if (type.getTypeName().contains("CStruct")) {
//                Class cstruct = getStruct(type.getTypeName());
////                System.out.println(cstruct);
//                return cstruct.cast(randCStruct(cstruct)) ;
//            }
//        } catch (Exception e) {
//        }
//        return 0;
//    }

    public static  <T> T createAndFill(Class<T> clazz) throws Exception {
        T instance = clazz.newInstance();
//        for(Field field: clazz.getDeclaredFields()) {
//            field.setAccessible(true);
//            Object value = rand(field.getType().toString());
//            field.set(instance, value);
//            System.out.println(field);
//        }
        return instance;
    }

    private static String getPrimitiveType(String arrName) {

        //String typeName = arrName.substring(arrName.lastIndexOf(".") + 1,arrName.length());
        String pri = arrName.replace("[","").replace("]","");
        return pri;
    }

    private static int randInt() {
        return random.nextInt((int) Math.pow(2, Configs.architecture >> 2));
    }
    private static String randString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }
    private static Object randCStruct(Class struct) throws IllegalAccessException, InstantiationException {
        Field[] fields = struct.getFields();
        Object[] params = new Object[fields.length];
        //Type[] paramTypes = struct.getGenericParameterTypes();
        Object param;
        for (int i = 0; i < fields.length; i++) {
            params[i] = TypeRandom.rand(fields[i].getGenericType().getTypeName());
            System.out.println(params[i]);
        }
        try {

            param = Class.forName(struct.getName()).getConstructor(String.class).newInstance(params);
            return param;
        } catch (Exception e) {
        }
        return struct.newInstance();
    }

    private static Class getStruct(String name) {
        Class[] classes = CStruct.class.getDeclaredClasses();
        for (Class method : classes) {
            if (method.getName().equals(name)) return method;
        }
        return classes[0];
    }
    private static <T> T[] generateRandomArray(Class<T> clazz, int n) throws ClassNotFoundException {
        T[] list = (T[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) {
            list[i] = (T) rand(clazz.toString());
        }
        return list;
    }
    private static int[] generateIntArray(int n) {
        int[] list = (int[])Array.newInstance(int.class, n);
        for (int i = 0; i < n; i++) {
            list[i] = randInt();
        }
        return list;
    }
    private static Object[] generateRandomArray(String type, int n){
        Object[] list = new Object[n];
        for (int i = 0; i < n; i++) {
            list[i] = randString();
        }
        return list;
    }
    private static String[] generateRandomStrArr(int n){
        String[] list = new String[n];
        for (int i = 0; i < n; i++) {
            list[i] = randString();
        }
        return list;
    }
}
