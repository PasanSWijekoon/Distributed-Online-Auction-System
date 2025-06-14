package com.auction.web.filter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/index.html", "/auction-servlet"})
public class AuthCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        boolean isLoggedIn = (session != null && session.getAttribute("loggedInUserId") != null);
        String action = httpRequest.getParameter("action");

        if (requestURI.endsWith("login.html") ||
                requestURI.endsWith("register.html") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/images/") ||
                (requestURI.endsWith("user-servlet") && ("login".equals(action) || "register".equals(action))) ) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (requestURI.endsWith("auction-servlet") && ("listActive".equals(action) || "listFinished".equals(action))) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }


        if (isLoggedIn) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            System.out.println("AuthCheckFilter: User not logged in. Redirecting to login.html from " + requestURI);
            httpResponse.sendRedirect(contextPath + "/login.html");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}