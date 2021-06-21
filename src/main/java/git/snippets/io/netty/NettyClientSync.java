package git.snippets.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @since
 */
public class NettyClientSync {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();
        thread.register(client);
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());
        ChannelFuture connect = client.connect(new InetSocketAddress("192.168.205.138", 9090));
        ChannelFuture sync = connect.sync();
        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture send = client.writeAndFlush(buf);
        send.sync();
        sync.channel().closeFuture().sync();
        System.out.println("client over....");
    }

    @ChannelHandler.Sharable
    static class MyInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            System.out.println("client  register...");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("client active...");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            CharSequence str = buf.getCharSequence(0, buf.readableBytes(), UTF_8);
            System.out.println(str);
            ctx.writeAndFlush(buf);
        }
    }
}


