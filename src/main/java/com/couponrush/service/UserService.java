package com.couponrush.service;

import com.couponrush.dto.request.UserCreateRequest;
import com.couponrush.dto.response.UserResponse;
import com.couponrush.entity.User;
import com.couponrush.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = User.create(request.email(), request.name());
        return UserResponse.from(userRepository.save(user));
    }
}
