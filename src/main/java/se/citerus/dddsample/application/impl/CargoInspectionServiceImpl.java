package se.citerus.dddsample.application.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.application.CargoInspectionService;
import se.citerus.dddsample.domain.model.cargo.Cargo;
import se.citerus.dddsample.domain.model.cargo.CargoRepository;
import se.citerus.dddsample.domain.model.cargo.TrackingId;
import se.citerus.dddsample.domain.model.handling.HandlingEventRepository;
import se.citerus.dddsample.domain.model.handling.HandlingHistory;

@Slf4j
@Service
public class CargoInspectionServiceImpl implements CargoInspectionService {

    private final ApplicationEvents applicationEvents;
    private final CargoRepository cargoRepository;
    private final HandlingEventRepository handlingEventRepository;

    public CargoInspectionServiceImpl(final ApplicationEvents applicationEvents,
                                      final CargoRepository cargoRepository,
                                      final HandlingEventRepository handlingEventRepository) {
        this.applicationEvents = applicationEvents;
        this.cargoRepository = cargoRepository;
        this.handlingEventRepository = handlingEventRepository;
    }

    @Override
    @Transactional
    public void inspectCargo(final TrackingId trackingId) {
        Validate.notNull(trackingId, "Tracking ID is required");

        final Cargo cargo = cargoRepository.find(trackingId);
        if (cargo == null) {
            log.warn("Can't inspect non-existing cargo {}", trackingId);
            return;
        }

        final HandlingHistory handlingHistory = handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId);

        cargo.deriveDeliveryProgress(handlingHistory);

        if (cargo.delivery().isMisdirected()) {
            applicationEvents.cargoWasMisdirected(cargo);
        }

        if (cargo.delivery().isUnloadedAtDestination()) {
            applicationEvents.cargoHasArrived(cargo);
        }

        cargoRepository.store(cargo);
    }
}
