package git.snippets.io.multiplexing.multiple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.currentThread;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.Selector.open;

public class SelectorThread extends ThreadLocal<LinkedBlockingQueue<Channel>> implements Runnable {
    Selector selector = null;
    LinkedBlockingQueue<Channel> lbq = get();
    SelectorThreadGroup stg;

    @Override
    protected LinkedBlockingQueue<Channel> initialValue() {
        return new LinkedBlockingQueue<>();
    }

    SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;
            selector = open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {
                        }
                    }
                }
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, OP_ACCEPT);
                        System.out.println(currentThread().getName() + " register listen");
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer buffer = allocateDirect(4096);
                        client.register(selector, OP_READ, buffer);
                        System.out.println(currentThread().getName() + " register client: " + client.getRemoteAddress());
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void readHandler(SelectionKey key) {
        System.out.println(currentThread().getName() + " read......");
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            try {
                int num = client.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (num == 0) {
                    break;
                } else {
                    //客户端断开了
                    System.out.println("client: " + client.getRemoteAddress() + "closed......");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        System.out.println(currentThread().getName() + "   acceptHandler......");

        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            stg.nextSelector(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
