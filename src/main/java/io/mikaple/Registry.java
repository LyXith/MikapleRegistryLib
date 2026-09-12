package io.mikaple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Registry<T> {
    private final String type;
    private final List<T> entries = new ArrayList<>();
    private Map<Integer,T> idsMap = Collections.emptyMap();
    private boolean isFrozen = false;

    protected Registry(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    protected void onRegister(T entry) {
    }

    public boolean register(T entry) {
        if (isFrozen) return false;
        if (entry == null || entries.contains(entry) ) return false;
        entries.addLast(entry);
        onRegister(entry);
        return true;
    }

    protected void onUnregister(T entry) {

    }

    public boolean unregister(T entry) {
        if (isFrozen) return false;
        if (entry == null || !entries.contains(entry)) return false;
        entries.remove(entry);
        onUnregister(entry);
        return true;
    }

    public List<T> getEntries() {
        return List.copyOf(entries);
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
        idsMap = Collections.unmodifiableMap(map);
        freeze();
        return idsMap;
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

        idsMap = Collections.unmodifiableMap(syncedIdsMap);
        freeze();
        return true;
    }

    public Map<Integer,T> getIdsMap() {
        return idsMap;
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
        idsMap = Collections.emptyMap();
        return true;
    }
}
