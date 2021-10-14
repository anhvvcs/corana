package utils;

public class MyStr {
    private final StringBuilder sb;

    public MyStr(Object... obj) {
        sb = new StringBuilder();
        append(obj);
    }

    public void append(Object... obj) {
        for (Object o : obj) {
            sb.append(o.toString());
        }
    }

    public MyStr append(Object obj, int i, int j) {
        String s = obj.toString();
        for (int k = i; k < j; k++) {
            append(s.charAt(k));
        }
        return this;
    }

    public String value() {
        return sb.toString();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
