package org.cucumbergrid.junit.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {

    public static <K,V> HashMap<K,V> filter(Map<K, V> map, final Collection<K> keys) {
        return filter(map, new Function<K, Boolean>() {
            @Override
            public Boolean apply(K input) {
                return keys.contains(input);
            }
        });
    }

    public static <K,V> HashMap<K,V> filter(Map<K, V> map, Function<K, Boolean> matcher) {
        HashMap<K, V> result = new HashMap<>();
        for (K key : map.keySet()) {
            if (matcher.apply(key)) {
                result.put(key, map.get(key));
            }
        }
        return result;
    }
}
