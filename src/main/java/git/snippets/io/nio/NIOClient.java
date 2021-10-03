/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package git.snippets.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Grey
 */
public class NIOClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                //连接事件
                if (selectionKey.isConnectable()) {
                    if (socketChannel.finishConnect()) {
                        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE);
                    }
                } else if (selectionKey.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(32);
                    int len = socketChannel.read(buffer);
                    if (len == -1) {
                        throw new RuntimeException("连接已断开");
                    }
                    byte[] buf = new byte[len];
                   buffer.flip();
                    buffer.get(buf);
                    System.out.println("recv:" + new String(buf, 0, len));
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                } else if (selectionKey.isWritable()) {
                    int len = socketChannel.write(ByteBuffer.wrap("hello".getBytes()));
                    if (len == -1) {
                        throw new RuntimeException("连接已断开");
                    }
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    //这个只是控制一下发送数据的速度
                    Thread.sleep(1000);
                }
            }
        }
    }
}
