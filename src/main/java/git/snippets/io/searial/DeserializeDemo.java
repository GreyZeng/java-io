package git.snippets.io.searial;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2021/11/21
 * @since
 */
public class DeserializeDemo {
    public static void main(String[] args) throws Exception {


        FileInputStream fileIn = new FileInputStream("C:\\git\\sample\\employee.ser");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Employee e = (Employee) in.readObject();
        in.close();
        fileIn.close();

        System.out.println("Deserialized Employee...");
        System.out.println("Name: " + e.name);
        System.out.println("Address: " + e.address);
        System.out.println("SSN: " + e.SSN);
        System.out.println("Number: " + e.number);
    }
}
