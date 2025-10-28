package com.zyna.dev.ecommerce.user;

import com.zyna.dev.ecommerce.user.dto.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper mapper;

    public User toUser(UserCreateRequest createRequest) {
        User user = mapper.map(createRequest, User.class);
        user.setDeleted(false);
        return user;
    }

    public UserResponse toUserResponse(User user) {
        return mapper.map(user, UserResponse.class);
    }
}
