package com.careerpilot.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careerpilot.auth.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>
{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
