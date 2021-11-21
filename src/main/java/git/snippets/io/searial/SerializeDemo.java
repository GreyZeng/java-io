package git.snippets.io.searial;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * 序列化示例
 *
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/11/21
 * @since 1.8
 */
public class SerializeDemo {
    public static void main(String[] args) throws IOException {
        Employee e = new Employee();
        e.name = "Reyan Ali";
        e.address = "Phokka Kuan, Ambehta Peer";
        e.SSN = 11122333;
        e.number = 101;

        FileOutputStream fileOut = new FileOutputStream("C:\\git\\sample\\employee.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(e);
        out.close();
        fileOut.close();

    }
}
