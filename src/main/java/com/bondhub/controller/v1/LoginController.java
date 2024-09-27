package com.bondhub.controller.v1;

import com.bondhub.controller.v1.request.LoginRequest;
import com.bondhub.controller.v1.response.LoginResponse;
import com.bondhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LoginController {

    private final UserService userService;

    @PostMapping("/api/v1/login/register")
    public void register(@RequestBody LoginRequest request) {
        userService.register(request.getUsername(), request.getPassword());
    }

    @PostMapping("/api/v1/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return new LoginResponse(token);
    }
}
