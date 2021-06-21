package git.snippets.io.netty;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @since
 */
public class NettyServerSync {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();
        thread.register(server);
        ChannelPipeline p = server.pipeline();
        p.addLast(new MyAcceptHandler(thread, new NettyClientSync.MyInHandler()));
        ChannelFuture bind = server.bind(new InetSocketAddress("192.168.205.1",9090));
        bind.sync().channel().closeFuture().sync();
        System.out.println("server close....");
    }

    static class MyAcceptHandler extends ChannelInboundHandlerAdapter {


        private final EventLoopGroup selector;
        private final ChannelHandler handler;

        public MyAcceptHandler(EventLoopGroup thread, ChannelHandler myInHandler) {
            this.selector = thread;
            this.handler = myInHandler;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            System.out.println("server registered...");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            SocketChannel client = (SocketChannel) msg;
            ChannelPipeline p = client.pipeline();
            p.addLast(handler);
            selector.register(client);
        }
    }
}

