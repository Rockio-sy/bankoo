package com.example.bankcards.config;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final static Logger logger = LoggerFactory.getLogger(DataSeeder.class);


    @Override
    public void run(String... args) {
        String USERNAME = "superuser";
        String PASSWORD = "1234";
        String FULLNAME = "Super User";
        if (userRepository.findByUsername(USERNAME).isEmpty()) {
            User superUser = new User();
            superUser.setUsername(USERNAME);
            superUser.setFullName(FULLNAME);
            superUser.setPassword(passwordEncoder.encode(PASSWORD));
            superUser.setRole(Role.ADMIN);
            userRepository.save(superUser);
            logger.info("\nSuper user has been seeded with username : [{}], and Password: [{}] \n", USERNAME, PASSWORD);
        }else {
            logger.info("\nSuper user exists with username : [{}], and Password: [{}]\n", USERNAME, PASSWORD);
        }
    }
}
