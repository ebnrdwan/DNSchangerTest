package com.frostnerd.dnschangerlight.API;

import java.util.Set;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 *
 * Terms on usage of my code can be found here: https://git.frostnerd.com/PublicAndroidApps/DnsChanger/blob/master/README.md
 *
 * <p>
 * development@frostnerd.com
 */
public class VariableChecker {

    public static boolean isInteger(Object value){
        return value instanceof Integer|| value.getClass().getSimpleName().endsWith("Integer") || isInteger(value.toString());
    }

    private static boolean isInteger(String s){
        try{
            Integer.parseInt(s);
            return true;
        }catch(Exception e){

        }
        return false;
    }

    public static boolean isLong(Object value){
        return value instanceof Long|| value.getClass().getSimpleName().endsWith("Long") || isLong(value.toString());
    }

    private static boolean isLong(String s){
        try{
            Integer.parseInt(s);
            return true;
        }catch(Exception e){

        }
        return false;
    }

    public static boolean isFloat(Object value){
        return value instanceof Float|| value.getClass().getSimpleName().endsWith("Float") || isFloat(value.toString());
    }

    private static boolean isFloat(String s){
        try{
            Integer.parseInt(s);
            return true;
        }catch(Exception e){

        }
        return false;
    }

    public static boolean isBoolean(Object value){
        return value instanceof Boolean|| value.getClass().getSimpleName().endsWith("Boolean") || isBoolean(value.toString());
    }

    private static boolean isBoolean(String s){
        return s.equalsIgnoreCase("false") || s.equalsIgnoreCase("true");
    }

    public static boolean isSet(Object value){
        return value instanceof Set || value.getClass().getSimpleName().endsWith("Set");
    }

    public static boolean isString(Object value){
        return value instanceof String || value.getClass().getSimpleName().endsWith("String");
    }

}
