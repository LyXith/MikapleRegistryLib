package io.mikaple.regfiles;

import io.mikaple.objectinfo.ObjectInfo;
import io.mikaple.objectinfo.ObjectInfoValueType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MregFileParser {

    public static List<MregFileInfo> readFiles(File path) throws IOException {
        File[] files = path.listFiles();
        if (files == null) return new ArrayList<>();
        List<MregFileInfo> mregFileInfos = new ArrayList<>();
        for (File file : files) {
            MregFileInfo mregFileInfo = read(file);
            if (mregFileInfo != null) mregFileInfos.add(mregFileInfo);
        }
        return mregFileInfos;
    }
    public static MregFileInfo read(File file) throws IOException {
        if (file.canRead() && file.getName().endsWith(".mreg")) {
            List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
            ObjectInfo objectInfo = new ObjectInfo();
            String type = "";
            String name = "";
            boolean failed = false;
            for (String line : lines) {
                try {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    LineInfo lineInfo = lineParser(line);
                    if (lineInfo.key.equals("type")) {
                        type = lineInfo.value;
                    } else if (lineInfo.key.equals("name")) {
                        name = lineInfo.value;
                    } else {
                        objectInfo.putKV(lineInfo.key, lineInfo.value, lineInfo.type);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    failed = true;
                }
            }
            if (type.isEmpty() || name.isEmpty()) {
                System.out.println("Unexpected format.");
                failed = true;
            }
            if (failed) return null;
            return new MregFileInfo(type, name, objectInfo);
        }
        return null;
    }

    private static LineInfo lineParser(String line) {
        if (!line.contains("=")) {
            throw new IllegalArgumentException("Unexpected format: " + line);
        }
        line = line.trim();
        String[] splits = line.split("\\s+", 2);
        if (splits.length < 2) {
            throw new IllegalArgumentException("Unexpected format: " + line);
        }
        String typeString = splits[0];
        ObjectInfoValueType type = switch (typeString) {
            case "bool" -> ObjectInfoValueType.BOOL;
            case "int" -> ObjectInfoValueType.INT;
            case "long" -> ObjectInfoValueType.LONG;
            case "double" -> ObjectInfoValueType.DOUBLE;
            case "float" -> ObjectInfoValueType.FLOAT;
            case "str" -> ObjectInfoValueType.STR;
            default -> throw new IllegalArgumentException("Unexpected type: " + typeString);
        };
        String KV = splits[1];
        String[] kv = KV.split("=", 2);
        String key = kv[0].trim();
        String value = kv[1].trim();

        return new LineInfo(key,value,type);
    }

    private record LineInfo(String key, String value, ObjectInfoValueType type) {
    }
}
