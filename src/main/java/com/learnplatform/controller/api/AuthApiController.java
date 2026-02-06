package com.learnplatform.controller.api;

import com.learnplatform.dto.request.LoginRequest;
import com.learnplatform.dto.request.RegisterRequest;
import com.learnplatform.dto.ApiResponse;
import com.learnplatform.dto.response.UserDto;
import com.learnplatform.entity.User;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.security.JwtTokenProvider;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录、注册、退出等认证相关API")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Operation(summary = "用户登录", description = "使用邮箱和密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("收到登录请求: " + loginRequest.getEmail());
        try {
            // 验证用户凭证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 设置认证信息到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT Token
            String jwt = tokenProvider.generateToken(authentication);

            // 获取用户信息
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            UserDto userDto = UserDto.fromUser(userPrincipal.toUser());

            // 返回登录响应
            LoginResponse loginResponse = new LoginResponse(jwt, userDto);
            return ApiResponse.success(loginResponse, "登录成功");
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ApiResponse.error("用户名或密码错误", "BAD_CREDENTIALS");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("登录失败: " + e.getMessage(), "LOGIN_ERROR");
        }
    }

    @Operation(summary = "用户注册", description = "创建新用户账户")
    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // 验证用户是否已存在
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ApiResponse.error("该邮箱已被注册", "EMAIL_EXISTS");
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ApiResponse.error("该用户名已被注册", "USERNAME_EXISTS");
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setRole(User.UserRole.USER);
        newUser.setEnabled(true);

        // 保存用户
        User savedUser = userService.createUser(newUser);

        // 返回用户信息（不包含密码）
        UserDto userDto = UserDto.fromUser(savedUser);
        return ApiResponse.success(userDto, "注册成功");
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/profile")
    public ApiResponse<UserDto> getCurrentUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error("用户未登录", "NOT_AUTHENTICATED");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserDto userDto = UserDto.fromUser(userPrincipal.toUser());

        return ApiResponse.success(userDto, "获取用户信息成功");
    }

    @Operation(summary = "用户登出", description = "清除当前用户会话（实际上JWT是无状态，主要是前端清除token）")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // JWT是无状态的，所以这里主要是前端清除token
        SecurityContextHolder.clearContext();
        return ApiResponse.success(null, "登出成功");
    }

    // 内部类：登录响应
    public static class LoginResponse {
        private String token;
        private UserDto user;

        public LoginResponse() {
        }

        public LoginResponse(String token, UserDto user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserDto getUser() {
            return user;
        }

        public void setUser(UserDto user) {
            this.user = user;
        }
    }
}