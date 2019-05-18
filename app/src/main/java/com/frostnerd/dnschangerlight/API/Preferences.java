package com.frostnerd.dnschangerlight.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 *
 * Terms on usage of my code can be found here: https://git.frostnerd.com/PublicAndroidApps/DnsChanger/blob/master/README.md
 *
 * <p>
 * development@frostnerd.com
 */
public final class Preferences {
    private static final ConcurrentHashMap<String,Object> local = new ConcurrentHashMap<>();
    private static boolean forcePreference = false;
    private static boolean debug;

    public static void setDebug(boolean debug){
        Preferences.debug = debug;
    }

    public static void setForcePreference(boolean forcePreference) {
        Preferences.forcePreference = forcePreference;
    }

    public static SharedPreferences getDefaultPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Map<String,Object> getAll(Context context, boolean onlyLocal){
        Map<String,Object> data = new HashMap<>();
        for(String s: local.keySet())data.put(s, local.get(s));
        if(!onlyLocal){
            Map<String,?> all = getDefaultPreferences(context).getAll();
            for(String s: all.keySet())if(!data.containsKey(s))data.put(s, all.get(s));
        }
        return data;
    }

    public static void put(SharedPreferences sp,String key,Object value){
        if(debug)System.out.println("Preference Changed: '" + key + "' new value: " + value + " Type: " + getTypeName(value));
        if(VariableChecker.isInteger(value))sp.edit().putInt(key,Integer.parseInt(value.toString())).commit();
        else if(VariableChecker.isLong(value))sp.edit().putLong(key,Long.parseLong(value.toString())).commit();
        else if(VariableChecker.isFloat(value))sp.edit().putFloat(key, Float.parseFloat(value.toString())).commit();
        else if(VariableChecker.isSet(value))sp.edit().putStringSet(key,(Set<String>)value).commit();
        else if(VariableChecker.isBoolean(value))sp.edit().putBoolean(key,Boolean.parseBoolean(value.toString())).commit();
        else if(VariableChecker.isString(value))sp.edit().putString(key,(String)value).commit();
        else if(debug)System.out.println("UNKNOWN TYPE. CLASS: " + value.getClass() + " VAL: " + value + "  KEY: " + key);
        local.put(key,value);
    }

    public static void put(SharedPreferences sp,String key,Object value,boolean putSharedPreferences){
        if(debug)System.out.println("Preference Changed: '" + key + "' new value: " + value + " Type: " + getTypeName(value));
        if(putSharedPreferences){
            if(VariableChecker.isInteger(value))sp.edit().putInt(key,Integer.parseInt(value.toString())).commit();
            else if(VariableChecker.isLong(value))sp.edit().putLong(key,Long.parseLong(value.toString())).commit();
            else if(VariableChecker.isFloat(value))sp.edit().putFloat(key,Float.parseFloat(value.toString())).commit();
            else if(VariableChecker.isSet(value))sp.edit().putStringSet(key,(Set<String>)value).commit();
            else if(VariableChecker.isBoolean(value))sp.edit().putBoolean(key,Boolean.parseBoolean(value.toString())).commit();
            else if(VariableChecker.isString(value))sp.edit().putString(key,(String)value).commit();
            else if(debug)System.out.println("UNKNOWN TYPE. CLASS: " + value.getClass() + " VAL: " + value + "  KEY: " + key);
        }
        local.put(key,value);
    }

    public static void putLocal(String key,Object value){
        if(debug)System.out.println("'" + key + "' put locally (" + value + ")");
        put(null,key,value,false);
    }

    public static void put(Context c,String key,Object value){
        put(getDefaultPreferences(c),key,value);
    }

    public static void flushBuffer(){
        local.clear();
    }

    public static <V> V get(Context c,Class<V> type,String key,V defaultValue){
        if(debug)System.out.println("Local contains '" + key + "': " + local.containsKey(key) + ", forcing prefs: " + forcePreference);
        if(local.containsKey(key) && !forcePreference)return (V)local.get(key);
        else if(getDefaultPreferences(c).contains(key))return (V)getDefaultPreferences(c).getAll().get(key);
        return defaultValue;
    }

    public static <V> V get(SharedPreferences sp,Class<V> type,String key,V defaultValue){
        if(debug)System.out.println("Local contains '" + key + "': " + local.containsKey(key) + ", forcing prefs: " + forcePreference);
        if(local.containsKey(key) && !forcePreference)return (V)local.get(key);
        else if(sp.contains(key))return (V)sp.getAll().get(key);
        return defaultValue;
    }

    public static Set<String> getStringSet(Context c, String key){
        return (Set<String>)get(c, Set.class,key,new HashSet<String>());
    }

    public static boolean getBoolean(Context c,String key,boolean defaultValue){
        return get(c,Boolean.class,key,defaultValue);
    }

    public static String getString(Context c,String key,String defaultValue){
        return get(c,String.class,key,defaultValue);
    }

    public static int getInteger(Context c,String key,int defaultValue){
        return get(c,Integer.class,key,defaultValue);
    }

    public static double getDouble(Context c,String key,double defaultValue){
        return get(c,Double.class,key,defaultValue);
    }

    public static long getLong(Context c,String key,long defaultValue){
        return get(c,Long.class,key,defaultValue);
    }

    public static float getFloat(Context c,String key,float defaultValue){
        return get(c,Float.class,key,defaultValue);
    }

    public static boolean getBoolean(SharedPreferences sp,String key,boolean defaultValue){
        return get(sp,Boolean.class,key,defaultValue);
    }

    public static String getString(SharedPreferences sp,String key,String defaultValue){
        return get(sp,String.class,key,defaultValue);
    }

    public static int getInteger(SharedPreferences sp,String key,int defaultValue){
        return get(sp,Integer.class,key,defaultValue);
    }

    public static double getDouble(SharedPreferences sp,String key,double defaultValue){
        return get(sp,Double.class,key,defaultValue);
    }

    public static long getLong(SharedPreferences sp,String key,long defaultValue){
        return get(sp,Long.class,key,defaultValue);
    }

    public static float getFloat(SharedPreferences sp,String key,float defaultValue){
        return get(sp,Float.class,key,defaultValue);
    }

    public static PreferenceGetter getter(Context c){
        return new PreferenceGetter(c);
    }

    public static PreferenceGetter getter(SharedPreferences preferences){
        return new PreferenceGetter(preferences);
    }

    private static String getTypeName(Object value){
        if(VariableChecker.isInteger(value))return "Integer";
        else if(VariableChecker.isLong(value))return "Long";
        else if(VariableChecker.isFloat(value))return "Float";
        else if(VariableChecker.isSet(value))return "Set";
        else if(VariableChecker.isBoolean(value))return "Boolean";
        else if(VariableChecker.isString(value))return "String";
        return "Unknown";
    }

    public static PreferenceType getType(Context c,String key){
        return getType(get(c,Object.class,key,null));
    }

    public static PreferenceType getType(Object value){
        if(value == null)return PreferenceType.NULL;
        if(VariableChecker.isInteger(value))return PreferenceType.INTEGER;
        else if(VariableChecker.isLong(value))return PreferenceType.LONG;
        else if(VariableChecker.isFloat(value))return PreferenceType.FLOAT;
        else if(VariableChecker.isSet(value))return PreferenceType.SET;
        else if(VariableChecker.isBoolean(value))return PreferenceType.BOOLEAN;
        else if(VariableChecker.isString(value))return PreferenceType.STRING;
        return PreferenceType.UNKNOWN;
    }

    public static String exportToString(Context context, boolean onlyLocal, String entrySeperator){
        return exportToString(context, onlyLocal, entrySeperator, new ArrayList<String>());
    }

    public static String exportToString(Context context, boolean onlyLocal, String entrySeperator, String... excludeFromExport ){
        return exportToString(context, onlyLocal, entrySeperator, Arrays.asList(excludeFromExport));
    }

    public static String exportToString(Context context, boolean onlyLocal, String entrySeparator, List<String> excludeFromExport){
        Map<String,PreferenceValue> values = new HashMap<>();
        Object value;
        for(String s: local.keySet()){
            if(excludeFromExport.contains(s))continue;
            value = local.get(s);
            values.put(s, new PreferenceValue(value, getType(value)));
        }
        if(!onlyLocal) {
            Map<String,?> all = getDefaultPreferences(context).getAll();
            for (String s : all.keySet()) {
                if(values.containsKey(s) || excludeFromExport.contains(s))continue;
                value = all.get(s);
                values.put(s, new PreferenceValue(value, getType(value)));
            }
        }
        String result = "";
        PreferenceValue preferenceValue;
        for(String s: values.keySet()){
            preferenceValue = values.get(s);
            result += s + "<-->" + preferenceValue.type.getID() + "<-->" + (preferenceValue.type == PreferenceType.SET ? setToString((Set<?>)preferenceValue.value) :
                    preferenceValue.value.toString()) + entrySeparator;
        }
        return result.substring(0, result.length()-entrySeparator.length());
    }

    private static class PreferenceValue{
        Object value;
        PreferenceType type;

        PreferenceValue(Object value, PreferenceType type){
            this.value = value;
            this.type = type;
        }
    }

    public static HashMap<String, Object> importFromString(String text, String entrySeparator){
        HashMap<String, Object> data = new HashMap<>();
        PreferenceType type;
        String key;
        String[] splt;
        for(String s: text.split(entrySeparator)){
            splt = s.split("<-->");
            if(splt.length < 3 || !VariableChecker.isInteger(splt[1]))continue;
            key = splt[0];
            type = PreferenceType.getByID(Integer.parseInt(splt[1]));
            data.put(key, type.parseTo(splt[2]));
        }
        return data;
    }

    public static void importFromStringAndPut(Context context,String text, String entrySeparator){
        HashMap<String, Object> data = importFromString(text, entrySeparator);
        for(String s: data.keySet()){
            put(context,s,data.get(s));
        }
    }

    public static <V> String setToString(Set<V> set){
        if(set.size() == 0)return "";
        Object[] arr = set.toArray();
        String s = getType(arr[0]).getID() + "<--->";
        for(int i = 0; i < arr.length;i++){
            s += arr[i].toString();
            if(i < arr.length-1)s += ";_;";
        }
        return s;
    }

    public static Set<?> setFromString(String s){
        if(s.split("<--->").length < 2 || !VariableChecker.isInteger(s.split("<--->")[0]))return new HashSet<String>();
        PreferenceType type = PreferenceType.getByID(Integer.parseInt(s.split("<--->")[0]));
        Set<Object> set = new HashSet<>();
        for(String entry: s.split("<--->")[1].split(";_;")){
            set.add(type.parseTo(entry));
        }
        return set;
    }

    public static enum PreferenceType{
        NULL(-1),STRING(0),INTEGER(1),LONG(2),FLOAT(3),SET(4),BOOLEAN(5),UNKNOWN(6);

        private final int ID;
        PreferenceType(int ID){
            this.ID = ID;
        }

        public int getID(){
            return ID;
        }

        public static PreferenceType getByID(int id){
            for(PreferenceType val: PreferenceType.values())if(val.getID() == id)return val;
            return null;
        }

        public Object parseTo(Object origin){
            if(this == NULL)return null;
            if(this == UNKNOWN)return origin;
            if(this == STRING)return (String)origin;
            if(this == BOOLEAN){
                if(origin instanceof Boolean|| origin.getClass().getSimpleName().endsWith("Boolean"))return (Boolean)origin;
                else return Boolean.parseBoolean(origin.toString());
            }
            if(this == LONG){
                if(origin instanceof Long|| origin.getClass().getSimpleName().endsWith("Long"))return (Long)origin;
                else return Long.parseLong(origin.toString());
            }
            if(this == FLOAT){
                if(origin instanceof Float|| origin.getClass().getSimpleName().endsWith("Float"))return (Float)origin;
                else return Long.parseLong(origin.toString());
            }
            if(this == INTEGER){
                if(origin instanceof Integer|| origin.getClass().getSimpleName().endsWith("Integer"))return (Integer)origin;
                else return Integer.parseInt(origin.toString());
            }
            if(this == SET){
                String arrWrap = origin.toString().substring(1, origin.toString().length()-1);
                if(origin instanceof Set || origin.getClass().getSimpleName().endsWith("Set"))return (Set)origin;
                else return setFromString(origin.toString());
            }
            return null;
        }
    }

    public static final class PreferenceGetter{
        private SharedPreferences preference;

        public PreferenceGetter(Context context){
            this.preference = getDefaultPreferences(context);
        }

        public PreferenceGetter(SharedPreferences preference){
            this.preference = preference;
        }

        public <V> V get(Class<V> type,String key,V defaultValue){
            return Preferences.get(preference,type,key,defaultValue);
        }

        public Set<String> getStringSet(Context c, String key){
            return (Set<String>)Preferences.get(preference, Set.class,key,new HashSet<String>());
        }

        public boolean getBoolean(String key,boolean defaultValue){
            return Preferences.get(preference,Boolean.class,key,defaultValue);
        }

        public String getString(String key,String defaultValue){
            return Preferences.get(preference,String.class,key,defaultValue);
        }

        public int getInteger(String key,int defaultValue){
            return Preferences.get(preference,Integer.class,key,defaultValue);
        }

        public double getDouble(String key,double defaultValue){
            return Preferences.get(preference,Double.class,key,defaultValue);
        }

        public long getLong(String key,long defaultValue){
            return Preferences.get(preference,Long.class,key,defaultValue);
        }

        public float getFloat(String key,float defaultValue){
            return Preferences.get(preference,Float.class,key,defaultValue);
        }
    }

    public static final class PreferenceSetter{
        private SharedPreferences preference;
        private boolean applyInstant,useApply;
        private SharedPreferences.Editor editor;

        public PreferenceSetter(Context c){
            preference = getDefaultPreferences(c);
            editor = preference.edit();
        }

        public PreferenceSetter(SharedPreferences sp){
            preference = sp;
            editor = sp.edit();
        }

        public void setApply(boolean useApply){
            this.useApply = useApply;
        }

        public void setApplyInstant(boolean instant){
            applyInstant = instant;
        }

        public void put(String key,Object value){
            if(!canBePut(value))throw new IllegalArgumentException("That type of value cannot be put into the SharedPreference");
            if(VariableChecker.isInteger(value))putInt(key,Integer.parseInt(value.toString()));
            else if(VariableChecker.isLong(value))putLong(key,Long.parseLong(value.toString()));
            else if(VariableChecker.isFloat(value))putFloat(key,Float.parseFloat(value.toString()));
            else if(VariableChecker.isSet(value))putStringSet(key,(Set<String>)value);
            else if(VariableChecker.isBoolean(value))putBoolean(key,Boolean.parseBoolean(value.toString()));
            else if(VariableChecker.isString(value))putString(key,(String)value);
        }

        public void put(HashMap<String,Object> insertment){
            for(String s: insertment.keySet()){
                put(s,insertment.get(s));
            }
        }

        public void putBoolean(String key,boolean value){
            editor.putBoolean(key,value);
            applyChanges();
        }

        public void putString(String key,String value){
            editor.putString(key,value);
            applyChanges();
        }

        public void putLong(String key,long value){
            editor.putLong(key,value);
            applyChanges();
        }

        public void putFloat(String key,float value){
            editor.putFloat(key,value);
            applyChanges();
        }

        public void putInt(String key,int value){
            editor.putInt(key,value);
            applyChanges();
        }

        public void putStringSet(String key,Set<String> value){
            editor.putStringSet(key,value);
            applyChanges();
        }

        private void applyChanges(){
            if(applyInstant){
                if(useApply)apply();
                else commit();
            }
        }

        public void apply(){
            editor.apply();
        }

        public void commit(){
            editor.commit();
        }

        public boolean canBePut(Object value){
            if(VariableChecker.isString(value))return true;
            else if(VariableChecker.isInteger(value))return true;
            else if(VariableChecker.isLong(value))return true;
            else if(VariableChecker.isFloat(value))return true;
            else if(VariableChecker.isSet(value))return true;
            else if(VariableChecker.isBoolean(value))return true;
            return false;
        }
    }
}
