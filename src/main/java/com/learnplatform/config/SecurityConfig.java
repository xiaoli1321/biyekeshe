package com.learnplatform.config;

import com.learnplatform.security.JwtAuthenticationEntryPoint;
import com.learnplatform.security.JwtAuthenticationFilter;
import com.learnplatform.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类（支持JWT认证）
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT，无需CSRF保护）
            .csrf(csrf -> csrf.disable())

            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置会话管理为无状态（使用JWT）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 公开API - 不需要认证
                .requestMatchers(
                    "/api/auth/**",    // 认证相关API
                    "/api/public/**",  // 其他公开API
                    "/api/llm/**",     // LLM 工作流 API
                    "/api/agents/*/chat-stream", // 允许 SSE 流式对话公开访问 (兼容 EventSource)
                    "/v3/api-docs/**", // Swagger文档
                    "/swagger-ui/**",  // Swagger UI
                    "/error"           // 错误页面
                ).permitAll()

                // 允许静态资源访问
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // 允许H2控制台（开发环境）
                .requestMatchers("/h2-console/**").permitAll()

                // 管理员API需要ADMIN角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 其他所有API需要认证
                .anyRequest().authenticated()
            )

            // 配置禁用frameOptions（为了H2控制台）
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            );

        // 添加JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前）
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        // 允许的源（生产环境中应该设置具体的域名）
        configuration.setAllowedOriginPatterns(java.util.Collections.singletonList("*"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));

        // 允许发送认证信息（JWT Token）
        configuration.setAllowCredentials(true);

        // 缓存预检请求的时间（秒）
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}