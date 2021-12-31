package git.snippets.io.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since 11
 */
public class NettyServerPoor {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();
        thread.register(server);
        ChannelPipeline p = server.pipeline();
        p.addLast(new MyAcceptHandler(thread, new ChannelInit()));
        ChannelFuture fu = server.bind(new InetSocketAddress("localhost", 9090));
        fu.sync().channel().closeFuture().sync();
        System.out.println("close...");
    }
}
