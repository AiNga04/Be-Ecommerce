package com.zyna.dev.ecommerce.users.service.impl;

import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.authentication.service.AccountActivationService;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserMapper;
import com.zyna.dev.ecommerce.users.UserRepository;
import com.zyna.dev.ecommerce.users.service.UserAuditService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppRoleRepository appRoleRepository;
    private final AccountActivationService accountActivationService;
    private final UserAuditService userAuditService;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest createRequest) {
        log.info("Create user with email={}", createRequest.getEmail());

        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new ApplicationException(
                    HttpStatus.CONFLICT,
                    "Email đã được sử dụng"
            );
        }

        User user = userMapper.createToUser(createRequest);

        if (user.getStatus() == null) {
            user.setStatus(Status.PENDING);
        }

        user.setPassword(passwordEncoder.encode(createRequest.getPassword()));

        // Gán ROLE
        Set<String> rolesToAssign = new HashSet<>();
        if (createRequest.getRoles() != null && !createRequest.getRoles().isEmpty()) {
            rolesToAssign.addAll(createRequest.getRoles());
        }
        if (createRequest.getRole() != null && !createRequest.getRole().isEmpty()) {
            rolesToAssign.add(createRequest.getRole());
        }

        if (!rolesToAssign.isEmpty()) {
            var roles = appRoleRepository.findAllByCodeIn(rolesToAssign);

            if (roles.size() != rolesToAssign.size()) {
                throw new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Một số vai trò không hợp lệ"
                );
            }

            user.setRoles(new HashSet<>(roles));
        } else {
            // fallback: ROLE USER mặc định
            var userRole = appRoleRepository.findByCode("USER")
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Vai trò mặc định USER không tồn tại"
                    ));
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            user.getRoles().add(userRole);
        }

        User saved = userRepository.save(user);
        accountActivationService.sendActivationToken(saved, getCurrentActorEmail(), createRequest.getPassword(), com.zyna.dev.ecommerce.common.enums.ActivationType.ADMIN_CREATE);
        return userMapper.toUserResponse(saved);
    }

    @Override
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với email: " + email
                ))
                .getId();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với id " + id + " hoặc đã bị xóa")
        );
        return userMapper.toUserResponse(user);
    }

    @Override
    public Page<UserResponse> searchUsers(UserCriteria criteria, int page, int size) {
        org.springframework.data.jpa.domain.Specification<User> spec = com.zyna.dev.ecommerce.users.repository.UserSpecification.fromCriteria(criteria, false);
        
        // Ensure sorting by createdAt DESC
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage = userRepository.findAll(spec, pageRequest);

        return userPage.map(userMapper::toUserResponse);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        log.info("Update user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với id " + id + " hoặc đã bị xóa"
                ));

        if (user.isDeleted()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng đã bị xóa. Vui lòng khôi phục trước khi cập nhật"
            );
        }

        // map các field cơ bản
        userMapper.applyUpdate(user, updateRequest);

        // update password nếu có
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        if (updateRequest.getAvatarUrl() != null && !updateRequest.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }

        // UPDATE ROLES (nếu FE gửi lên)
        if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {

            var roles = appRoleRepository.findAllByCodeIn(updateRequest.getRoles());

            if (roles.size() != updateRequest.getRoles().size()) {
                throw new ApplicationException(
                        HttpStatus.BAD_REQUEST,
                        "Một số vai trò không hợp lệ"
                );
            }

            // Quan trọng: thao tác trên collection đã được Hibernate quản lý
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            } else {
                user.getRoles().clear();
            }
            user.getRoles().addAll(roles);
        }

        User saved = userRepository.save(user);

        log.info("User updated id={}, rolesUpdated={}",
                saved.getId(), updateRequest.getRoles() != null);

        return userMapper.toUserResponse(saved);
    }


    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với id " + id + " hoặc đã bị xóa")
        );
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        // Force status to DELETED
        user.setStatus(Status.DELETED);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với id " + id
                ));

        if (!user.isDeleted()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng không ở trạng thái đã xóa"
            );
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        // Force status to PENDING (users must re-verify or admin must re-approve if needed, 
        // but requirement says 'pending' on restore)
        user.setStatus(Status.PENDING);

        User saved = userRepository.save(user);
        
        // Gửi lại email kích hoạt
        accountActivationService.sendActivationToken(saved, getCurrentActorEmail(), com.zyna.dev.ecommerce.common.enums.ActivationType.RESTORE);
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng với id " + id
                ));

        userRepository.delete(user);
    }

    @Override
    @Transactional
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
            u.setStatus(Status.DELETED);
        });

        userRepository.saveAll(users);

        List<Long> processedIds = users.stream()
                .map(User::getId)
                .toList();

        log.info("Soft deleted {} users: {}", processedIds.size(), processedIds);
        return processedIds;
    }

    @Override
    @Transactional
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
            u.setStatus(Status.PENDING);
        });

        userRepository.saveAll(toRestore);

        // Gửi lại email kích hoạt cho từng user
        String actorEmail = getCurrentActorEmail();
        toRestore.forEach(u -> accountActivationService.sendActivationToken(u, actorEmail, com.zyna.dev.ecommerce.common.enums.ActivationType.RESTORE));

        List<Long> restoredIds = toRestore.stream()
                .map(User::getId)
                .toList();

        return restoredIds;
    }

    @Override
    @Transactional
    public List<Long> hardDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userRepository.findAllByIdIn(ids);

        userRepository.deleteAll(users);

        List<Long> deletedIds = users.stream()
                .map(User::getId)
                .toList();

        return deletedIds;
    }

    @Override
    public Page<UserResponse> getDeletedUsers(UserCriteria criteria, int page, int size) {
        org.springframework.data.jpa.domain.Specification<User> spec = com.zyna.dev.ecommerce.users.repository.UserSpecification.fromCriteria(criteria, true);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage = userRepository.findAll(spec, pageRequest);

        return userPage.map(userMapper::toUserResponse);
    }

    @Override
    public UserBatchCreateResponse createUsers(UserBatchCreateRequest request) {
        var createdList = new java.util.ArrayList<UserResponse>();
        var failedList = new java.util.ArrayList<UserBatchCreateResponse.FailedUser>();
        String actorEmail = getCurrentActorEmail();

        // thời điểm tạo
        var now = java.time.LocalDateTime.now();

        // Pre-fetch default USER role to avoid repeated DB calls if possible, 
        // but for batch it might be better to just do it inside loop or cache it.
        // Let's stick to the logic for now.

        request.getUsers().forEach(item -> {
            try {
                // check email trùng giống createUser()
                if (userRepository.existsByEmail(item.getEmail())) {
                    failedList.add(
                            UserBatchCreateResponse.FailedUser.builder()
                                    .email(item.getEmail())
                                    .reason("Email đã được sử dụng")
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

                // Gán ROLE
                Set<String> rolesInput = new HashSet<>();
                if (item.getRoles() != null) rolesInput.addAll(item.getRoles());
                if (item.getRole() != null && !item.getRole().isEmpty()) rolesInput.add(item.getRole());
                
                if (!rolesInput.isEmpty()) {
                    var roles = appRoleRepository.findAllByCodeIn(rolesInput);

                    if (roles.size() != rolesInput.size()) {
                        failedList.add(
                                UserBatchCreateResponse.FailedUser.builder()
                                        .email(item.getEmail())
                                        .reason("Một số vai trò không hợp lệ: " + rolesInput)
                                        .build()
                        );
                        return;
                    }

                    user.setRoles(new HashSet<>(roles));
                } else {
                    // fallback: ROLE USER mặc định
                    var userRole = appRoleRepository.findByCode("USER")
                            .orElseThrow(() -> new ApplicationException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Vai trò USER mặc định chưa được cấu hình"
                            ));
                    if (user.getRoles() == null) {
                       user.setRoles(new HashSet<>());
                    }
                    user.getRoles().add(userRole);
                }

                // set các field audit nếu cần (tùy bạn, vì @CreationTimestamp tự set)
                user.setCreatedAt(now);

                User saved = userRepository.save(user);
                accountActivationService.sendActivationToken(saved, actorEmail, item.getPassword(), com.zyna.dev.ecommerce.common.enums.ActivationType.ADMIN_CREATE);

                createdList.add(userMapper.toUserResponse(saved)); // :contentReference[oaicite:11]{index=11}
            } catch (Exception e) {
                // nếu có lỗi khác (ví dụ enum city sai) thì cũng add vào failed
                failedList.add(
                        UserBatchCreateResponse.FailedUser.builder()
                                .email(item.getEmail())
                                .reason(e.getMessage())
                                .build()
                );
            }
        });

        return UserBatchCreateResponse.builder()
                .created(createdList)
                .failed(failedList)
                .build();
    }

    @Override
    public Page<UserResponse> getShippers(int page, int size) {
        Page<User> basePage = userRepository.findDistinctByRoles_CodeIgnoreCaseAndIsDeletedFalse(
                "SHIPPER",
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<UserResponse> list = basePage.getContent()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        return new PageImpl<>(list, basePage.getPageable(), basePage.getTotalElements());
    }

    @Override
    @Transactional
    public UserResponse updateAvatar(Long userId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cần có tệp hình ảnh");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng hoặc đã bị xóa")
        );

        String newUrl;
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            newUrl = com.zyna.dev.ecommerce.common.utils.FileUploadUtil.saveImage(image);
        } else {
            newUrl = com.zyna.dev.ecommerce.common.utils.FileUploadUtil.replaceImage(user.getAvatarUrl(), image);
        }

        user.setAvatarUrl(newUrl);
        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, com.zyna.dev.ecommerce.common.enums.Status newStatus) {
        // 1. Chỉ cho phép admin chỉnh tay sang ACTIVE hoặc DISABLED
        if (newStatus != Status.ACTIVE && newStatus != Status.DISABLED) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Không được phép cập nhật thủ công sang trạng thái " + newStatus + ". Chỉ ACTIVE hoặc DISABLED có thể được đặt thủ công."
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (user.isDeleted()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Không thể cập nhật trạng thái của người dùng đã xóa. Vui lòng khôi phục trước!");
        }

        Status currentStatus = user.getStatus();

        // 2. Validate State Transitions (Strict State Machine)
        switch (currentStatus) {
            case PENDING -> {
                 // PENDING -> ACTIVE: Blocked (System only via email verification)
                if (newStatus == Status.ACTIVE) {
                    throw new ApplicationException(
                            HttpStatus.BAD_REQUEST,
                            "Không thể kích hoạt thủ công người dùng đang chờ. Người dùng phải xác minh email!"
                    );
                }
                // PENDING -> DISABLED: Allowed (Admin ban)
            }
            case ACTIVE -> {
                // ACTIVE -> DISABLED: Allowed (Admin ban)
                // ACTIVE -> PENDING: Blocked (Invalid transition) - prevented by check #1
            }
            case DISABLED -> {
                // DISABLED -> ACTIVE: Allowed (Admin unban)
                // DISABLED -> PENDING: Blocked (Invalid transition) - prevented by check #1
            }
            case DELETED -> throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Người dùng đã bị xóa. Không thể thay đổi trạng thái thủ công. Sử dụng chức năng khôi phục."
            );
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Trạng thái hiện tại không xác định");
        }

        // Apply update
        user.setStatus(newStatus);

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateAvatarByAdmin(Long targetUserId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(targetUserId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng hoặc đã bị xóa")
        );

        String newUrl;
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            newUrl = com.zyna.dev.ecommerce.common.utils.FileUploadUtil.saveImage(image);
        } else {
            newUrl = com.zyna.dev.ecommerce.common.utils.FileUploadUtil.replaceImage(user.getAvatarUrl(), image);
        }

        user.setAvatarUrl(newUrl);
        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateMyInfo(Long userId, com.zyna.dev.ecommerce.users.dto.request.UserUpdateProfileRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found or deleted")
        );

        userMapper.applyUpdateProfile(user, request);

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    private String getCurrentActorEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
