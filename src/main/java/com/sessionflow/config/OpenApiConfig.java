package com.sessionflow.config;

import com.sessionflow.exception.ErrorDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SessionFlow API")
                        .description("工作階段管理系統 API")
                        .version("1.0.0"));
    }

    @Bean
    public OpenApiCustomizer errorExampleCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            // 為每個錯誤定義註冊範例
            for (ErrorDefinition errorDef : ErrorDefinition.values()) {
                Example example = new Example()
                        .summary(errorDef.getMessage())
                        .description("錯誤回應範例: " + errorDef.getMessage())
                        .value(errorDef.generateDefaultExample());

                String exampleKey = errorDef.getCode().toLowerCase() + "_example";
                components.addExamples(exampleKey, example);
            }
        };
    }
} 