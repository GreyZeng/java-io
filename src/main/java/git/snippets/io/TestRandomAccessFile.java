package git.snippets.io;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestRandomAccessFile {

    public static void main(String[] args) throws Exception {
        commonWrite();
        channelWrite();
    }

    private static void commonWrite() {
        String path = "C:\\git\\out.txt";
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
            randomAccessFile.write("Hello xxxld".getBytes(UTF_8));
            randomAccessFile.seek(6);
            randomAccessFile.write("Wor".getBytes(UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void channelWrite() throws Exception {
        String path = "C:\\git\\out.txt";
        byte[] data = "1234567\n".getBytes();
        RandomAccessFile file = new RandomAccessFile(path, "rw");
        FileChannel channel = file.getChannel();
        int size = 4096 * 100;
        // 只有文件（块设备）才会做内存映射，才会有map方法
        // 堆外的和文件的bytebuffer
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        while (size != 0) {
            // 不是系统调用 但是数据会到达内核的pagecache
            // 曾经我们需要用out.write()这样的系统调用才能让数据到达内核的pagecache（必须需要内核到用户态切换）
            map.put(data);
            size -= data.length;
        }
        map.force();
    }


}
