import java.io.*;
import java.net.Socket;

/**
 * Socket Client
 */
public class SocketClientTest {

    public static void main(String[] args) {

        try {
            Socket client = new Socket("192.168.205.138", 9090);
            OutputStream out = client.getOutputStream();
            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    byte[] bb = line.getBytes();
                    for (byte b : bb) {
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
