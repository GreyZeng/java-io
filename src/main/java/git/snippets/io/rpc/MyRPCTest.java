package git.snippets.io.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

class ResponseHandler {
    static ConcurrentHashMap<Long, Runnable> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long id, Runnable ch) {
        mapping.putIfAbsent(id, ch);
    }

    public static void runCallBack(long id) {
        Runnable runnable = mapping.get(id);
        runnable.run();
    }

    public static void removeCB(long id) {
        mapping.remove(id);
    }
}

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

    public static void main(String[] args) {
        new Thread(() -> {
            new MyRpCServer().startServer();
        }).start();
        System.out.println("start server...");
        int size = 20;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = proxyGet(Car.class);
                car.run("hello");
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

    private static <T> T proxyGet(Class<T> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        Class<?>[] methodInfo = {clazz};
        return (T) Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 1. 调用服务，方法，参数--》封装成message
                String name = clazz.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                MyContent context = new MyContent();
                context.setMethodName(methodName);
                context.setName(name);
                context.setParameterTypes(parameterTypes);
                context.setArgs(args);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                oout.writeObject(context);
                byte[] msgBody = out.toByteArray();


                // 2. requestID + message，本地要缓存
                MyHeader header = createHeader(msgBody);

                out.reset();
                oout = new ObjectOutputStream(out);
                oout.writeObject(header);
                // 解决数据decode问题
                byte[] msgHeader = out.toByteArray();
                //System.out.println("msgHeader :" + msgHeader.length);
                // 3. 连接池：取得连接

                ClientFactory factory = ClientFactory.getFactory();
                NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));
                // 4. 发送--》走IO out-》netty
                ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);
                long requestID = header.getRequestID();
                CountDownLatch latch = new CountDownLatch(1);
                ResponseHandler.addCallBack(requestID, () -> latch.countDown());
                buf.writeBytes(msgHeader).writeBytes(msgBody);

                ChannelFuture channelFuture = clientChannel.writeAndFlush(buf);
                channelFuture.sync();

                latch.await();
                // channel.writeAndFlush(ByteBuf)
                // 5. 如果从IO，未来回来了，怎么将代码执行到这里
                // 如何让线程停下来，你还能让它继续
                return proxy;
            }


            private MyHeader createHeader(byte[] msgBody) {
                int length = msgBody.length;
                int flag = 0x14141414;
                long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());
                MyHeader header = new MyHeader();
                header.setFlag((int) flag);
                header.setDataLen(length);
                header.setRequestID(requestID);
                return header;
            }
        });
    }

}

class ClientFactory {
    int poolSize = 1;
    Random random = new Random();

    private ClientFactory() {

    }

    private static final ClientFactory factory;

    static {
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }

    ConcurrentHashMap<InetSocketAddress, ClientPool> outboxs = new ConcurrentHashMap<>();

    public synchronized NioSocketChannel getClient(InetSocketAddress address) {
        ClientPool clientPool = outboxs.get(address);
        if (clientPool == null) {
            outboxs.putIfAbsent(address, new ClientPool(poolSize));
            clientPool = outboxs.get(address);
        }
        int i = random.nextInt(poolSize);
        if (clientPool.clients[i] != null && clientPool.clients[i].isActive()) {
            return clientPool.clients[i];
        }
        synchronized (clientPool.lock[i]) {
            return clientPool.clients[i] = create(address);
        }
    }

    NioEventLoopGroup clientWork;

    private NioSocketChannel create(InetSocketAddress address) {
        // 基于Netty的客户端
        clientWork = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(clientWork).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                pipeline.addLast(new ClientResponses());
            }
        }).connect(address);

        try {
            return (NioSocketChannel) connect.sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class ClientPool {
    NioSocketChannel[] clients;
    Object[] lock;

    ClientPool(int size) {
        clients = new NioSocketChannel[size];
        lock = new Object[size];
        for (int i = 0; i < lock.length; i++) {
            lock[i] = new Object();
        }
    }
}

class MyContent implements Serializable {
    String name;
    String methodName;
    Class<?>[] parameterTypes;
    Object[] args;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}

interface Car {
    void run(String str);
}

interface Fly {
    void fly(String str);
}