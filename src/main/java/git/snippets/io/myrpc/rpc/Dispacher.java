package git.snippets.io.myrpc.rpc;

import java.util.concurrent.ConcurrentHashMap;

public class Dispacher {
    private static Dispacher dis = null;

    static {
        dis = new Dispacher();
    }

    public static Dispacher getDis() {
        return dis;
    }

    private Dispacher() {

    }

    public static ConcurrentHashMap<String, Object> invokeMap = new ConcurrentHashMap<>();

    public void register(String k, Object obj) {
        invokeMap.put(k, obj);
    }

    public Object get(String k) {
        return invokeMap.get(k);
    }
}
