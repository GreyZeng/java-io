package git.snippets.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since 11
 */
@ChannelHandler.Sharable
public class MyInHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        System.out.println("client registered....");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("client active.....");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        // CharSequence cs = buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8);
        CharSequence cs = buf.getCharSequence(0, buf.readableBytes(), StandardCharsets.UTF_8);
        System.out.println(cs.toString());
        ctx.writeAndFlush(buf);
    }
}
