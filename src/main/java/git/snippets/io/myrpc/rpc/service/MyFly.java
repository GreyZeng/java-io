package git.snippets.io.myrpc.rpc.service;

public class MyFly implements Fly {

    @Override
    public void fly(String msg) {
        System.out.println("server,get client arg:" + msg);
    }
}
