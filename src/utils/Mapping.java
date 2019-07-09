package utils;

import enums.CondSuffix;

import java.util.HashMap;

public class Mapping {
    public static HashMap<String, Character> condStrToChar = new HashMap<>();
    public static HashMap<Character, String> condCharToStr = new HashMap<>();
    public static HashMap<Character, String> regCharToStr = new HashMap<>();
    public static HashMap<String, Character> regStrToChar = new HashMap<>();

    static {
        CondSuffix[] condSuffixes = CondSuffix.values();
        for (CondSuffix c : condSuffixes) {
            condStrToChar.put(c.name(), c.getS());
            condCharToStr.put(c.getS(), c.name());
        }
    }
}