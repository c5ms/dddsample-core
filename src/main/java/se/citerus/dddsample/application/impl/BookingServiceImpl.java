package se.citerus.dddsample.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.citerus.dddsample.application.BookingService;
import se.citerus.dddsample.domain.model.cargo.*;
import se.citerus.dddsample.domain.model.location.Location;
import se.citerus.dddsample.domain.model.location.LocationRepository;
import se.citerus.dddsample.domain.model.location.UnLocode;
import se.citerus.dddsample.domain.service.RoutingService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CargoRepository cargoRepository;
    private final LocationRepository locationRepository;
    private final RoutingService routingService;
    private final CargoFactory cargoFactory;

    @Override
    @Transactional
    public TrackingId bookNewCargo(final UnLocode originUnLocode,
                                   final UnLocode destinationUnLocode,
                                   final Instant arrivalDeadline) {
        Cargo cargo = cargoFactory.createCargo(originUnLocode, destinationUnLocode, arrivalDeadline);

        cargoRepository.store(cargo);
        log.info("Booked new cargo with tracking id {}", cargo.trackingId().idString());

        return cargo.trackingId();
    }

    @Override
    @Transactional
    public List<Itinerary> requestPossibleRoutesForCargo(final TrackingId trackingId) {
        final Cargo cargo = cargoRepository.find(trackingId);

        if (cargo == null) {
            return Collections.emptyList();
        }

        return routingService.fetchRoutesForSpecification(cargo.routeSpecification());
    }

    @Override
    @Transactional
    public void assignCargoToRoute(final Itinerary itinerary, final TrackingId trackingId) {
        final Cargo cargo = cargoRepository.find(trackingId);
        if (cargo == null) {
            throw new IllegalArgumentException("Can't assign itinerary to non-existing cargo " + trackingId);
        }

        cargo.assignToRoute(itinerary);
        cargoRepository.store(cargo);

        log.info("Assigned cargo {} to new route", trackingId);
    }

    @Override
    @Transactional
    public void changeDestination(final TrackingId trackingId, final UnLocode unLocode) {
        final Cargo cargo = cargoRepository.find(trackingId);
        final Location newDestination = locationRepository.find(unLocode);

        final RouteSpecification routeSpecification = new RouteSpecification(
            cargo.origin(), newDestination, cargo.routeSpecification().arrivalDeadline()
        );
        cargo.specifyNewRoute(routeSpecification);

        cargoRepository.store(cargo);
        log.info("Changed destination for cargo {} to {}", trackingId, routeSpecification.destination());
    }

}
