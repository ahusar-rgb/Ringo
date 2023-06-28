package com.ringo.service.common;

import com.ringo.auth.IdProvider;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.UserMapper;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public abstract class AbstractUserService<S extends UserRequestDto, T extends User, R extends UserResponseDto> {

    private final UserRepository userRepository;
    private final JpaRepository<T, Long> repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public abstract R createFromUser(S dto, User user);
    public abstract R partialUpdate(S dto);
    public abstract void throwIfNotFullyFilled(T user);

    public R create(S dto) {
        User user = buildFromDto(dto);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        return createFromUser(dto, user);
    }

    protected T signUpWithIdProvider(String token, IdProvider idProvider) {
        User user = idProvider.getUserFromToken(token);
        if(userRepository.findByEmail(user.getEmail()).isPresent())
            throw new UserException("User with email %s already exists".formatted(user.getEmail()));
        return (T)userRepository.save(user);
    }

    public T getCurrentUserIfActive() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getIsActive())
            throw new UserException("User is not active");
        return repository.findById(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not aan organisation")
        );
    }

    public T getCurrentUser() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return repository.findById(user.getId()).orElseThrow(
                () -> new UserException("The authorized user is not aan organisation")
        );
    }

    private User buildFromDto(S dto) {
        if(userRepository.findByEmail(dto.getEmail()).isPresent())
            throw new UserException("User with email %s already exists".formatted(dto.getEmail()));
        if(userRepository.findByUsername(dto.getUsername()).isPresent())
            throw new UserException("User with username %s already exists".formatted(dto.getUsername()));
        if(dto.getPassword() == null)
            throw new UserException("Password is required");

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsActive(false);
        return user;
    }

    public void delete() {
        repository.delete(getCurrentUser());
    }
}
