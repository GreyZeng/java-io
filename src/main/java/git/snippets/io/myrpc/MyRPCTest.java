package git.snippets.io.myrpc;

import git.snippets.io.myrpc.proxy.MyProxy;
import git.snippets.io.myrpc.rpc.Dispacher;
import git.snippets.io.myrpc.rpc.service.Car;
import git.snippets.io.myrpc.rpc.service.Fly;
import git.snippets.io.myrpc.rpc.service.MyCar;
import git.snippets.io.myrpc.rpc.service.MyFly;
import git.snippets.io.myrpc.rpc.transport.ServerDecode;
import git.snippets.io.myrpc.rpc.transport.ServerRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.IOException;
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
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                System.out.println("server accept client..." + nioSocketChannel.remoteAddress().getPort());
                ChannelPipeline p = nioSocketChannel.pipeline();
                p.addLast(new ServerDecode()).addLast(new ServerRequestHandler());
            }
        }).bind(new InetSocketAddress("localhost", 9090));
        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void get() {
        AtomicInteger num = new AtomicInteger(0);
//        new Thread(this::startServer).start();

        System.out.println("start server...");
        int size = 10;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = MyProxy.proxyGet(Car.class);
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


        //  Fly fly = proxyGet(Fly.class);

    }


}

