package com.zyna.dev.ecommerce.user;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.user.criteria.UserCriteria;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;
import com.zyna.dev.ecommerce.user.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest userRequest
    ) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Create user successfully!",
                userResponse
        );
    }

    // READ DETAIL
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get user successfully!",
                userResponse
        );
    }

    // SEARCH (filter + pagination)
    // GET /users?page=0&size=10&firstName=na&role=ADMIN&city=HO_CHI_MINH
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<UserResponse>> searchUsers(
            @Valid UserCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserResponse> result = userService.searchUsers(criteria, page, size);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get users successfully!",
                result
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userRequest
    ) {
        UserResponse userResponse = userService.updateUser(id, userRequest);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Update user successfully!",
                userResponse
        );
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Delete user successfully!",
                "User " + id + " deleted!"
        );
    }

    // RESTORE
    @PatchMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Restore user successfully!",
                "User " + id + " restored!"
        );
    }

    // HARD DELETE
    @DeleteMapping("/{id}/hard")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> hardDeleteUser(@PathVariable Long id) {
        userService.hardDeleteUser(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Delete user successfully!",
                "User " + id + " deleted!"
        );
    }
}
