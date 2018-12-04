import java.util.Set;

public interface CustomMap<K,V> {
    V put(K key, V value);
    V get(K key);
    Set<K> keySet();
    boolean containsKey(Object key);
    void save(String fileName);
    void load(String fileName);
}
