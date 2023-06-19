package com.ringo.service.security;

import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.exception.InternalException;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.UserMapper;
import com.ringo.model.photo.Photo;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PhotoService photoService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User [email: %s] not found".formatted(email))
        );
    }

    public UserResponseDto findById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User#" + id + " not found")));
    }

    public void delete() {
        User user = getCurrentUserIfActive();
        removePhoto();
        userRepository.delete(user);
    }

    public UserResponseDto partialUpdate(UserRequestDto userRequestDto) {
        User user = getCurrentUserIfActive();

        userMapper.partialUpdate(user, userRequestDto);
        return userMapper.toDto(userRepository.save(user));
    }

    public User getCurrentUserIfActive() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getIsActive())
            throw new UserException("User is not active");
        return user;
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void setPhoto(MultipartFile photo) {
        User user = getCurrentUserIfActive();

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

        userRepository.save(user);
    }

    public void removePhoto() {
        User user = getCurrentUserIfActive();

        if(user.getProfilePicture() != null) {
            long photoId = user.getProfilePicture().getId();
            user.setProfilePicture(null);
            userRepository.save(user);
            photoService.delete(photoId);
        }
    }
}
