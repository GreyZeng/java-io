package git.snippets.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;


public class C10Kclient {

    public static void main(String[] args) {
        LinkedList<SocketChannel> clients = new LinkedList<>();
        InetSocketAddress serverAddr = new InetSocketAddress("192.168.205.138", 9090);
        for (int i = 10000,j = 10001; i < 65000; i+=2,j+=2) {
            try {
                SocketChannel client1 = SocketChannel.open();
                SocketChannel client2 = SocketChannel.open();
                client1.bind(new InetSocketAddress("192.168.205.1", i )); 
                client1.connect(serverAddr);
                clients.add(client1);

                client2.bind(new InetSocketAddress("192.168.205.1", j));
                client2.connect(serverAddr);
                clients.add(client2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("clients "+ clients.size());
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
