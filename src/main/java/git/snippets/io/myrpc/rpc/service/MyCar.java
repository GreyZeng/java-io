package git.snippets.io.myrpc.rpc.service;

public class MyCar implements Car {

    @Override
    public String run(String msg) {
        System.out.println("server,get client arg:" + msg);
        return "server res " + msg;
    }
}
