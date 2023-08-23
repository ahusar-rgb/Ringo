package com.ringo.repository.common;

import com.ringo.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbstractUserRepository<T extends User> extends JpaRepository<T, Long> {
    Optional<T> findFullById(Long id);

    Optional<T> findFullActiveById(Long id);
}
