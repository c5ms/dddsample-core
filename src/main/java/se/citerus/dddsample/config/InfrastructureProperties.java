package se.citerus.dddsample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "ddd.sample.infrastructure.messaging")
public class InfrastructureProperties {

    private String brokerUrl;

}
