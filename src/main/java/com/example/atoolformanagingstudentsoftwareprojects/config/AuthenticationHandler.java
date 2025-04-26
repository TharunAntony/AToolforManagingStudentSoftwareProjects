package com.example.atoolformanagingstudentsoftwareprojects.config;

import com.example.atoolformanagingstudentsoftwareprojects.model.Role;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;

import java.io.IOException;

@Component
public class AuthenticationHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CurrentUser user = (CurrentUser) authentication.getPrincipal();
        Role role = user.getUser().getRole();

        if (role == Role.STUDENT) {
            response.sendRedirect("/student/home");
        } else if (role == Role.CONVENOR) {
            response.sendRedirect("/convenor/home");
        } else {
            response.sendRedirect("/login?error=true");
        }
    }

}
