package se.citerus.dddsample.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.citerus.dddsample.domain.model.cargo.CargoFactory;
import se.citerus.dddsample.domain.model.cargo.CargoRepository;
import se.citerus.dddsample.domain.model.handling.HandlingEventFactory;
import se.citerus.dddsample.domain.model.location.LocationRepository;
import se.citerus.dddsample.domain.model.voyage.VoyageRepository;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(DomainProperties.class)
public class DomainContext {

    private final CargoRepository cargoRepository;
    private final VoyageRepository voyageRepository;
    private final LocationRepository locationRepository;

    @Bean
    public CargoFactory cargoFactory() {
        return new CargoFactory(locationRepository, cargoRepository);
    }

    @Bean
    public HandlingEventFactory handlingEventFactory() {
        return new HandlingEventFactory(cargoRepository, voyageRepository, locationRepository);
    }
}