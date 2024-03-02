package com.schambeck.marketplace.usecase.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findAllByOrderByIdAsc(Pageable pageable);

    Page<User> findAllByFullNameContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
}
