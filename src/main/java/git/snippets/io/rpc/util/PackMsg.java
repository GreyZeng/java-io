package git.snippets.io.rpc.util;

import git.snippets.io.rpc.protocol.MyContent;
import git.snippets.io.rpc.protocol.MyHeader;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/12/31
 * @since
 */
public class PackMsg {
    public MyContent getContent() {
        return content;
    }

    public void setContent(MyContent content) {
        this.content = content;
    }

    public MyHeader getHeader() {
        return header;
    }

    public void setHeader(MyHeader header) {
        this.header = header;
    }

    MyContent content;
    MyHeader header;

    public PackMsg(MyHeader header, MyContent content) {
        this.header = header;
        this.content = content;
    }
}
