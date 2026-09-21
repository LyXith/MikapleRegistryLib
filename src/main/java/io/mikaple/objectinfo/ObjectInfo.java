package io.mikaple.objectinfo;

import java.util.HashMap;
import java.util.Map;

public class ObjectInfo {
    private final Map<String, String> KVMap = new HashMap<>();
    private final Map<String, ObjectInfoValueType> valueTypeMap = new HashMap<>();

    public ObjectInfo() {

    }

    public void putKV(String k,String v,ObjectInfoValueType valueType) {
        KVMap.put(k,v);
        valueTypeMap.put(v,valueType);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        String value = KVMap.get(key);
        if (value == null) {
            return null;
        }
        ObjectInfoValueType valueType = valueTypeMap.get(key);
        if (valueType == null) {
            return null;
        }
        Object result = switch (valueType) {
            case INT -> Integer.valueOf(value);
            case LONG -> Long.valueOf(value);
            case DOUBLE -> Double.valueOf(value);
            case FLOAT -> Float.valueOf(value);
            case BOOL -> Boolean.valueOf(value);
            case STR -> value;
        };
        return (T) result;
    }
}
