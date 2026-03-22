package com.learnplatform.controller.api;

import com.learnplatform.dto.ApiResponse;
import com.learnplatform.entity.KbCollection;
import com.learnplatform.security.UserPrincipal;
import com.learnplatform.service.KbCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kb/collections")
@Tag(name = "知识库空间管理", description = "处理多知识库空间的创建与管理")
@CrossOrigin(origins = "*", maxAge = 3600)
public class KbCollectionController {

    @Autowired
    private KbCollectionService collectionService;

    @Operation(summary = "获取当前用户的所有知识库")
    @GetMapping
    public ApiResponse<List<KbCollection>> getCollections(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<KbCollection> collections = collectionService.getCollectionsByUser(userPrincipal.getId());
        return ApiResponse.success(collections);
    }

    @Operation(summary = "创建新知识库空间")
    @PostMapping
    public ApiResponse<KbCollection> createCollection(
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String name = payload.get("name");
        String description = payload.get("description");
        if (name == null || name.isBlank()) {
            return ApiResponse.error("名称不能为空");
        }
        KbCollection collection = collectionService.createCollection(name, description, userPrincipal.getId());
        return ApiResponse.success(collection, "创建成功");
    }

    @Operation(summary = "删除知识库空间")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCollection(@PathVariable("id") String id, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        collectionService.deleteCollection(id, userPrincipal.getId());
        return ApiResponse.success(null, "已提交删除");
    }
}
