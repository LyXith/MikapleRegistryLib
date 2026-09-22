package io.mikaple.regfiles;

import io.mikaple.objectinfo.ObjectIdentity;
import io.mikaple.objectinfo.ObjectInfo;
import io.mikaple.objectinfo.ObjectInfoValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MregFileParser {

    private static final Logger log = LoggerFactory.getLogger("MregFileParser");

    public static List<MregFileInfo> readFiles(File path, String namespace) throws IOException {
        File[] files = path.listFiles();
        if (files == null) return new ArrayList<>();
        List<MregFileInfo> mregFileInfos = new ArrayList<>();
        for (File file : files) {
            MregFileInfo mregFileInfo = read(file,namespace);
            if (mregFileInfo != null) mregFileInfos.add(mregFileInfo);
        }
        return mregFileInfos;
    }
    public static MregFileInfo read(File file, String namespace) throws IOException {
        if (file.canRead() && file.getName().endsWith(".mreg")) {
            List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
            ObjectInfo objectInfo = new ObjectInfo();
            String type = "";
            String name = "";
            boolean failed = false;
            for (int i = 0; i < lines.size(); i++) {
                try {
                    String line = lines.get(i);
                    if (line.isBlank() || line.startsWith("#")) continue;
                    if (Character.isWhitespace(line.charAt(0))) {
                        throw new IllegalArgumentException("Unexpected format: " + line);
                    }
                    LineInfo lineInfo = lineParser(line);
                    if (lineInfo.key.equals("type") && lineInfo.type == ObjectInfoValueType.STR && type.isEmpty()) {
                        type = lineInfo.value;
                    } else if (lineInfo.key.equals("name") && lineInfo.type == ObjectInfoValueType.STR && name.isEmpty()) {
                        name = lineInfo.value;
                    } else {
                        objectInfo.putKV(lineInfo.key, lineInfo.value, lineInfo.type);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("{}, at: {}", e.getMessage(), i);
                    failed = true;
                }
            }
            if (type.isEmpty() || name.isEmpty()) {
                log.warn("Unexpected format.");
                failed = true;
            }
            if (failed) return null;
            objectInfo.putId(new ObjectIdentity(namespace, name));
            return new MregFileInfo(type, objectInfo);
        }
        return null;
    }

    private static LineInfo lineParser(String line) {
        line = line.trim();
        if (!line.contains("=")) {
            throw new IllegalArgumentException("Unexpected format: " + line);
        }
        String[] splits = line.split("\\s+", 2);
        if (splits.length < 2) {
            throw new IllegalArgumentException("Unexpected format: " + line);
        }
        String typeString = splits[0];
        ObjectInfoValueType type = ObjectInfo.getTypeFromString(typeString);
        String KV = splits[1];
        String[] kv = KV.split("=", 2);
        String key = kv[0].trim();
        String value = kv[1].trim();

        return new LineInfo(key,value,type);
    }

    private record LineInfo(String key, String value, ObjectInfoValueType type) {
    }
}
