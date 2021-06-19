package git.snippets.io.multiplexing.multiple;

public class Startup {

    public static void main(String[] args) {
        SelectorThreadGroup group = new SelectorThreadGroup(3,3);
        group.bind(9999);
        group.bind(8888);
        group.bind(6666);
        group.bind(7777);
    }
}
