package git.snippets.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @since
 */
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bs = new ServerBootstrap();
        ChannelFuture bind = bs
                .group(group, group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioServerSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioServerSocketChannel.pipeline();
                        pipeline.addLast(new NettyClientSync.MyInHandler());
                    }
                }).bind(new InetSocketAddress("192.168.205.1", 9090));
        bind.sync().channel().closeFuture().sync();
    }
}
