package com.zyna.dev.ecommerce.user.service.impl;

import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.user.User;
import com.zyna.dev.ecommerce.user.UserMapper;
import com.zyna.dev.ecommerce.user.UserRepository;
import com.zyna.dev.ecommerce.user.dto.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public UserResponse createUser(UserCreateRequest createRequest) {
        // check trùng email
        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // map DTO -> Entity
        User user = userMapper.toUser(createRequest);

        // set default status nếu null
        if (user.getStatus() == null) {
            user.setStatus(Status.PENDING);
        }

        // lưu DB
        User saved = userRepository.save(user);

        // map Entity -> Response DTO
        return userMapper.toUserResponse(saved);
    }
}
