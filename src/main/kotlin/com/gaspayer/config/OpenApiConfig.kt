package com.gaspayer.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "ApiKeyAuth"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Gas Payer Service API")
                    .version("1.0.0")
                    .description("""
                        Service for relaying blockchain transactions with gas payment handling.
                        
                        **Authentication Requirements:**
                        - All endpoints require an API key in the `X-API-KEY` header
                        - Requests must originate from whitelisted IP addresses configured in the security config
                        
                        **Security Note:** Both valid API key AND whitelisted IP address are required for access.
                        
                        **Access:** To request API access, contact charlie@conduit-ucpi.com
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("Gas Payer Service Team")
                            .email("charlie@conduit-ucpi.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0")
                    )
            )
            .addServersItem(Server().url("/").description("Default Server"))
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name("X-API-KEY")
                            .description("API key for authentication")
                    )
            )
    }
}