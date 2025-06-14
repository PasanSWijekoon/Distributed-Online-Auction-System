package com.auction.ejb.remote;

import com.auction.ejb.model.UserData;
import jakarta.ejb.Remote;

@Remote
public interface UserManager {
    boolean registerUser(String userId, String password, String email);
    UserData authenticateUser(String userId, String password);
    UserData findUser(String userId);
}
