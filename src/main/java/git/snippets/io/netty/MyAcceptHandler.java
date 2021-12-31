package git.snippets.io.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;

/**
 * 接收客户端，然后分配selector
 *
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since 11
 */
public class MyAcceptHandler extends ChannelInboundHandlerAdapter {
    private final EventLoopGroup selector;
    private final ChannelHandler handler;

    public MyAcceptHandler(EventLoopGroup thread, ChannelHandler myInHandler) {
        this.selector = thread;
        this.handler = myInHandler;

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server registered...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // listen socket accept client
        // socket R/W
        io.netty.channel.socket.SocketChannel c = (io.netty.channel.socket.SocketChannel) msg;
        ChannelPipeline pipeline = c.pipeline();
        pipeline.addLast(handler);
        selector.register(c);

    }
}
