package io.mikaple;

import io.mikaple.objectinfo.ObjectIdentity;
import io.mikaple.objectinfo.ObjectInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Registry<T> {
    private final String type;
    private final List<T> entries = new ArrayList<>();
    private final Set<T> entryIndex = new HashSet<>();
    private final Map<T, ObjectInfo> entryInfosMap = new HashMap<>();
    private final Map<ObjectIdentity, T> entryIdsMap = new HashMap<>();
    private Map<Integer,T> numIdsMap = Collections.emptyMap();
    private boolean isFrozen = false;

    protected Registry(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    protected void onRegister(T entry) {
    }

    public boolean register(T entry, ObjectInfo objectInfo) {
        if (isFrozen) return false;
        if (entry == null || !entryIndex.add(entry)) return false;
        entryIdsMap.put(objectInfo.getId(), entry);
        entryInfosMap.put(entry, objectInfo);
        entries.addLast(entry);
        onRegister(entry);
        return true;
    }

    protected void onUnregister(T entry) {

    }

    public boolean unregister(T entry) {
        if (isFrozen) return false;
        if (entry == null || !entryIndex.contains(entry)) return false;
        entryIndex.remove(entry);
        entryIdsMap.remove(entryInfosMap.get(entry).getId());
        entryInfosMap.remove(entry);
        entries.remove(entry);
        onUnregister(entry);
        return true;
    }

    public List<T> getEntries() {
        return List.copyOf(entries);
    }

    public T byId(int i) {
        return numIdsMap.get(i);
    }

    public ObjectInfo getObjectInfo(T entry) {
        return entryInfosMap.get(entry);
    }

    public Map<Integer, T> createIdsMap() {

        Map<Integer, T> map = IntStream.range(0, entries.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        entries::get,
                        (a, _) -> a,
                        LinkedHashMap::new
                ));
        numIdsMap = Collections.unmodifiableMap(map);
        freeze();
        return numIdsMap;
    }

    public boolean syncIdsMap(Map<Integer,T> syncIdsMap) {
        if (isFrozen) return false;
        if (syncIdsMap == null) return false;

        if (syncIdsMap.size() > entries.size()) return false;

        Set<T> entrySet = new HashSet<>(entries);
        Set<T> seen = new HashSet<>();
        for (T value : syncIdsMap.values()) {
            if (value == null || !entrySet.contains(value)) return false;
            if (!seen.add(value)) return false;
        }

        List<T> unsyncedIds = new ArrayList<>();
        for (T entry : entries) {
            if (!seen.contains(entry)) {
                unsyncedIds.add(entry);
            }
        }

        LinkedHashMap<Integer, T> syncedIdsMap = new LinkedHashMap<>(syncIdsMap);
        int nextKey = 0;
        for (T unsyncedId : unsyncedIds) {
            while (syncedIdsMap.containsKey(nextKey)) {
                nextKey++;
            }
            syncedIdsMap.put(nextKey, unsyncedId);
            nextKey++;
        }

        numIdsMap = Collections.unmodifiableMap(syncedIdsMap);
        freeze();
        return true;
    }

    public Map<Integer,T> getNumIdsMap() {
        return numIdsMap;
    }

    public void unfreeze() {
        isFrozen = false;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void freeze() {
        isFrozen = true;
    }

    protected void onClear() {
        for (T entry : entries) {
            onUnregister(entry);
        }
    }

    public boolean clear() {
        if (isFrozen) return false;
        onClear();
        entries.clear();
        entryIndex.clear();
        entryInfosMap.clear();
        entryIdsMap.clear();
        numIdsMap = Collections.emptyMap();
        return true;
    }
}
