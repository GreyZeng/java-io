package git.snippets.io.myrpc.rpc;

import git.snippets.io.myrpc.util.PackMsg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseMappingCallback {
    static ConcurrentHashMap<Long, CompletableFuture> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long id, CompletableFuture ch) {
        mapping.putIfAbsent(id, ch);
    }

    public static void runCallBack(PackMsg msg) {
        CompletableFuture runnable = mapping.get(msg.getHeader().getRequestID());
        runnable.complete(msg.getContent().getRes());
        removeCB(msg.getHeader().getRequestID());
    }

    public static void removeCB(long id) {
        mapping.remove(id);
    }
}
