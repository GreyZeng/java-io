package git.snippets.io;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO Socket Server
 */
public class SocketServerBIOTest {
    private static final int PORT = 9090;
    private static final int BACK_LOG = 2;

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(PORT), BACK_LOG);
            System.out.println("server started , port : " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // 接受客户端连接
            while (true) {
                // 先阻塞，这样客户端暂时无法连接进来
                System.in.read();

                // 这个方法也是阻塞的，如果没有客户端连接进来，会一直阻塞在这里，除非设置了超时时间
                Socket client = server.accept();

                System.out.println("client " + client.getPort() + " connected!!!");
                // 客户端连接进来后，开辟一个新的线程去接收并处理
                new Thread(() -> {
                    try {
                        InputStream inputStream = client.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        char[] data = new char[1024];
                        while (true) {
                            int num = reader.read(data);
                            if (num > 0) {
                                System.out.println("client read some data is :" + num + " val :" + new String(data, 0, num));
                            } else if (num == 0) {
                                System.out.println("client read nothing!");
                                continue;
                            } else {
                                System.out.println("client read -1...");
                                System.in.read();
                                client.close();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
