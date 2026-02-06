package com.learnplatform.service;

import com.learnplatform.entity.User;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到: " + usernameOrEmail));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用: " + usernameOrEmail);
        }

        // 返回UserPrincipal而不是Spring Security的默认User
        return UserPrincipal.create(user, true);
    }

    /**
     * 通过用户ID加载用户
     * @param id 用户ID
     * @return UserPrincipal
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Transactional
    public UserPrincipal loadUserById(String id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到: " + id));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用: " + id);
        }

        // 返回不带密码的UserPrincipal（更安全）
        return UserPrincipal.create(user, false);
    }

    /**
     * 通过邮箱获取用户（辅助方法）
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
}