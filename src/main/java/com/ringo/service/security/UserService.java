package com.ringo.service.security;

import com.ringo.exception.NotFoundException;
import com.ringo.repository.company.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findVerifiedByEmail(email).orElseThrow(
                () -> new NotFoundException("User [email: %s] not found".formatted(email))
        );
    }
}
