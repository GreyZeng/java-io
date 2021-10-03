/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package git.snippets.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Grey
 */
public class BIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9999);
        Socket socket = server.accept();
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        while (true) {
            byte[] buf = new byte[5];
            int len = in.read(buf);
            if (len == -1) {
                throw new RuntimeException("连接已经断开");
            }
            System.out.println("recv :" + new String(buf, 0, len));
            out.write(buf, 0, len);
        }
    }
}
