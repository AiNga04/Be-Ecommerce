package com.zyna.dev.ecommerce.user.service.impl;

import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.user.User;
import com.zyna.dev.ecommerce.user.UserMapper;
import com.zyna.dev.ecommerce.user.UserRepository;
import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;
import com.zyna.dev.ecommerce.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


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
        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        log.info("Update user id={}", id);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + id + " not found!"
                ));

        userMapper.applyUpdate(existing, updateRequest);

        User saved = userRepository.save(existing);

        log.info("User updated id={}", saved.getId());
        return userMapper.toUserResponse(saved);
    }
}
