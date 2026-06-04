package com.login.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

@Autowired
public EmailServiceImpl emailServiceImpl;

    @Autowired
    public MyRepository repo;

     Logger logger = LoggerFactory.getLogger(OAuthAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // You can log or redirect after successful login

DefaultOAuth2User user = (DefaultOAuth2User)authentication.getPrincipal();
  
String email = user.getAttribute("email").toString();
String name = user.getAttribute("given_name").toString();
String name2 = user.getAttribute("family_name").toString();
String picture = user.getAttribute("picture").toString();
Login existing = repo.findByEmail(email);
    if (existing == null) {
        Login laptop = new Login();
        laptop.setFname(name);
        laptop.setEmail(email);
        laptop.setLname(name2);

        try {
            // Open input stream from URL and read all bytes
            InputStream in = new URL(picture).openStream();
            byte[] imageBytes = in.readAllBytes();  // Java 11+ feature
            laptop.setImage(imageBytes);       // Save as BLOB
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        repo.save(laptop);
          existing = laptop;
    }
    

   HttpSession session = request.getSession();
        session.setAttribute("username", existing.getFname() + " " + existing.getLname());
        session.setAttribute("useremail", existing.getEmail());
        session.setAttribute("userimage", existing.getImage());
    emailServiceImpl.sendEmail(existing.getEmail(),"google login","somebody  has login with this email");
        logger.info("aloo khaoge");

        // Example: redirect to homepage
 new DefaultRedirectStrategy().sendRedirect(request,response,"/ab");
    }
}

