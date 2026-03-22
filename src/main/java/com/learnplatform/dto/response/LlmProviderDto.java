package com.learnplatform.dto.response;

public class LlmProviderDto {
    private String id;
    private String name;
    private String modelAlias;
    private boolean isCustom;
    private String baseUrl; 

    public LlmProviderDto() {}

    public LlmProviderDto(String id, String name, String modelAlias, boolean isCustom, String baseUrl) {
        this.id = id;
        this.name = name;
        this.modelAlias = modelAlias;
        this.isCustom = isCustom;
        this.baseUrl = baseUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelAlias() {
        return modelAlias;
    }

    public void setModelAlias(String modelAlias) {
        this.modelAlias = modelAlias;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
