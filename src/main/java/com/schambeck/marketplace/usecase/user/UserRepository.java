package com.schambeck.marketplace.usecase.user;

import com.schambeck.marketplace.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findAllByOrderByIdAsc(Pageable pageable);

    Page<User> findAllByFullNameContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
    
    default User findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByExternalId(String externalId);
    
    default User findByUsernameOrThrow(String username) {
        return findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }
    
    default User findByExternalIdOrThrow(String externalId) {
        return findByExternalId(externalId).orElseThrow(() -> new UserNotFoundException(externalId));
    }
    
    boolean existsByUsername(String username);
}
