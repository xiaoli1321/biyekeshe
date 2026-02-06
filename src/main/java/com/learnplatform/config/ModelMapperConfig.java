package com.learnplatform.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // 配置全局映射策略
        mapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true);

        // 自定义映射配置
        configureMappings(mapper);

        return mapper;
    }

    private void configureMappings(ModelMapper mapper) {
        // Course -> CourseDto 映射
        /* 已在CourseDto中自定义转换方法，暂时不需要全局映射
        TypeMap<Course, CourseDto> courseTypeMap = mapper.createTypeMap(Course.class, CourseDto.class);
        */
    }
}