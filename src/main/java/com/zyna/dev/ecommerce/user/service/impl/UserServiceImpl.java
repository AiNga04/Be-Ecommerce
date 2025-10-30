package com.zyna.dev.ecommerce.user.service.impl;

import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.user.User;
import com.zyna.dev.ecommerce.user.UserMapper;
import com.zyna.dev.ecommerce.user.UserRepository;
import com.zyna.dev.ecommerce.user.criteria.UserCriteria;
import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;
import com.zyna.dev.ecommerce.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreateRequest createRequest) {
        log.info("Create user with email={}", createRequest.getEmail());

        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new ApplicationException(
                    HttpStatus.CONFLICT,
                    "Email already in use!"
            );
        }
        User user = userMapper.createToUser(createRequest);
        if (user.getStatus() == null) {
            user.setStatus(Status.PENDING);
        }

        user.setPassword(passwordEncoder.encode(createRequest.getPassword()));

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id " + id + " not found or deleted")
        );
        return userMapper.toUserResponse(user);
    }

    @Override
    public Page<UserResponse> searchUsers(UserCriteria criteria, int page, int size) {
        Page<User> basePage = userRepository.findAllByIsDeletedFalse(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        // Lọc tiếp bằng criteria (filter in-memory)
        Predicate<User> matches = u -> {
            if (criteria.getFirstName() != null &&
                    !u.getFirstName().toLowerCase().contains(criteria.getFirstName().toLowerCase())) return false;

            if (criteria.getLastName() != null &&
                    !u.getLastName().toLowerCase().contains(criteria.getLastName().toLowerCase())) return false;

            if (criteria.getRole() != null &&
                    !u.getRole().name().equalsIgnoreCase(criteria.getRole())) return false;

            if (criteria.getPhone() != null &&
                    (u.getPhone() == null || !u.getPhone().contains(criteria.getPhone()))) return false;

            if (criteria.getDateOfBirth() != null &&
                    (u.getDateOfBirth() == null || !u.getDateOfBirth().isEqual(criteria.getDateOfBirth()))) return false;

            if (criteria.getGender() != null &&
                    (u.getGender() == null || u.getGender() != criteria.getGender())) return false;

            if (criteria.getCity() != null &&
                    (u.getCity() == null || u.getCity() != criteria.getCity())) return false;

            return true;
        };

        var filters = basePage.getContent().stream()
                .filter(matches)
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(
                filters, basePage.getPageable(), filters.size()
        );
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        log.info("Update user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + id + " not found!"
                ));

        if (user.isDeleted()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "User is deleted. Restore before updating!"
            );
        }

        userMapper.applyUpdate(user, updateRequest);

        user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));

        User saved = userRepository.save(user);

        log.info("User updated id={}", saved.getId());
        return userMapper.toUserResponse(saved);
    }

    @Override
    public void softDeleteUser(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id " + id + " not found or already deleted")
        );
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Soft deleted user id={}", id);
    }

    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + id + " not found"
                ));

        if (!user.isDeleted()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "User is not deleted"
            );
        }

        user.setDeleted(false);
        user.setDeletedAt(null);

        userRepository.save(user);
        log.info("Restored user id={}", id);
    }

    @Override
    public void hardDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + id + " not found!"
                ));

        userRepository.delete(user);
        log.info("Hard deleted user id={}", id);
    }
}
