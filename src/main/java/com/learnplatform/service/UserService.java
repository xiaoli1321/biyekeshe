package com.learnplatform.service;

import com.learnplatform.entity.User;
import com.learnplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户业务服务
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public User registerUser(String username, String email, String password) {
        // 检查用户名和邮箱是否存在
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在: " + email);
        }

        // 创建新用户
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword, User.UserRole.USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * 用户登录
     */
    public User authenticateUser(String usernameOrEmail, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return user;
            }
        }

        throw new IllegalArgumentException("用户名或密码错误");
    }

    /**
     * 查找用户
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * 查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 查找用户
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 启用/禁用用户
     */
    public User setUserEnabled(String userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户未找到"));
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    /**
     * 创建用户 (别名方法)
     */
    public User createUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 按角色查找用户
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 统计用户总数
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * 统计启用用户数
     */
    public long countEnabledUsers() {
        return userRepository.countByEnabled(true);
    }
}