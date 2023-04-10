package com.ringo.service.security;

import com.ringo.dto.security.UserRequestDto;
import com.ringo.dto.security.UserResponseDto;
import com.ringo.exception.IllegalInsertException;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.UserMapper;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException("User [username: %s] not found".formatted(username))
        );
    }

    public UserResponseDto saveUser(UserRequestDto userRequestDto) {
        if(userRepository.findByUsername(userRequestDto.getUsername()).isPresent())
            throw new IllegalInsertException("User with [username: %s] already exists".formatted(userRequestDto.getUsername()));
        if(userRepository.findByEmail(userRequestDto.getEmail()).isPresent())
            throw new IllegalInsertException("User with [email: %s] already exists".formatted(userRequestDto.getEmail()));

        User user = userMapper.toEntity(userRequestDto);
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRole(Role.ROLE_ORGANISATION);
        user.setIsActive(true);

        return userMapper.toDto(userRepository.save(user));
    }
}
