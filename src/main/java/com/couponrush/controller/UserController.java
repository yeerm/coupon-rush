package com.couponrush.controller;

import com.couponrush.common.response.ApiResponse;
import com.couponrush.dto.request.UserCreateRequest;
import com.couponrush.dto.response.UserResponse;
import com.couponrush.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }
}
