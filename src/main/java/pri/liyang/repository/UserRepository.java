package pri.liyang.repository;

import pri.liyang.entity.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRepository {

    private static List<User> userList = new ArrayList<>();
    private static AtomicInteger idSeq = new AtomicInteger(1);

    static {
        userList.add(new User().setId(idSeq.getAndIncrement()).setAge(56).setUsername("develop").setPassword("123456").setAddress("chengdu").setSalary(16000.00));
        userList.add(new User().setId(idSeq.getAndIncrement()).setAge(59).setUsername("product").setPassword("234567").setAddress("chengdu").setSalary(12000.00));
        userList.add(new User().setId(idSeq.getAndIncrement()).setAge(57).setUsername("test").setPassword("345678").setAddress("chengdu").setSalary(13000.00));
    }

    public static List<User> getAllUser() {
        Collections.sort(userList);
        return userList;
    }

    public static User getUserById(Integer id) {
        for (User user : userList) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public static void addUser(User user) {
        user.setId(idSeq.getAndIncrement());
        userList.add(user);
    }

    public static void updateUser(User user) {
        boolean find = false;
        for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
            User curUser = iterator.next();
            if (curUser.getId().equals(user.getId())) {
                iterator.remove();
                find = true;
                break;
            }
        }

        if (find) {
            userList.add(user);
        }
    }

    public static void deleteUser(Integer id) {
        for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
            User curUser = iterator.next();
            if (curUser.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

}
