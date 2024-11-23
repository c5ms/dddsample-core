package se.citerus.dddsample.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.citerus.dddsample.domain.model.cargo.CargoFactory;
import se.citerus.dddsample.domain.model.cargo.CargoRepository;
import se.citerus.dddsample.domain.model.handling.HandlingEventFactory;
import se.citerus.dddsample.domain.model.location.LocationRepository;
import se.citerus.dddsample.domain.model.voyage.VoyageRepository;


@Configuration
public class DDDSampleApplicationContext {

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private VoyageRepository voyageRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Bean
    public CargoFactory cargoFactory() {
        return new CargoFactory(locationRepository, cargoRepository);
    }

    @Bean
    public HandlingEventFactory handlingEventFactory() {
        return new HandlingEventFactory(cargoRepository, voyageRepository, locationRepository);
    }

}
