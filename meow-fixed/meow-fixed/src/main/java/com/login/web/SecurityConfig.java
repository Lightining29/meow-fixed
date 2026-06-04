package com.login.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {





 @Autowired
    private OAuthAuthenticationSuccessHandler successHandler;

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {

        http
        .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "bot1.png", "bot2.png", "bot3.png", "bot4.png", "/bn.webp", "meal.png", "momos.png", "chinese.png", "south.png", "italian.png", "beaverage.png", "snack.png", "dessert.png", "veg.png", "chef.png", "/deleiveryman", "chef.png", "chef2.png", "chef3.png", "/pngwing.com.png",
                        "/kholo", "/prof", "/bot.png", "/bur.png", "/avatar.png", "/chaap.jpg", "/paneer.jpg", "/noodl.jpg", "/naan.jpg", "/pizza.jpg", "/rice.jpg", "/roll.jpg",
                        "/extra/{id}", "/foodlist/{id}", "/inex.css", "/img/{id}", "/cate", "/login",
                        "/save", "/verify-otp", "/lg", "/index", "/", "/search",       // registration, login & public pages
                        "/admin", "/approve", "/reject", "/admin/stats",             // admin dashboard
                        "/ai/**", "/discount/**", "/qr/**"                              // public API endpoints
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .successHandler(successHandler)  // Set your custom success handler
            );

        return http.build();
    }


}
