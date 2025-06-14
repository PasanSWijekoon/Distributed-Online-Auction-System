package com.auction.web.servlet;

import com.auction.ejb.model.UserData;
import com.auction.ejb.remote.UserManager;
import com.auction.web.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;

@WebServlet("/user-servlet")
public class UserServlet extends HttpServlet {

    @EJB
    private UserManager userManager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Object responseObject = null;

        try {
            if ("register".equals(action)) {
                String userId = request.getParameter("userId");
                String email = request.getParameter("email");
                String password = request.getParameter("password");

                if (userId == null || userId.trim().isEmpty() ||
                        password == null || password.trim().isEmpty() ||
                        email == null || email.trim().isEmpty() || !email.contains("@")) {
                    responseObject = Map.of("success", false, "message", "Invalid registration details. All fields are required and email must be valid.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else if (userManager.registerUser(userId, password, email)) {
                    responseObject = Map.of("success", true, "message", "User registered successfully.");
                } else {
                    responseObject = Map.of("success", false, "message", "Registration failed. User ID might already exist or input invalid.");
                }
            } else if ("login".equals(action)) {
                String userId = request.getParameter("userId");
                String password = request.getParameter("password");

                if (userId == null || userId.trim().isEmpty() || password == null || password.isEmpty()) {
                    responseObject = Map.of("success", false, "message", "User ID and password are required.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    UserData user = userManager.authenticateUser(userId, password);

                    if (user != null) {
                        HttpSession httpSession = request.getSession(true);
                        httpSession.setAttribute("loggedInUserId", user.getUserId());
                        httpSession.setAttribute("userEmail", user.getEmail() == null ? "" : user.getEmail());

                        responseObject = Map.of(
                                "success", true,
                                "userId", user.getUserId(),
                                "email", user.getEmail() == null ? "" : user.getEmail(),
                                "message", "Login successful."
                        );
                    } else {
                        responseObject = Map.of("success", false, "message", "Invalid user ID or password.");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            } else if ("logout".equals(action)) {
                HttpSession httpSession = request.getSession(false);
                if (httpSession != null) {
                    httpSession.invalidate();
                }
                responseObject = Map.of("success", true, "message", "Logged out successfully.");
            } else {
                responseObject = Map.of("success", false, "message", "Invalid action specified.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObject = Map.of("success", false, "message", "An unexpected server error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        out.print(gson.toJson(responseObject));
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Object responseObject;

        if ("checkLoginStatus".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("loggedInUserId") != null) {
                String userId = (String) session.getAttribute("loggedInUserId");
                String userEmail = (String) session.getAttribute("userEmail");
                responseObject = Map.of(
                        "loggedIn", true,
                        "userId", userId,
                        "email", userEmail == null ? "" : userEmail
                );
            } else {
                responseObject = Map.of("loggedIn", false);
            }
        } else {
            responseObject = Map.of("success", false, "message", "Unsupported GET action.");
        }
        out.print(gson.toJson(responseObject));
        out.flush();
    }
}