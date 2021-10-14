package external.testing;

import external.jni.CLibrary;
import external.jni.CStruct;
import utils.TypeRandom;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Random;

public class APITest extends Thread {
    Random generator;
    Method[] apiMethods = CLibrary.class.getDeclaredMethods();
    String funcname;

    private Method getMethod(String name) {
        for (Method method : apiMethods) {
            if (method.getName().equals(name)) return method;
        }
        return apiMethods[0];
    }

    public void run() {
        try {
            generator = new Random();
            Method method = getMethod(funcname);

            method.setAccessible(true);
            Object[] params = new Object[method.getParameterCount()];
            Type[] paramTypes = method.getGenericParameterTypes();
            for (int i = 0; i < method.getParameterCount(); i++) {
                params[i] = TypeRandom.rand(paramTypes[i].getTypeName());
                //  System.out.println(params[i].toString());
            }
            String output = String.valueOf(method.invoke(CLibrary.INSTANCE, params));

            System.out.println(method.getName() + ":");
            System.out.println(output);

        } catch (Exception e) {
            System.out.println("+ Error: " + funcname);
        }
    }

    public static void main(String[] args) {
        Method[] apiMethods = CLibrary.class.getDeclaredMethods();
        for (Method method : apiMethods) {
            APITest t = new APITest();
            t.funcname = method.getName();
            t.start();
        }
        //APITest.test("setmntent");

    }
}
