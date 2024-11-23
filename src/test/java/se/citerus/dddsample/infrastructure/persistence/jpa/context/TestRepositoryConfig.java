package se.citerus.dddsample.infrastructure.persistence.jpa.context;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;
import se.citerus.dddsample.domain.model.cargo.CargoRepository;
import se.citerus.dddsample.domain.model.handling.HandlingEventFactory;
import se.citerus.dddsample.domain.model.handling.HandlingEventRepository;
import se.citerus.dddsample.domain.model.location.LocationRepository;
import se.citerus.dddsample.domain.model.voyage.VoyageRepository;
import se.citerus.dddsample.infrastructure.sampledata.SampleDataGenerator;

/**
 * This config is required by the repository tests to initialize the test env.
 */
@TestConfiguration
public class TestRepositoryConfig {

    @Bean
    public HandlingEventFactory handlingEventFactory(CargoRepository cargoRepository,
                                                     VoyageRepository voyageRepository,
                                                     LocationRepository locationRepository) {
        return new HandlingEventFactory(cargoRepository, voyageRepository, locationRepository);
    }

    @Bean
    public SampleDataGenerator sampleDataGenerator(CargoRepository cargoRepository,
                                                   VoyageRepository voyageRepository,
                                                   LocationRepository locationRepository,
                                                   HandlingEventRepository handlingEventRepository,
                                                   HandlingEventFactory handlingEventFactory,
                                                   TransactionTemplate transactionTemplate) {
        return new SampleDataGenerator(cargoRepository,
            voyageRepository,
            locationRepository,
            handlingEventRepository,
            handlingEventFactory,
            transactionTemplate);
    }
}
