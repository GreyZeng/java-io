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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Grey
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(9999));
        // 设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        // 关心Accept事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isAcceptable()) {
                    // 新客户端连接进来
                    SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                    //NIO规定，必须要用Buffer进行读写
                    ByteBuffer buffer = ByteBuffer.allocateDirect(32);
                    int len = socketChannel.read(buffer);
                    if(len == -1){
                        throw  new RuntimeException("连接已断开");
                    }
                    //上面那一步只是读到缓冲区，这里是从缓冲区真正的拿出数据
                    byte[] buf = new byte[len];
                    //这个操作可以举个例子
                    //例如read(buffer)的时候，其实内部是调用了buffer.put这个方法
                    //那么read结束，position的位置必定等于len
                    //所以我们必须重置一下position为0，才可以从头开始读，但是读到什么地方呢？
                    //那就需要设置limit = position，所以flip后，position=0， limit = len
                    buffer.flip();
                    buffer.get(buf);
                    System.out.println("recv:" + new String(buf, 0, len));
                    //注册写事件
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                } else if (selectionKey.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                    //写数据，也要用Buffer来写
                    int len = socketChannel.write(ByteBuffer.wrap("hello".getBytes()));
                    if(len == -1){
                        throw  new RuntimeException("连接已断开");
                    }
                    //这里为什么要取消写事件呢？因为只要底层的写缓冲区不满，就会一直收到这个事件
                    //所以只有想写数据的时候，才要注册这个写事件
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                }
            }
        }
    }
}
