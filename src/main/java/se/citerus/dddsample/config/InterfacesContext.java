package se.citerus.dddsample.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.interfaces.handling.file.UploadDirectoryScanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(InterfacesProperties.class)
public class InterfacesContext implements WebMvcConfigurer {

    public final InterfacesProperties properties;

    @Bean
    // todo  is this in using?
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    @Bean
    // todo  is this in using?
    public FixedLocaleResolver localeResolver() {
        FixedLocaleResolver fixedLocaleResolver = new FixedLocaleResolver();
        fixedLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return fixedLocaleResolver;
    }

    @Bean
    public UploadDirectoryScanner uploadDirectoryScanner(ApplicationEvents applicationEvents) {
        Path uploadDirectory = Paths.get(properties.getUploadDirectory());
        Path parseFailureDirectory = Paths.get(properties.getParseFailureDirectory());
        return new UploadDirectoryScanner(uploadDirectory, parseFailureDirectory, applicationEvents);
    }

}
