package com.learnplatform.controller;

import com.learnplatform.entity.User;
import com.learnplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String showLoginPage(Model model,
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误！");
        }
        if (logout != null) {
            model.addAttribute("message", "您已成功退出登录！");
        }

        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }

        return "auth/login";
    }

    /**
     * 处理登录提交（由Spring Security处理）
     */
    @PostMapping("/login")
    public String processLogin() {
        // 实际由Spring Security处理
        return "redirect:/dashboard";
    }

    /**
     * 显示注册页面
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    /**
     * 注册用户
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              BindingResult result,
                              Model model) {
        // 验证输入
        if (result.hasErrors()) {
            return "auth/register";
        }

        // 检查用户名和邮箱是否已存在
        if (userService.usernameExists(user.getUsername())) {
            result.rejectValue("username", "error.username", "用户名已存在");
            return "auth/register";
        }

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.email", "邮箱已存在");
            return "auth/register";
        }

        try {
            User registeredUser = userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
            model.addAttribute("message", "注册成功！请使用您的用户名和密码登录。");
            model.addAttribute("success", true);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.registration", e.getMessage());
            return "auth/register";
        }
    }

    /**
     * 首页重定向
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}