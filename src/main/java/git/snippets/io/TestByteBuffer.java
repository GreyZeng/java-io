package git.snippets.io;

import java.nio.ByteBuffer;

public class TestByteBuffer {
  public static void main(String[] args) {
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
