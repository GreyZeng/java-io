package git.snippets.io.rpc.protocol;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since
 */
public class MyHeader implements Serializable {
    // 通信协议
    // 1. 协议值
    // 2. UUID
    // 3. DATA_LEN
    int flag;
    long requestID;
    int dataLen;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public static MyHeader createHeader(byte[] msg) {
        MyHeader header = new MyHeader();
        int size = msg.length;
        int f = 0x14141414;
        long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        //0x14  0001 0100
        header.setFlag(f);
        header.setDataLen(size);
        header.setRequestID(requestID);
        return header;

    }
}
