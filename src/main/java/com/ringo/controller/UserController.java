package com.ringo.controller;

import com.ringo.dto.auth.ChangePasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.company.UserResponseDto;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping(value ="/{id}", produces = {"application/json"})
    public ResponseEntity<UserResponseDto> findById(@PathVariable("id") Long id) {
        return ResponseEntity
                .ok(userService.findById(id));
    }

    @DeleteMapping(produces = {"application/json"})
    public ResponseEntity<String> delete() {
        userService.delete();
        return ResponseEntity
                .ok("User deleted successfully");
    }

    @PutMapping(consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<String> update(@RequestBody UserRequestDto userRequestDto) {
        userService.partialUpdate(userRequestDto);
        return ResponseEntity.ok("User updated successfully");
    }

    @PutMapping(value = "change-password", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<String> updatePassword(@RequestBody ChangePasswordForm changePasswordForm) {
        userService.updatePassword(changePasswordForm);
        return ResponseEntity.ok("User password updated successfully");
    }

    @PutMapping(value = "/set-photo", consumes = {"multipart/form-data"}, produces = {"application/json"})
    public ResponseEntity<String> setPhoto(@RequestPart("file") MultipartFile photo) {
        userService.setPhoto(photo);
        return ResponseEntity.ok("User photo added successfully");
    }

    @PutMapping(value ="/remove-photo", produces = {"application/json"})
    public ResponseEntity<String> removePhoto() {
        userService.removePhoto();
        return ResponseEntity.ok("User photo removed successfully");
    }
}
