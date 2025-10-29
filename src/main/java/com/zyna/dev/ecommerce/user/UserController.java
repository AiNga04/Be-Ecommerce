package com.zyna.dev.ecommerce.user;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;
import com.zyna.dev.ecommerce.user.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userRequest) {
        log.info("API: create user {}", userRequest.getEmail());
        UserResponse userResponse = userService.createUser(userRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Create user successfully!",
                userResponse
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userRequest
    ) {
        log.info("API: update user id={}", id);
        UserResponse userResponse = userService.updateUser(id, userRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Update user successfully!",
                userResponse
        );
    }
}
