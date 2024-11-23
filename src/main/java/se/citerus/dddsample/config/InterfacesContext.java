package se.citerus.dddsample.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.interfaces.handling.file.UploadDirectoryScanner;

import java.io.File;
import java.time.Duration;
import java.util.Locale;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(InterfacesProperties.class)
public class InterfacesContext implements WebMvcConfigurer {

    public final InterfacesProperties properties;

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    public FixedLocaleResolver localeResolver() {
        FixedLocaleResolver fixedLocaleResolver = new FixedLocaleResolver();
        fixedLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return fixedLocaleResolver;
    }

    @Bean
    public UploadDirectoryScanner uploadDirectoryScanner(ApplicationEvents applicationEvents) {
        File uploadDirectoryFile = new File(properties.getUploadDirectory());
        File parseFailureDirectoryFile = new File(properties.getParseFailureDirectory());
        return new UploadDirectoryScanner(uploadDirectoryFile, parseFailureDirectoryFile, applicationEvents);
    }

    @Bean
    public ThreadPoolTaskScheduler myScheduler(@Nullable UploadDirectoryScanner scanner) {
        if (scanner == null) {
            log.info("No UploadDirectoryScannerBean found, skipping creation of scheduler.");
            return null;
        }
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.scheduleAtFixedRate(scanner, Duration.ofSeconds(5));
        return threadPoolTaskScheduler;
    }
}
