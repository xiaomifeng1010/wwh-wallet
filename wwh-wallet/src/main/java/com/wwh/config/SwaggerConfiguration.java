package com.wwh.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

/**
 * Created by zl on 2015/8/27.
 */
@Configuration
@EnableSwagger
@EnableAutoConfiguration
public class SwaggerConfiguration
{

    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }


    public SwaggerSpringMvcPlugin customImplementation(){
        return  new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
                .apiInfo(new ApiInfo("api", "desc", null, null, null, null))
                .useDefaultResponseMessages(false)
                .includePatterns("/users.*");
    }
}