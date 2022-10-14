package io.ix0rai.tantalisingteas.util;

import java.util.HashMap;
import java.util.Map;

/**
 * a simple map that counts the number of times a key is added.
 * provides a utility method, {@link #highestValue()} to get the key with the highest value, as well as shorthand, {@link #increment(T)} to increment the value of a key
 *
 * @param <T> the type of keys maintained by this map
 * @author ix0rai
 */
public class CountMap<T> extends HashMap<T, Integer> {
    public void increment(T key) {
        this.put(key, this.getOrDefault(key, 0) + 1);
    }

    public void reset(T key) {
        this.put(key, 0);
    }

    public T highestValue() {
        T highestValue = null;
        int highestCount = 0;

        // run over map and find element with the highest value
        for (Map.Entry<T, Integer> entry : this.entrySet()) {
            if (entry.getValue() > highestCount) {
                highestValue = entry.getKey();
                highestCount = entry.getValue();
            }
        }

        return highestValue;
    }
}
