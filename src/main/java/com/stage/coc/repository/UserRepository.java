package com.stage.coc.repository;

import com.stage.coc.entity.User;
import com.stage.coc.enums.TypeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByTypeUser(TypeUser typeUser);
    boolean existsByEmail(String email);
}