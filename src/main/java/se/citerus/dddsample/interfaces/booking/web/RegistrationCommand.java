package se.citerus.dddsample.interfaces.booking.web;

import lombok.Data;

@Data
public final class RegistrationCommand {

  private String originUnlocode;
  private String destinationUnlocode;
  private String arrivalDeadline;

}
