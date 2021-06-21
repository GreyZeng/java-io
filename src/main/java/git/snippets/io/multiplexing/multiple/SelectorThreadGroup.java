package git.snippets.io.multiplexing.multiple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.channels.ServerSocketChannel.open;


public class SelectorThreadGroup {

    SelectorThread[] bosses;
    SelectorThread[] workers;

    ServerSocketChannel server = null;
    AtomicInteger xid = new AtomicInteger(0);

    SelectorThreadGroup(int bossNum, int workerNum) {
        bosses = new SelectorThread[bossNum];
        workers = new SelectorThread[workerNum];
        for (int i = 0; i < bossNum; i++) {
            bosses[i] = new SelectorThread(this);
            new Thread(bosses[i]).start();
        }
        for (int i = 0; i < workerNum; i++) {
            workers[i] = new SelectorThread(this);
            new Thread(workers[i]).start();
        }
    }

    public void bind(int port) {
        try {
            server = open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            nextSelector(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextSelector(Channel c) {
        try {
            SelectorThread st;
            if (c instanceof ServerSocketChannel) {
                st = nextBoss();
            } else {
                st = nextWork();
            }
            st.lbq.put(c);
            st.selector.wakeup();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SelectorThread nextBoss() {
        int index = xid.incrementAndGet() % bosses.length;
        return bosses[index];
    }

    private SelectorThread nextWork() {
        int index = xid.incrementAndGet() % workers.length;  //动用worker的线程分配
        return workers[index];
    }
}
