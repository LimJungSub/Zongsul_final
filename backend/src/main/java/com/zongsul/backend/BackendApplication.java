package com.zongsul.backend;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
    @Bean
    public ApplicationRunner applicationRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("===== REGISTERED MAPPINGS =====");
            var mapping = ctx.getBean(
                    "requestMappingHandlerMapping",
                    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping.class
            );
            mapping.getHandlerMethods().forEach((key, value) -> {
                System.out.println(key + " -> " + value);
            });
            System.out.println("================================");
        };
    }
}
