package git.snippets.io.rpc.service;

public class MyFly implements Fly {

    @Override
    public void fly(String msg) {
        System.out.println("server,get client arg:" + msg);
    }
}
