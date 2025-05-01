package dev.kkm.config;


import dev.kkm.model.FileProperties;
import dev.kkm.service.FileStorageService;
import dev.kkm.service.FileStorageServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@AutoConfiguration
@EnableConfigurationProperties(FileProperties.class)
public class FileStorageConfig {

    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService() {
        return new FileStorageServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileProperties fileProperties() {
        return new FileProperties();
    }

}
