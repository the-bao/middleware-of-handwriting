package io.github;

import io.github.annotation.Table;

/**
 * @author rty
 * @version 1.0
 * @description:
 * @date 2025/8/23 16:29
 */
@Table(tableName = "user")
public class User {
    public Integer id;
    public String name;
    public Integer age;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
