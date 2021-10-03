/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package git.snippets.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Grey
 */
public class BIOClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello".getBytes().length);
        Socket socket = new Socket("127.0.0.1",9999);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        byte[] send = "Hello".getBytes();
        while(true) {
            out.write(send);
            byte[] buf = new byte[5];
            int len = in.read(buf,0,send.length);
            if (len == -1) {
                throw new RuntimeException("连接已断开");
            }
            System.out.println("recv : " + new String(buf,0, len));
            Thread.sleep(1000);
        }
    }
}
