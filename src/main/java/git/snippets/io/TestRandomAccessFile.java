package git.snippets.io;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestRandomAccessFile {

    public static void main(String[] args) {
        String path = "C:\\workspace\\out.txt";
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
            randomAccessFile.write("Hello xxxld".getBytes(UTF_8));
            randomAccessFile.seek(6);
            randomAccessFile.write("Wor".getBytes(UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void testRandomAccessFileIO() throws Exception {
        String path = "C:\\workspace\\out.txt";
        byte[] data = "1234567\n".getBytes();
        RandomAccessFile file = new RandomAccessFile(path, "rw");
        FileChannel channel = file.getChannel();
        int size = 4096 * 100;
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        while (size != 0) {
            map.put(data);
            size -= data.length;
        }
        map.force();
    }


}
