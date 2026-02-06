package com.learnplatform.integration.python;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python 服务登录令牌响应
 */
public record PythonTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType
) {
}
