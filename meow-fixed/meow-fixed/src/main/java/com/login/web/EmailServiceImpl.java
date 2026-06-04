package com.login.web;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailServiceImpl implements EmailService{


@Autowired
private JavaMailSender mailSender;


private Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);




    @Override
    public void sendEmail(String to, String subject, String message) {
SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
simpleMailMessage.setTo(to);
simpleMailMessage.setSubject(subject);
simpleMailMessage.setText(message);
simpleMailMessage.setFrom("brayw433@gmail.com");
mailSender.send(simpleMailMessage);
logger.info("email has been sent");

    }

    @Override
    public void sendEmail(String[] to, String subject, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmail'");
    }

    @Override
    public void sendEmailWithHtml(String to, String subject, String htmlContent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailWithHtml'");
    }

    @Override
    public void sendEmailWithFile(String to, String subject, String message, File file) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailWithFile'");
    }

}

