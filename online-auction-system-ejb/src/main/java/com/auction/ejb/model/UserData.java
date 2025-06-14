package com.auction.ejb.model;
import java.io.Serializable;
import java.util.Objects;

public class UserData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String password;
    private String email;

    public UserData(String userId, String password, String email) {
        this.userId = userId;
        this.password = password;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(userId, userData.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}