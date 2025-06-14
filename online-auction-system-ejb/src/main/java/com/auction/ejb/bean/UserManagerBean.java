package com.auction.ejb.bean;

import com.auction.ejb.model.UserData;
import com.auction.ejb.remote.UserManager;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Startup
public class UserManagerBean implements UserManager {
    private final Map<String, UserData> registeredUsers = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeTestUsers() {
        System.out.println("UserManagerBean: Initializing test users...");

        for (int i = 1; i <= 50; i++) {
            String userId = "testuser" + i;
            String password = "password" + i;
            String email = "testuser" + i + "@example.com";

            if (!registeredUsers.containsKey(userId)) {
                registeredUsers.put(userId, new UserData(userId, password, email));
                System.out.println("UserManagerBean: Created test user - ID: " + userId + ", Email: " + email);
            }
        }
        System.out.println("UserManagerBean: Test user initialization complete. Total users: " + registeredUsers.size());
    }

    @Override
    @Lock(LockType.WRITE)
    public boolean registerUser(String userId, String password, String email) {
        if (userId == null || userId.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("UserManagerBean: Registration failed - User ID or password empty.");
            return false;
        }
        if (registeredUsers.containsKey(userId)) {
            System.err.println("UserManagerBean: Registration failed - User ID '" + userId + "' already exists.");
            return false;
        }
        registeredUsers.put(userId, new UserData(userId, password, email));
        System.out.println("UserManagerBean: User registered successfully: " + userId);
        return true;
    }

    @Override
    @Lock(LockType.READ)
    public UserData authenticateUser(String userId, String password) {
        UserData user = registeredUsers.get(userId);
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("UserManagerBean: User authenticated: " + userId);
            return user;
        }
        System.out.println("UserManagerBean: Authentication failed for user: " + userId);
        return null;
    }

    @Override
    @Lock(LockType.READ)
    public UserData findUser(String userId) {
        return registeredUsers.get(userId);
    }
}