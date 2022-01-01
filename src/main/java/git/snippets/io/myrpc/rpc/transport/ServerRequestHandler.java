package git.snippets.io.myrpc.rpc.transport;

import git.snippets.io.myrpc.util.PackMsg;
import git.snippets.io.myrpc.util.SerialUtil;
import git.snippets.io.myrpc.rpc.Dispacher;
import git.snippets.io.myrpc.rpc.protocol.MyContent;
import git.snippets.io.myrpc.rpc.protocol.MyHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
        MyHeader header = new MyHeader();


        String ioThread = Thread.currentThread().getName();
        // 1. 直接在当前方法处理业务逻辑
        // 2. 自己创建线程池
        // 3. 使用netty自己的eventloop来处理业务以及返回
        ctx.executor().execute(new Runnable() {
            @Override
            public void run() {
                String serviceName = requestPkg.getContent().getName();
                String method = requestPkg.getContent().getMethodName();
                Object c = Dispacher.getDis().get(serviceName);
                Class<?> clazz = c.getClass();
                Method m = null;
                Object res = null;
                try {
                    m = clazz.getMethod(method, requestPkg.getContent().getParameterTypes());
                    res = m.invoke(c, requestPkg.getContent().getArgs());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // io线程和exec可能同一个
                String execName = Thread.currentThread().getName();
                MyContent content = new MyContent();
                // String all = "io thread:" + ioThread + " exec thread:" + execName + " from args:" + requestPkg.getContent().getArgs()[0];
                //System.out.println(all);
                content.setRes((String) res);
                byte[] contentByte = SerialUtil.ser(content);
                header.setRequestID(requestPkg.getHeader().getRequestID());
                header.setFlag(0x14141424);
                byte[] headerByte = SerialUtil.ser(header);
                header.setDataLen(contentByte.length);
                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerByte.length + contentByte.length);
                byteBuf.writeBytes(headerByte);
                byteBuf.writeBytes(contentByte);
                // ctx.writeAndFlush(byteBuf);
            }
        });
        // 注意和上述的区别
//        ctx.executor().parent().next().execute(new Runnable() {
//            @Override
//            public void run() {
//                // io线程和exec可能同一个
//                String execName = Thread.currentThread().getName();
//                MyContent content = new MyContent();
//                String all = "io thread:" + ioThread + " exec thread:" + execName + " from args:" + buf.getContent().getArgs()[0];
//                System.out.println(all);
//                content.setRes(all);
//                byte[] contentByte = SerialUtil.ser(content);
//                header.setRequestID(buf.getHeader().getRequestID());
//                header.setFlag(0x14141424);
//                byte[] headerByte = SerialUtil.ser(header);
//                header.setDataLen(contentByte.length);
//                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerByte.length + contentByte.length);
//                byteBuf.writeBytes(headerByte);
//                byteBuf.writeBytes(contentByte);
//                ctx.writeAndFlush(byteBuf);
//            }
//        });
        System.out.println("server handler:" + requestPkg.getContent().getArgs()[0]);
    }
}

