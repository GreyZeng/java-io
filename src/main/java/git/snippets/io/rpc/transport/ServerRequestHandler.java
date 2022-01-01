package git.snippets.io.rpc.transport;


import git.snippets.io.rpc.Dispacher;
import git.snippets.io.rpc.protocol.MyContent;
import git.snippets.io.rpc.protocol.MyHeader;
import git.snippets.io.rpc.util.PackMsg;
import git.snippets.io.rpc.util.SerialUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since
 */
public class ServerRequestHandler extends ChannelInboundHandlerAdapter {


    // provider
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        PackMsg requestPkg = (PackMsg) msg;

        ctx.executor().execute(new Runnable() {
//        ctx.executor().parent().next().execute(new Runnable() {

            @Override
            public void run() {

                String serviceName = requestPkg.getContent().getName();
                String method = requestPkg.getContent().getMethodName();
                Object c = Dispacher.getDis().get(serviceName);
                Class<?> clazz = c.getClass();
                Object res = null;
                try {


                    Method m = clazz.getMethod(method, requestPkg.getContent().getParameterTypes());
                    res = m.invoke(c, requestPkg.getContent().getArgs());


                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }


//                String execThreadName = Thread.currentThread().getName();
                MyContent content = new MyContent();
//                String s = "io thread: " + ioThreadName + " exec thread: " + execThreadName + " from args:" + requestPkg.content.getArgs()[0];
                content.setRes(res);
                byte[] contentByte = SerialUtil.ser(content);

                MyHeader resHeader = new MyHeader();
                resHeader.setRequestID(requestPkg.getHeader().getRequestID());
                resHeader.setFlag(0x14141424);
                resHeader.setDataLen(contentByte.length);
                byte[] headerByte = SerialUtil.ser(resHeader);
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerByte.length + contentByte.length);

                byteBuf.writeBytes(headerByte);
                byteBuf.writeBytes(contentByte);
                ctx.writeAndFlush(byteBuf);
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client close");
    }
}

