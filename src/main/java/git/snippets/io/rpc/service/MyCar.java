package git.snippets.io.rpc.service;

public class MyCar implements Car {

    @Override
    public String run(String msg) {
        System.out.println("server,get client arg:" + msg);
        return "server res " + msg;
    }

    @Override
    public Person ofPerson(String name, int age) {
        Person person = new Person();
        person.setAge(age);
        person.setName(name);
        return person;
    }

}
