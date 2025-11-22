package com.zyna.dev.ecommerce.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    Page<User> findAllByIsDeletedFalse(Pageable pageable);

    List<User> findAllByIdInAndIsDeletedFalse(Collection<Long> ids);

    List<User> findAllByIdIn(Collection<Long> ids);

    Page<User> findAllByIsDeletedTrue(Pageable pageable);

    Page<User> findDistinctByRoles_CodeIgnoreCaseAndIsDeletedFalse(String roleCode, Pageable pageable);
}
