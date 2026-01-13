package Nuvola.Projekatsiit2025.model;

import lombok.Data;

import java.util.List;

@Data
public abstract class User {
    protected Long id;
    protected String email;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String address;
    protected String phone;
    protected String picture;
    protected boolean isBlocked;
    protected String blockingReason;
    protected List<Notification> notifications;
    protected Chat chat;

    public User() {
    }

    public User(Long id, String email, String password, String firstName,
                String lastName, String address, String phone, String picture) {
        super();
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.picture = picture;
    }


}
