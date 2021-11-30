package git.snippets.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 测试NIO文件拷贝
 *
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/11/30
 * @since 1.8
 */
public class FileCopyTest {
    public static void main(String[] args) throws IOException {
        File source = new File("D:\\leak_info.txt");
        File dest = new File("D:\\leak_info2.txt");
        fileCopy(source, dest);
    }

    static void fileCopy(File source, File dest) throws
            IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel targetChannel = new FileOutputStream(dest).getChannel();) {
            for (long count = sourceChannel.size(); count > 0; ) {
                long transferred = sourceChannel.transferTo(
                        sourceChannel.position(), count, targetChannel);
                sourceChannel.position(sourceChannel.position() + transferred);
                count -= transferred;
            }
        }
    }
}
