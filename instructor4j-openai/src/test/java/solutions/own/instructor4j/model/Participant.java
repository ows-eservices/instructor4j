package solutions.own.instructor4j.model;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import solutions.own.instructor4j.annotation.Description;

public class Participant {

    @Description("The name of the user")
    @NotNull
    private String name;

    @Description("The email of the user")
    @NotNull
    private String email;

    @Description("The twitter handle of the user")
    @NotNull
    private String handle;

    public Participant() {
    }

    public Participant(String name, String email, String handle) {
        this.name = name;
        this.email = email;
        this.handle = handle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", handle='" + handle + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Participant that = (Participant) o;
        return Objects.equals(name, that.name) && Objects.equals(email, that.email)
            && Objects.equals(handle, that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, handle);
    }
}
