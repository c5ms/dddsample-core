package se.citerus.dddsample.infrastructure.messaging.jms;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.citerus.dddsample.application.HandlingEventService;
import se.citerus.dddsample.interfaces.handling.HandlingEventRegistrationAttempt;

import java.lang.invoke.MethodHandles;

/**
 * Consumes handling event registration attempt messages and delegates to
 * proper registration.
 *
 */
@Slf4j
public class HandlingEventRegistrationAttemptConsumer implements MessageListener {

  private final HandlingEventService handlingEventService;

  public HandlingEventRegistrationAttemptConsumer(HandlingEventService handlingEventService) {
    this.handlingEventService = handlingEventService;
  }

  @Override
  public void onMessage(final Message message) {
    try {
      final ObjectMessage om = (ObjectMessage) message;
      HandlingEventRegistrationAttempt attempt = (HandlingEventRegistrationAttempt) om.getObject();
      handlingEventService.registerHandlingEvent(
        attempt.getCompletionTime(),
        attempt.getTrackingId(),
        attempt.getVoyageNumber(),
        attempt.getUnLocode(),
        attempt.getType()
      );
    } catch (Exception e) {
      log.error("Error consuming HandlingEventRegistrationAttempt message", e);
    }
  }
}
