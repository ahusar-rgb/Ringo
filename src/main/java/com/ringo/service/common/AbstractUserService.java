package com.ringo.service.common;

import com.ringo.auth.AuthenticationService;
import com.ringo.auth.IdProvider;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.exception.InternalException;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.common.AbstractUserMapper;
import com.ringo.model.photo.Photo;
import com.ringo.model.security.Role;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.repository.common.AbstractUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    protected final UserRepository userRepository;
    private final AbstractUserRepository<T> repository;
    private final PasswordEncoder passwordEncoder;
    private final AbstractUserMapper<S, T, R> abstractUserMapper;
    private final PhotoService photoService;
    private final AuthenticationService authenticationService;

    protected abstract void throwIfRequiredFieldsNotFilled(T user);

    protected abstract void throwIfUniqueConstraintsViolated(T user);

    //    This method is called before saving the user to the database.
    //    It can be used to set additional fields.
    protected void prepareForSave(T user) {}
    //    This method is called before deleting the user from the database.
    //    It can be used to delete referenced objects.
    protected void prepareForDelete(T user) {}

    public R save(S dto, Role role) {
        log.info("save: {}, role: {}", dto.getEmail(), role);
        User _user = buildFromDto(dto);
        _user.setCreatedAt(LocalDateTime.now());
        _user.setIsActive(true);

        T user = abstractUserMapper.fromUser(_user);
        abstractUserMapper.partialUpdate(user, dto);

        throwIfUniqueConstraintsViolated(user);
        throwIfRequiredFieldsNotFilled(user);

        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(false);
        user.setEmailVerified(false);
        user.setWithIdProvider(false);
        prepareForSave(user);

        R savedDto = abstractUserMapper.toDto(repository.save(user));
        savedDto.setEmail(_user.getEmail());

        authenticationService.sendVerificationEmail(_user);

        return savedDto;
    }

    public R signUpWithIdProvider(String token, IdProvider idProvider, Role role) {
        T user = abstractUserMapper.fromUser(idProvider.getUserFromToken(token));
        throwIfUniqueConstraintsViolated(user);

        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUsername("user" + System.currentTimeMillis());
        user.setIsActive(false);
        user.setWithIdProvider(true);

        R savedDto = abstractUserMapper.toDto(repository.save(user));

        if (!user.getEmailVerified())
            authenticationService.sendVerificationEmail(user);

        savedDto.setEmail(user.getEmail());
        return savedDto;
    }

    protected T getUserDetails() {
        User user = authenticationService.getCurrentUser();
        return abstractUserMapper.fromUser(user);
    }

    public T getFullUser() {
        User user = authenticationService.getCurrentUser();
        if(user == null)
            throw new UserException("User is not authenticated");

        Optional<T> result = repository.findFullById(user.getId());
        if (result.isEmpty())
            throw new NotFoundException("User is not found");

        return result.get();
    }

    private User buildFromDto(S dto) {
        User user = abstractUserMapper.toEntity(dto);
        user.setId(null);
        throwIfUniqueConstraintsViolated(abstractUserMapper.fromUser(user));

        if (dto.getPassword() == null)
            throw new UserException("Password is required");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return user;
    }

    public R activate() {
        T user = getFullUser();

        log.info("activateParticipant: {}", user.getEmail());

        if (user.getIsActive())
            throw new UserException("User [email: %s] is already active".formatted(user.getEmail()));

        throwIfRequiredFieldsNotFilled(user);
        if (!user.getEmailVerified()) {
            throw new UserException("Email is not verified");
        }

        user.setIsActive(true);
        R dto = abstractUserMapper.toDto(repository.save(user));
        dto.setEmail(user.getEmail());
        return dto;
    }

    public R partialUpdate(S dto) {
        T user = getFullUser();
        abstractUserMapper.partialUpdate(user, dto);

        throwIfUniqueConstraintsViolated(user);
        throwIfRequiredFieldsNotFilled(user);

        user.setUpdatedAt(LocalDateTime.now());
        prepareForSave(user);

        R responseDto = abstractUserMapper.toDto(repository.save(user));
        responseDto.setEmail(user.getEmail());
        return responseDto;
    }

    public void delete() {
        T user = getFullUser();
        prepareForDelete(user);
        repository.delete(getFullUser());
    }

    public R setPhoto(MultipartFile photo) {
        T user = getFullUser();

        log.info("email: {}, setPhoto: {}", user.getEmail(), photo.getOriginalFilename());

        if (photo.getContentType() == null)
            throw new UserException("Photo is not valid");
        String contentType = photo.getContentType().split("/")[1];

        try {
            if (user.getProfilePicture() != null)
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
        T user = getFullUser();

        if (user.getProfilePicture() == null)
            throw new UserException("User does not have a photo");

        long photoId = user.getProfilePicture().getId();
        user.setProfilePicture(null);
        repository.save(user);
        photoService.delete(photoId);

        R dto = abstractUserMapper.toDto(user);
        dto.setEmail(user.getEmail());
        return dto;
    }
}
