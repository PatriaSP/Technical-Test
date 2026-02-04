package com.patria.test.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "BE Test API",
                version = "1.0",
                description = "Test API Documentation",
                contact = @Contact(
                        name = "Patria",
                        email = "patriasp809@gmail.com"
                )
        )
)
public class SwaggerConfig {

}
