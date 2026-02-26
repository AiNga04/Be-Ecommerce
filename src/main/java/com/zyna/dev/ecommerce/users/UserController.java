package com.zyna.dev.ecommerce.users;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.users.criteria.UserCriteria;
import com.zyna.dev.ecommerce.users.dto.response.UserBatchCreateResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import com.zyna.dev.ecommerce.users.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.users.dto.request.UserUpdateRequest;
import com.zyna.dev.ecommerce.users.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private Long getCurrentUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return userService.getUserIdByEmail(email);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> getMyInfo(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        UserResponse userResponse = userService.getUserById(userId);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Fetched current user successfully!",
                userResponse
        );
    }

    @PatchMapping("/me/avatar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> updateMyAvatar(
            Authentication authentication,
            @RequestPart("image") MultipartFile image
    ) {
        Long userId = getCurrentUserId(authentication);
        UserResponse userResponse = userService.updateAvatar(userId, image);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Avatar updated successfully!",
                userResponse
        );
    }

    @PatchMapping("/me/info")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> updateMyInfo(
            Authentication authentication,
            @RequestBody @Valid com.zyna.dev.ecommerce.users.dto.request.UserUpdateProfileRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        UserResponse userResponse = userService.updateMyInfo(userId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Profile updated successfully!",
                userResponse
        );
    }

    @PatchMapping("/{id}/avatar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<UserResponse> updateUserAvatarByAdmin(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image
    ) {
        UserResponse userResponse = userService.updateAvatarByAdmin(id, image);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Avatar updated successfully!",
                userResponse
        );
    }

    // LIST SHIPPERS (đặt trước GET /{id} để tránh conflict path)
    @GetMapping("/shippers")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('SHIPPING_MANAGE')")
    public ApiResponse<List<UserResponse>> getShippers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserResponse> result = userService.getShippers(page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get shippers successfully!",
                result
        );
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_WRITE')")
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
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get user successfully!",
                userResponse
        );
    }

    // SEARCH (filter + pagination)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<List<UserResponse>> searchUsers(
            @Valid UserCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserResponse> result = userService.searchUsers(criteria, page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get users successfully!",
                result
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
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

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody @Valid com.zyna.dev.ecommerce.users.dto.request.UserStatusUpdateRequest request
    ) {
        UserResponse userResponse = userService.updateStatus(id, request.getStatus());
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Update user status successfully!",
                userResponse
        );
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
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
    @PreAuthorize("hasAuthority('USER_WRITE')")
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
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<String> hardDeleteUser(@PathVariable Long id) {
        userService.hardDeleteUser(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Delete user successfully!",
                "User " + id + " deleted!"
        );
    }

    // SOFT DELETE NHIỀU
    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<Map<String, Object>> softDeleteUsers(@RequestBody List<Long> ids) {
        var deletedIds = userService.softDeleteUsers(ids);

        Map<String, Object> result = new HashMap<>();
        result.put("requestedIds", ids);
        result.put("deletedIds", deletedIds);
        result.put("notDeletedIds", diff(ids, deletedIds));

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Soft delete users successfully!",
                result
        );
    }

    // RESTORE NHIỀU
    @PatchMapping("/batch/restore")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<Map<String, Object>> restoreUsers(@RequestBody List<Long> ids) {
        var restoredIds = userService.restoreUsers(ids);

        Map<String, Object> result = new HashMap<>();
        result.put("requestedIds", ids);
        result.put("restoredIds", restoredIds);
        result.put("notRestoredIds", diff(ids, restoredIds));

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Restore users successfully!",
                result
        );
    }

    // HARD DELETE NHIỀU
    @DeleteMapping("/batch/hard")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<Map<String, Object>> hardDeleteUsers(@RequestBody List<Long> ids) {
        var deletedIds = userService.hardDeleteUsers(ids);

        Map<String, Object> result = new HashMap<>();
        result.put("requestedIds", ids);
        result.put("deletedIds", deletedIds);
        result.put("notDeletedIds", diff(ids, deletedIds));

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Hard delete users successfully!",
                result
        );
    }

    // GET DELETED
    @GetMapping("/deleted")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<List<UserResponse>> getDeletedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            UserCriteria criteria
    ) {
        Page<UserResponse> deletedUsers = userService.getDeletedUsers(criteria, page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "List of soft-deleted users!",
                deletedUsers
        );
    }

    @PostMapping("/batch-create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ApiResponse<UserBatchCreateResponse> createUsers(
            @Valid @RequestBody com.zyna.dev.ecommerce.users.dto.request.UserBatchCreateRequest request
    ) {
        var result = userService.createUsers(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Batch create users done!",
                result
        );
    }


    private List<Long> diff(List<Long> requested, List<Long> processed) {
        Set<Long> processedSet = new HashSet<>(processed);
        return requested.stream()
                .filter(id -> !processedSet.contains(id))
                .toList();
    }
}
