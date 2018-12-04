import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class CustomHash<K,V> implements CustomMap<K,V> {
    private static final int numValuePerBucket = 4;
    private static final int T = 4194303;
    private Object[] hashArray = new Object[T];

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        if (hashArray[(key.hashCode() & 0x7fffffff) % T] == null) {
            Node<K,V> newNode = new Node<>();
            newNode.setValue(key, value);
            hashArray[(key.hashCode() & 0x7fffffff) % T] = newNode;
        } else {
            Node<K,V> currNode = (Node<K,V>)hashArray[(key.hashCode() & 0x7fffffff) % T];
            while (!currNode.setValue(key, value)) {
                if (currNode.getNext() != null)
                    currNode = currNode.getNext();
                else {
                    // make new node
                    Node<K,V> newNode = new Node<>();
                    currNode.setNext(newNode);
                    currNode = currNode.getNext();
                }
            }
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Node<K,V> currNode = (Node<K,V>)hashArray[(key.hashCode() & 0x7fffffff) % T];
        while (currNode != null) {
            if (currNode.posKey(key) >= 0)
                return currNode.getValue(currNode.posKey(key));
            else
                currNode = currNode.getNext();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (int i = 0; i < T; i++) {
            if (hashArray[i] != null) {
                Node<K,V> currNode = (Node<K,V>) hashArray[i];
                while (currNode != null) {
                    for (int j = 0; j < currNode.getCountValue(); j++)
                        keySet.add(currNode.getKey(j));
                    currNode = currNode.getNext();
                }
            }
        }
        return keySet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        Node<K,V> currNode = (Node<K,V>)hashArray[(key.hashCode() & 0x7fffffff) % T];
        while (currNode != null) {
            if (currNode.posKey(key) > 0)
                return true;
            currNode = currNode.getNext();
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void save(String fileName) {
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(new File(fileName)))) {
            for (int i = 0 ; i < T; i++) {
                if (hashArray[i] != null) {
                    os.writeInt(i);
                    Node<K,V> currNode = (Node<K,V>) hashArray[i];
                    while (currNode != null) {
                        for (int j = 0; j < currNode.getCountValue(); j++) {
                            os.writeUTF((String) currNode.getKey(j));
                            os.writeInt((Integer) currNode.getValue(j));
                        }
                        currNode = currNode.getNext();
                    }
                    os.writeUTF(",");
                }
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(String fileName) {
        try (DataInputStream is = new DataInputStream(new FileInputStream(new File(fileName)))) {
            while (is.available() > 0) {
                int pos = is.readInt();
                String key = is.readUTF();
                while (!key.equals(",")) {
                    int value = is.readInt();
                    if (hashArray[pos] == null) {
                        Node<String, Integer> currNode = new Node<>();
                        currNode.setValue(key, value);
                        hashArray[pos] = currNode;
                    } else {
                        Node<String, Integer> currNode = (Node<String, Integer>) hashArray[pos];
                        while (!currNode.setValue(key, value)) {
                            if (currNode.getNext() != null)
                                currNode = currNode.getNext();
                            else {
                                // make new node
                                Node<String, Integer> newNode = new Node<>();
                                currNode.setNext(newNode);
                                currNode = currNode.getNext();
                            }
                        }
                    }
                    key = is.readUTF();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Node type, inner private class.
     */
    private static class Node<K,V> {
        // Stored value of Node.
        protected Object[] hPair = new Object[numValuePerBucket];
        // Reference to next node.
        protected Node<K,V> hNext;
        // Save the first empty position of oValue[]
        private int countValue;

        public Node() {
            hNext = null;
            countValue = 0;
        }

        @SuppressWarnings("unchecked")
        public int posKey(Object key) {
            for (int i = 0; i < countValue; i++) {
                if (((Pair<K,V>)hPair[i]).getKey().hashCode() == key.hashCode())
                    return i;
            }
            return -1;
        }

        @SuppressWarnings("unchecked")
        public K getKey(int i) { return ((Pair<K,V>)hPair[i]).getKey(); }
        @SuppressWarnings("unchecked")
        public V getValue(int i) { return ((Pair<K,V>)hPair[i]).getValue(); }
        public Node<K,V> getNext() { return hNext; }
        public int getCountValue() { return countValue; }

        @SuppressWarnings("unchecked")
        public boolean setValue(K key, V value) {
            if (countValue < numValuePerBucket) {
                hPair[countValue] = new Pair<>(key, value);
                countValue++;
                return true;
            }
            return false;
        }
        public void setNext(Node<K,V> next) { hNext = next; }
    }

    private static class Pair<K,V> {
        K key;
        V value;
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }
        public V getValue() { return value; }
        public void setKey(K key) { this.key = key; }
        public void setValue(V value) { this.value = value; }
    }
}
