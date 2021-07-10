package com.dryadandnaiad.sethlans.bootstrap;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDataLoader implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count() == 0) {
            loadDefaultUsers();
        }
        
    }

    private void loadDefaultUsers() {

        userRepository.save(User.builder().username("admin")
                .password(passwordEncoder.encode("test1234")).roles(Sets.newHashSet(Role.SUPER_ADMINISTRATOR)).userID("1234125123").active(true).build());

        log.debug("Users Loaded: " + userRepository.count());
    }
}
