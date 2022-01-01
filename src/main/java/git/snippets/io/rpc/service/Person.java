package git.snippets.io.rpc.service;

import java.io.Serializable;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2022/1/1
 * @since
 */
public class Person implements Serializable {
    private String name;
    private int age;

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
