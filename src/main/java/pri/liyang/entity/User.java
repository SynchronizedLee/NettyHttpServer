package pri.liyang.entity;

import java.util.Map;

public class User implements Comparable<User> {

    private Integer id;
    private Integer age;
    private String username;
    private String password;
    private String address;
    private Double salary;

    public Integer getId() {
        return id;
    }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public User setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public User setAddress(String address) {
        this.address = address;
        return this;
    }

    public Double getSalary() {
        return salary;
    }

    public User setSalary(Double salary) {
        this.salary = salary;
        return this;
    }

    public static User extractParamFromMap(Map<String, Object> param) {
        User user = new User();

        if (param.get("id") != null) {
            user.setId(Integer.parseInt(param.get("id").toString()));
        }
        if (param.get("age") != null) {
            user.setAge(Integer.parseInt(param.get("age").toString()));
        }
        if (param.get("username") != null) {
            user.setUsername(param.get("username").toString());
        }
        if (param.get("password") != null) {
            user.setPassword(param.get("password").toString());
        }
        if (param.get("address") != null) {
            user.setAddress(param.get("address").toString());
        }
        if (param.get("salary") != null) {
            user.setSalary(Double.parseDouble(param.get("salary").toString()));
        }

        return user;
    }

    @Override
    public int compareTo(User o) {
        return this.id - o.id;
    }

}
