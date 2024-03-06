package com.schambeck.marketplace.usecase.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;

    @Transactional
    public User create(User entity) {
        return repository.save(entity);
    }

    @Transactional
    public User update(int id, User entity) {
        User toUpdate = findById(id);
        UserMapper.INSTANCE.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    @Transactional
    public void remove(int id) {
        repository.deleteById(id);
    }

    public User findById(int id) {
        return repository.findByIdOrThrow(id);
    }

    public Page<User> search(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByFullNameContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }
}
