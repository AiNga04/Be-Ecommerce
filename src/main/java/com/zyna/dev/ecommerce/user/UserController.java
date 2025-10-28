package com.zyna.dev.ecommerce.user;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.user.dto.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@RequestBody UserCreateRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Create user successfully!",
                userResponse
        );
    }
}
