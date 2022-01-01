package git.snippets.io.rpc.transport;


import git.snippets.io.rpc.ClientResponses;
import git.snippets.io.rpc.ResponseMappingCallback;
import git.snippets.io.rpc.protocol.MyContent;
import git.snippets.io.rpc.protocol.MyHeader;
import git.snippets.io.rpc.util.SerialUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: 马士兵教育
 * @create: 2020-08-16 20:51
 */
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

//       自定义协议 String type = "rpc";
        String type = "http";
        CompletableFuture<Object> res = new CompletableFuture<>();

        if (type.equals("rpc")) {
            byte[] msgBody = SerialUtil.ser(content);
            MyHeader header = MyHeader.createHeader(msgBody);
            byte[] msgHeader = SerialUtil.ser(header);
            System.out.println("main:::" + msgHeader.length);


            NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));

            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);

            long id = header.getRequestID();

            ResponseMappingCallback.addCallBack(id, res);
            byteBuf.writeBytes(msgHeader);
            byteBuf.writeBytes(msgBody);
            ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
        } else {
            //使用http协议为载体
            //1，用URL 现成的工具（包含了http的编解码，发送，socket，连接）
            //urlTS(content, res);

            nettyTS(content, res);
        }


        return res;
    }

    private static void nettyTS(MyContent content, CompletableFuture<Object> res) {

        NioEventLoopGroup group = new NioEventLoopGroup(1);//定义到外面
        Bootstrap bs = new Bootstrap();
        Bootstrap client = bs.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new HttpClientCodec()).addLast(new HttpObjectAggregator(1024 * 512)).addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        FullHttpResponse response = (FullHttpResponse) msg;
                        System.out.println(response.toString());

                        ByteBuf resContent = response.content();
                        byte[] data = new byte[resContent.readableBytes()];
                        resContent.readBytes(data);

                        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
                        MyContent o = (MyContent) oin.readObject();


                        res.complete(o.getRes());
                    }
                });
            }
        });

        try {
            ChannelFuture syncFuture = client.connect("localhost", 9090).sync();
            //2，发送

            Channel clientChannel = syncFuture.channel();
            byte[] data = SerialUtil.ser(content);
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/", Unpooled.copiedBuffer(data));

            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, data.length);

            clientChannel.writeAndFlush(request).sync();//作为client 向server端发送：http  request
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void urlTS(MyContent content, CompletableFuture<Object> res) {

        //这种方式是每请求占用一个连接的方式，因为使用的是http协议
        Object obj = null;
        try {
            URL url = new URL("http://localhost:9090/");

            HttpURLConnection hc = (HttpURLConnection) url.openConnection();

            //post
            hc.setRequestMethod("POST");
            hc.setDoOutput(true);
            hc.setDoInput(true);


            OutputStream out = hc.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(content);  //这里真的发送了嘛？

            if (hc.getResponseCode() == 200) {
                InputStream in = hc.getInputStream();
                ObjectInputStream oin = new ObjectInputStream(in);
                MyContent myContent = (MyContent) oin.readObject();
                obj = myContent.getRes();

            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        res.complete(obj);

    }


    //一个consumer 可以连接很多的provider，每一个provider都有自己的pool  K,V

    ConcurrentHashMap<InetSocketAddress, ClientPool> outboxs = new ConcurrentHashMap<>();


    public NioSocketChannel getClient(InetSocketAddress address) {

        //TODO 在并发情况下一定要谨慎
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
                if (clientPool.clients[i] == null || !clientPool.clients[i].isActive())
                    clientPool.clients[i] = create(address);
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
                p.addLast(new ClientResponses());
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

