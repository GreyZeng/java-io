package git.snippets.io.myrpc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2022/1/1
 * @since
 */
public class SerialUtil {
    static ByteArrayOutputStream out = new ByteArrayOutputStream();

    public synchronized  static byte[] ser(Object msg){
        out.reset();
        ObjectOutputStream oout = null;
        byte[] msgBody = null;
        try {
            oout = new ObjectOutputStream(out);
            oout.writeObject(msg);
            msgBody= out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msgBody;


    }

}
