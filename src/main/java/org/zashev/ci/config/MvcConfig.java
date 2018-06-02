package org.zashev.ci.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/create-job").setViewName("create-job");
        registry.addViewController("/save-job").setViewName("index");
        registry.addViewController("/build-history").setViewName("build-history");
        registry.addViewController("/get-run-job").setViewName("run-job");
        registry.addViewController("/").setViewName("index");
    }
}
