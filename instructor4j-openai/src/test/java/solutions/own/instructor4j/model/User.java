package solutions.own.instructor4j.model;

import solutions.own.instructor4j.annotation.Description;

public class User {
    @Description("The age of the user on the current date")
    private int age;

    @Description("The name of the user")
    private String name;

    public User() {
    }

    public User(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
            "age=" + age +
            ", name='" + name + '\'' +
            '}';
    }
}
