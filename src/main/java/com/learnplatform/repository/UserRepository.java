package com.learnplatform.repository;

import com.learnplatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓库接口
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据角色查找用户
     */
    List<User> findByRole(User.UserRole role);

    /**
     * 根据角色分页查找用户
     */
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    /**
     * 根据激活状态查找用户
     */
    List<User> findByEnabled(boolean enabled);

    /**
     * 查询最近注册的用户
     */
    List<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 统计启用用户数
     */
    long countByEnabled(boolean enabled);

    /**
     * 根据关键词搜索用户
     */
    @Query("{$or: [{username: {$regex: ?0, $options: 'i'}}, {email: {$regex: ?0, $options: 'i'}}]}")
    Page<User> searchUsers(String keyword, Pageable pageable);
}