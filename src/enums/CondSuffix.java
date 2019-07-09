package enums;

public enum CondSuffix {
    EQ('e'), NE('n'), CS('c'), HS('h'), CC('x'), LO('l'), MI('m'), PL('p'),
    VS('s'), VC('v'), HI('i'), LS('k'), GE('q'), LT('t'), GT('g'), LE('z'), AL('a');

    private char s;

    CondSuffix(char s) {
        this.s = s;
    }

    public static CondSuffix getByName(char name) {
        for (CondSuffix type : CondSuffix.values())
            if (type.s == name)
                return type;
        return null;
    }

    public char getS() {
        return s;
    }

    public void setS(char s) {
        this.s = s;
    }
}
