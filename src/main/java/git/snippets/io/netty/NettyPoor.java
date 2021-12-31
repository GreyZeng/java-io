package git.snippets.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Netty常规使用（简陋版本)
 *
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since 11
 */
public class NettyPoor {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();
        thread.register(client);
        ChannelFuture connect = client.connect(new InetSocketAddress("192.168.150.128", 9090));
        ChannelFuture sync = connect.sync();
        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello World".getBytes(StandardCharsets.UTF_8));
        ChannelFuture fu = client.writeAndFlush(byteBuf);
        fu.sync();
        sync.channel().closeFuture().sync();
        System.out.println("over...");


    }
}
