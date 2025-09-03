package com.gaspayer.config

import com.utility.chainservice.BlockchainProperties
import com.utility.chainservice.UtilityAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(UtilityAutoConfiguration::class)
class BlockchainConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "blockchain")
    fun blockchainProperties(): BlockchainProperties {
        return BlockchainProperties()
    }
    
    @Bean("securityConfigPath")
    fun securityConfigPath(@org.springframework.beans.factory.annotation.Value("\${security.config-path:/app/config/security-config.json}") configPath: String): String {
        return configPath
    }
}