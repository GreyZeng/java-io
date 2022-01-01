package git.snippets.io.rpc;

import git.snippets.io.rpc.util.PackMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since 11
 */
public class ClientResponses extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackMsg response = (PackMsg) msg;
        ResponseMappingCallback.runCallBack(response);
    }
}
