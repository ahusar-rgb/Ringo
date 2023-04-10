package com.ringo;

import com.ringo.auth.AuthenticationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthenticationProperties.class})
public class RingoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RingoApplication.class, args);
    }

}
