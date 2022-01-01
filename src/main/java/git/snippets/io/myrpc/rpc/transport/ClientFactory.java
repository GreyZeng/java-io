package git.snippets.io.myrpc.rpc.transport;

import git.snippets.io.myrpc.rpc.ClientResponses;
import git.snippets.io.myrpc.rpc.ResponseMappingCallback;
import git.snippets.io.myrpc.util.SerialUtil;
import git.snippets.io.myrpc.rpc.protocol.MyContent;
import git.snippets.io.myrpc.rpc.protocol.MyHeader;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ClientFactory {
    int poolSize = 5;
    NioEventLoopGroup clientWorker;
    Random rand = new Random();

    private ClientFactory() {
    }

    private static final ClientFactory factory;

    static {
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }

    public static CompletableFuture<Object> transport(MyContent content) {
        byte[] msgBody = SerialUtil.ser(content);


        // 2. requestID + message，本地要缓存
        MyHeader header = MyHeader.createHeader(msgBody);

        // 解决数据decode问题
        byte[] msgHeader = SerialUtil.ser(header);
        System.out.println("msgHeader :" + msgHeader.length);

        NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));
        // 4. 发送--》走IO out-》netty
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);
        long requestID = header.getRequestID();
        //CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Object> res = new CompletableFuture<>();
        ResponseMappingCallback.addCallBack(requestID, res);
        buf.writeBytes(msgHeader);
        buf.writeBytes(msgBody);

        ChannelFuture channelFuture = clientChannel.writeAndFlush(buf);
        return res;
    }

    //一个consumer 可以连接很多的provider，每一个provider都有自己的pool  K,V

    ConcurrentHashMap<InetSocketAddress, ClientPool> outboxs = new ConcurrentHashMap<>();

    public synchronized NioSocketChannel getClient(InetSocketAddress address) {

        ClientPool clientPool = outboxs.get(address);
        if (clientPool == null) {
            synchronized (outboxs) {
                if (clientPool == null) {
                    outboxs.putIfAbsent(address, new ClientPool(poolSize));
                    clientPool = outboxs.get(address);

                }
            }
        }

        int i = rand.nextInt(poolSize);

        if (clientPool.clients[i] != null && clientPool.clients[i].isActive()) {
            return clientPool.clients[i];
        } else {
            synchronized (clientPool.lock[i]) {
                if (clientPool.clients[i] == null || !clientPool.clients[i].isActive()) {
                    clientPool.clients[i] = create(address);
                }
            }
        }
        return clientPool.clients[i];
    }

    private NioSocketChannel create(InetSocketAddress address) {

        //基于 netty 的客户端创建方式
        clientWorker = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(clientWorker).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new ServerDecode());
                p.addLast(new ClientResponses());  //解决给谁的？？  requestID..
            }
        }).connect(address);
        try {
            NioSocketChannel client = (NioSocketChannel) connect.sync().channel();
            return client;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;


    }


}
