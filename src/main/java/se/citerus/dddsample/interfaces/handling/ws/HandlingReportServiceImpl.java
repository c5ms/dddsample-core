package se.citerus.dddsample.interfaces.handling.ws;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.interfaces.handling.HandlingEventRegistrationAttempt;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.parse;

/**
 * This web service endpoint implementation performs basic validation and parsing
 * of incoming data, and in case of a valid registration attempt, sends an asynchronous message
 * with the information to the handling event registration system for proper registration.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HandlingReportServiceImpl implements HandlingReportService {

    private final ApplicationEvents applicationEvents;

    @PostMapping(value = "/handlingReport")
    @Override
    public ResponseEntity<?> submitReport(@Valid @RequestBody HandlingReport handlingReport) {
        try {
            List<HandlingEventRegistrationAttempt> attempts = parse(handlingReport);
            attempts.forEach(applicationEvents::receivedHandlingEventRegistrationAttempt);
        } catch (Exception e) {
            log.error("Unexpected error in submitReport", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
        return ResponseEntity.status(CREATED).build();
    }
}
