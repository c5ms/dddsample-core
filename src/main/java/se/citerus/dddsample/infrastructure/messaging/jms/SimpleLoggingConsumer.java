package se.citerus.dddsample.infrastructure.messaging.jms;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleLoggingConsumer implements MessageListener {


  @Override
  public void onMessage(Message message) {
    log.debug("Received JMS message: {}", message);
  }

}
