package com.ringo;

import com.ringo.auth.AuthenticationProperties;
import com.ringo.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthenticationProperties.class, ApplicationProperties.class})
public class RingoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RingoApplication.class, args);
    }

}
