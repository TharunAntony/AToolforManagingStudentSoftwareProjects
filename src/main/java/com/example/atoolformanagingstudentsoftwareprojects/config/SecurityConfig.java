package com.example.atoolformanagingstudentsoftwareprojects.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Autowired
    private AuthenticationHandler authenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        //allow login page and static files
                        .requestMatchers("/home","/login","/register", "/images/**", "/css/**").permitAll()
                        //everything else requires login
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form

                        //Tells spring to use custom login page
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler)
                        .permitAll()

                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout").permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable);


        return http.build();

    }


    //Used to encrypt password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}

