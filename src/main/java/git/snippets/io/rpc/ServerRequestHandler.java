package git.snippets.io.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since
 */
public class ServerRequestHandler extends ChannelInboundHandlerAdapter {
    // provider
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBuf copy = buf.copy();
        if (buf.readableBytes() >= 98) {
            byte[] bytes = new byte[98];
            buf.readBytes(bytes);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();
            System.out.println("client response @id:" + header.getRequestID());
            if (buf.readableBytes() >= header.getDataLen()) {
                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);
                ByteArrayInputStream dd = new ByteArrayInputStream(data);
                ObjectInputStream din = new ObjectInputStream(dd);
                MyContent content = (MyContent) din.readObject();
                System.out.println(content.getName());
            }
        }
        ChannelFuture channelFuture = ctx.writeAndFlush(copy);
        channelFuture.sync();
    }
}
