package com.tripnest.tripnest_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:tripnest.travel.app@gmail.com}")
    private String username;

    @Value("${spring.mail.password:htrnxsexmybqcdla}")
    private String password;

    @Bean(name = "mailSender")
    @org.springframework.context.annotation.Primary
    public JavaMailSender mailSender() {


        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host != null ? host.trim() : "smtp.gmail.com");
        mailSender.setPort(port > 0 ? port : 587);

        // Sanitize username and password (strip all spaces and quotes)
        String cleanUsername = "tripnest.travel.app@gmail.com";
        String cleanPassword = "htrnxsexmybqcdla";

        if (password != null && !password.trim().isEmpty()) {
            String p = password.replaceAll("\\s+", "").replaceAll("^\"|\"$", "");
            if (p.length() == 16) {
                cleanPassword = p;
            }
        }

        mailSender.setUsername(cleanUsername);
        mailSender.setPassword(cleanPassword);



        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");


        return mailSender;
    }
}
