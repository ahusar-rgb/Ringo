package com.ringo.service.common;

import com.ringo.auth.IdProvider;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.exception.InternalException;
import com.ringo.exception.UserException;
import com.ringo.mapper.common.AbstractUserMapper;
import com.ringo.model.photo.Photo;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.repository.common.AbstractUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Transactional
@RequiredArgsConstructor
public abstract class AbstractUserService<S extends UserRequestDto, T extends User, R extends UserResponseDto> {

    private final UserRepository userRepository;
    private final AbstractUserRepository<T> repository;
    private final PasswordEncoder passwordEncoder;
    private final AbstractUserMapper<S, T, R> abstractUserMapper;
    private final PhotoService photoService;

    protected abstract void throwIfNotFullyFilled(T user);
    protected abstract void throwIfUniqueConstraintsViolated(T user);

    public R save(S dto, Role role) {
        log.info("save: {}, role: {}", dto.getEmail(), role);
        User _user = buildFromDto(dto);
        _user.setCreatedAt(LocalDateTime.now());
        _user.setIsActive(true);

        T user = abstractUserMapper.fromUser(_user);
        abstractUserMapper.partialUpdate(user, dto);

        throwIfUniqueConstraintsViolated(user);
        throwIfNotFullyFilled(user);

        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        R savedDto = abstractUserMapper.toDto(repository.save(user));
        savedDto.setEmail(_user.getEmail());

        return savedDto;
    }

    protected R signUpWithIdProvider(String token, IdProvider idProvider, Role role) {
        T user = abstractUserMapper.fromUser(idProvider.getUserFromToken(token));
        throwIfUniqueConstraintsViolated(user);

        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(false);

        R saved = abstractUserMapper.toDto(repository.save(user));
        saved.setEmail(user.getEmail());
        return saved;
    }

    protected T getUserDetails() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return abstractUserMapper.fromUser(user);
    }

    public T getFullUser() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<T> result = repository.findFullById(user.getId());
        if(result.isEmpty())
            throw new UserException("User not found");

        return result.get();
    }

    private User buildFromDto(S dto) {
        User user = abstractUserMapper.toEntity(dto);
        throwIfUniqueConstraintsViolated(abstractUserMapper.fromUser(user));

        if(dto.getPassword() == null)
            throw new UserException("Password is required");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return user;
    }

    public R activate() {
        T user = getUserDetails();

        log.info("activateParticipant: {}", user.getEmail());

        if(user.getIsActive()) {
            throw new UserException("User [email: %s] is already active".formatted(user.getEmail()));
        }
        throwIfNotFullyFilled(user);

        user.setIsActive(true);
        R dto = abstractUserMapper.toDto(repository.save(user));
        dto.setEmail(user.getEmail());
        return dto;
    }

    public R partialUpdate(S dto) {
        T user = getUserDetails();
        abstractUserMapper.partialUpdate(user, dto);

        throwIfUniqueConstraintsViolated(user);
        throwIfNotFullyFilled(user);

        user.setUpdatedAt(LocalDateTime.now());

        R responseDto = abstractUserMapper.toDto(repository.save(user));
        responseDto.setEmail(user.getEmail());
        return responseDto;
    }

    public void delete() {
        repository.delete(getUserDetails());
    }

    public R setPhoto(MultipartFile photo) {
        T user = getUserDetails();

        log.info("email: {}, setPhoto: {}", user.getEmail(), photo.getOriginalFilename());

        if(photo.getContentType() == null)
            throw new UserException("Photo is not valid");
        String contentType = photo.getContentType().split("/")[1];

        try {
            removePhoto();
            Photo profilePicture = photoService.save("profilePictures/user#" + user.getId(), contentType, photo.getBytes());
            user.setProfilePicture(profilePicture);
        } catch (IOException e) {
            throw new InternalException("Error while saving photo");
        }

        T saved = repository.save(user);
        R dto = abstractUserMapper.toDto(saved);
        dto.setEmail(user.getEmail());

        return dto;
    }

    public R removePhoto() {
        User user = getUserDetails();

        if (user.getProfilePicture() != null) {
            long photoId = user.getProfilePicture().getId();
            user.setProfilePicture(null);
            userRepository.save(user);
            photoService.delete(photoId);
        }

        R dto = abstractUserMapper.toDto(getUserDetails());
        dto.setEmail(user.getEmail());
        return dto;
    }
}