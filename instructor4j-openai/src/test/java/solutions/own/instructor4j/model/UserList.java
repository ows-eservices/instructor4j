package solutions.own.instructor4j.model;

import java.util.List;
import javax.validation.constraints.NotNull;
import solutions.own.instructor4j.annotation.Description;

public class UserList {
    @Description("The list of users")
    @NotNull
    private List<User> users;

    public UserList() {}

    public UserList(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "UserList{" +
            "users=" + users +
            '}';
    }
}
