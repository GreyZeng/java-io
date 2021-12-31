package git.snippets.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledDirectByteBuf;

import java.nio.ByteBuffer;

public class TestByteBuffer {
    public static void main(String[] args) {
        jdkByteBuffer();
        nettyByteBuf();
    }

    private static void nettyByteBuf() {

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8, 20);
        // 其他分配方式，池化，非池化，堆内，堆外
        // buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        // buf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        // 以下会报错，超过最大容量
        buf.writeBytes(new byte[]{1, 2, 3, 4});
    }

    private static void print(ByteBuf buf) {
        System.out.println(buf);
        System.out.println(buf.isReadable());
        System.out.println(buf.readerIndex());
        System.out.println(buf.readableBytes());
        System.out.println(buf.isWritable());
        System.out.println(buf.writableBytes());
        System.out.println(buf.capacity());
        System.out.println(buf.maxCapacity());
        System.out.println(buf.isDirect());
        System.out.println("---end---");
    }


    private static void jdkByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        //  ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("mark: " + buffer);

        buffer.put("123".getBytes());

        System.out.println("-------------put:123......");
        System.out.println("mark: " + buffer);

        buffer.flip(); // 读写交替

        System.out.println("-------------flip......");
        System.out.println("mark: " + buffer);

        buffer.get();

        System.out.println("-------------get......");
        System.out.println("mark: " + buffer);

        buffer.compact();

        System.out.println("-------------compact......");
        System.out.println("mark: " + buffer);

        buffer.clear();

        System.out.println("-------------clear......");
        System.out.println("mark: " + buffer);
    }
}
