package com.zyna.dev.ecommerce.users.service.impl;

import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.UserMapper;
import com.zyna.dev.ecommerce.users.UserRepository;
import com.zyna.dev.ecommerce.users.criteria.UserCriteria;
import com.zyna.dev.ecommerce.users.dto.request.UserBatchCreateRequest;
import com.zyna.dev.ecommerce.users.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.users.dto.response.UserBatchCreateResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import com.zyna.dev.ecommerce.users.dto.request.UserUpdateRequest;
import com.zyna.dev.ecommerce.users.service.interfaces.UserService;
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
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppRoleRepository appRoleRepository;

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

        var userRole = appRoleRepository.findByCode("USER")
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Default role USER is not configured!"
                ));
        user.getRoles().add(userRole);

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

            if (criteria.getRole() != null) {
                String roleCode = criteria.getRole();
                if (u.getRoles() == null ||
                        u.getRoles().stream().noneMatch(r -> r.getCode().equalsIgnoreCase(roleCode))) {
                    return false;
                }
            }


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

        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

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

    @Override
    public List<Long> softDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // chỉ lấy user chưa xóa
        List<User> users = userRepository.findAllByIdInAndIsDeletedFalse(ids);

        LocalDateTime now = LocalDateTime.now();
        users.forEach(u -> {
            u.setDeleted(true);
            u.setDeletedAt(now);
        });

        userRepository.saveAll(users);

        List<Long> processedIds = users.stream()
                .map(User::getId)
                .toList();

        log.info("Soft deleted {} users: {}", processedIds.size(), processedIds);
        return processedIds;
    }

    @Override
    public List<Long> restoreUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // lấy all user để kiểm tra trạng thái
        List<User> users = userRepository.findAllByIdIn(ids);

        List<User> toRestore = users.stream()
                .filter(User::isDeleted)
                .toList();

        toRestore.forEach(u -> {
            u.setDeleted(false);
            u.setDeletedAt(null);
        });

        userRepository.saveAll(toRestore);

        List<Long> restoredIds = toRestore.stream()
                .map(User::getId)
                .toList();

        log.info("Restored {} users: {}", restoredIds.size(), restoredIds);
        return restoredIds;
    }

    @Override
    public List<Long> hardDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userRepository.findAllByIdIn(ids);

        userRepository.deleteAll(users);

        List<Long> deletedIds = users.stream()
                .map(User::getId)
                .toList();

        log.info("Hard deleted {} users: {}", deletedIds.size(), deletedIds);
        return deletedIds;
    }

    @Override
    public Page<UserResponse> getDeletedUsers(UserCriteria criteria, int page, int size) {
        Page<User> basePage = userRepository.findAllByIsDeletedTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        Predicate<User> matches = u -> {
            if (criteria.getFirstName() != null && !u.getFirstName().toLowerCase().contains(criteria.getFirstName().toLowerCase()))
                return false;
            if (criteria.getLastName() != null && !u.getLastName().toLowerCase().contains(criteria.getLastName().toLowerCase()))
                return false;
            if (criteria.getRole() != null) {
                String roleCode = criteria.getRole();
                if (u.getRoles() == null ||
                        u.getRoles().stream()
                                .noneMatch(r -> r.getCode().equalsIgnoreCase(roleCode))) {
                    return false;
                }
            }
            if (criteria.getPhone() != null && (u.getPhone() == null || !u.getPhone().contains(criteria.getPhone())))
                return false;
            if (criteria.getDateOfBirth() != null && (u.getDateOfBirth() == null || !u.getDateOfBirth().isEqual(criteria.getDateOfBirth())))
                return false;
            if (criteria.getGender() != null && (u.getGender() == null || u.getGender() != criteria.getGender()))
                return false;
            if (criteria.getCity() != null && (u.getCity() == null || u.getCity() != criteria.getCity()))
                return false;
            return true;
        };

        var filtered = basePage.getContent().stream()
                .filter(matches)
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(filtered, basePage.getPageable(), basePage.getTotalElements());
    }

    @Override
    public UserBatchCreateResponse createUsers(UserBatchCreateRequest request) {
        var createdList = new java.util.ArrayList<UserResponse>();
        var failedList = new java.util.ArrayList<UserBatchCreateResponse.FailedUser>();

        // thời điểm tạo
        var now = java.time.LocalDateTime.now();

        request.getUsers().forEach(item -> {
            try {
                // check email trùng giống createUser()
                if (userRepository.existsByEmail(item.getEmail())) {
                    failedList.add(
                            UserBatchCreateResponse.FailedUser.builder()
                                    .email(item.getEmail())
                                    .reason("Email already in use!")
                                    .build()
                    );
                    return;
                }

                // map dto -> entity
                User user = userMapper.createToUser(item);  // :contentReference[oaicite:10]{index=10}

                // set status nếu null
                if (user.getStatus() == null) {
                    user.setStatus(com.zyna.dev.ecommerce.common.enums.Status.PENDING);
                }

                // encode password giống createUser()
                user.setPassword(passwordEncoder.encode(item.getPassword()));

                // set các field audit nếu cần (tùy bạn, vì @CreationTimestamp tự set)
                user.setCreatedAt(now);

                User saved = userRepository.save(user);

                createdList.add(userMapper.toUserResponse(saved)); // :contentReference[oaicite:11]{index=11}
            } catch (Exception e) {
                // nếu có lỗi khác (ví dụ enum city sai) thì cũng add vào failed
                failedList.add(
                        UserBatchCreateResponse.FailedUser.builder()
                                .email(item.getEmail())
                                .reason(e.getMessage())
                                .build()
                );
                log.error("Failed to create user email={}", item.getEmail(), e);
            }
        });

        log.info("Batch create users: success={}, failed={}", createdList.size(), failedList.size());

        return UserBatchCreateResponse.builder()
                .created(createdList)
                .failed(failedList)
                .build();
    }


}
