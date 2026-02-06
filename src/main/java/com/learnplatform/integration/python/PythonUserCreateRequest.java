package com.learnplatform.integration.python;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python 服务注册请求
 */
public record PythonUserCreateRequest(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password,
        @JsonProperty("email") String email
) {
}
