package git.snippets.io.rpc;

import java.io.Serializable;

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
    long dataLen;

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

    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }
}
