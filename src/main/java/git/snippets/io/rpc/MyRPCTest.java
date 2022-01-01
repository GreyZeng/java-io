package git.snippets.io.rpc;


import git.snippets.io.rpc.protocol.MyContent;
import git.snippets.io.rpc.proxy.MyProxy;
import git.snippets.io.rpc.service.Car;
import git.snippets.io.rpc.service.Fly;
import git.snippets.io.rpc.service.MyCar;
import git.snippets.io.rpc.service.MyFly;
import git.snippets.io.rpc.service.Person;
import git.snippets.io.rpc.transport.MyHttpRpcHandler;
import git.snippets.io.rpc.util.SerialUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. 拆包
 * <p>
 * 2. 动态代理，序列号，协议封装
 * <p>
 * 3. 连接池
 *
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since 11
 */
public class MyRPCTest {
    @Test
    public void startServer() {
        MyCar car = new MyCar();
        MyFly fly = new MyFly();
        Dispacher dis = Dispacher.getDis();
        dis.register(Car.class.getName(), car);
        dis.register(Fly.class.getName(), fly);
        NioEventLoopGroup boss = new NioEventLoopGroup(20);
        NioEventLoopGroup worker = boss;
        ServerBootstrap bs = new ServerBootstrap();
        ChannelFuture bind = bs.group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                System.out.println("server accept client..." + ch.remoteAddress().getPort());
                ChannelPipeline p = ch.pipeline();
                // 自定义协议
                // p.addLast(new ServerDecode()).addLast(new ServerRequestHandler());
                // HTTP协议
                p.addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(1024 * 512)).addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        FullHttpRequest request = (FullHttpRequest) msg;
                        System.out.println(request.toString());


                        ByteBuf content = request.content();
                        byte[] data = new byte[content.readableBytes()];
                        content.readBytes(data);
                        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
                        MyContent myContent = (MyContent) oin.readObject();

                        String serviceName = myContent.getName();
                        String method = myContent.getMethodName();
                        Object c = dis.get(serviceName);
                        Class<?> clazz = c.getClass();
                        Object res = null;
                        try {
                            Method m = clazz.getMethod(method, myContent.getParameterTypes());
                            res = m.invoke(c, myContent.getArgs());
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        MyContent resContent = new MyContent();
                        resContent.setRes(res);
                        byte[] contentByte = SerialUtil.ser(resContent);

                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.copiedBuffer(contentByte));

                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentByte.length);

                        //http协议，header+body
                        ctx.writeAndFlush(response);
                    }
                });
            }
        }).bind(new InetSocketAddress("localhost", 9090));
        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startHttpServer() {
        MyCar car = new MyCar();
        MyFly fly = new MyFly();

        Dispacher dis = Dispacher.getDis();

        dis.register(Car.class.getName(), car);
        dis.register(Fly.class.getName(), fly);


        //tomcat jetty  【servlet】
        Server server = new Server(new InetSocketAddress("localhost", 9090));
        ServletContextHandler handler = new ServletContextHandler(server, "/");
        server.setHandler(handler);
        handler.addServlet(MyHttpRpcHandler.class, "/*");  //web.xml
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void get() {

        AtomicInteger num = new AtomicInteger(0);
        int size = 50;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = MyProxy.proxyGet(Car.class);//动态代理实现   //是真的要去触发 RPC调用吗？
                String arg = "hello" + num.incrementAndGet();
                String res = car.run(arg);
                System.out.println("client over msg: " + res + " src arg: " + arg);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRPC() {

        Car car = MyProxy.proxyGet(Car.class);
        Person zhangsan = car.ofPerson("zhangsan", 16);
        System.out.println(zhangsan);
    }


    @Test
    public void testRpcLocal() {
        new Thread(this::startServer).start();

        System.out.println("server started......");

        Car car = MyProxy.proxyGet(Car.class);
        Person zhangsan = car.ofPerson("zhangsan", 16);
        System.out.println(zhangsan);
    }
}

