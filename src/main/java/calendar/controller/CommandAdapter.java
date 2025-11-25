package calendar.controller;

import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Implementation of IcommandAdapter that converts GUI parameters
 * into CLI-style command strings compatible with the existing
 * CommandParser / CommandFactory pipeline.
 *
 * <p>This class now delegates all formatting/quoting to
 * {@link CommandStringHelper} to maintain SRP and reduce code duplication.</p>
 */
public class CommandAdapter implements IcommandAdapter {

  @Override
  public String buildCreateCalendarCommand(String name, String timezone) {
    return "create calendar "
        + "--name " + CommandStringHelper.quote(name) + " "
        + "--timezone " + CommandStringHelper.quote(timezone);
  }

  @Override
  public String buildUseCalendarCommand(String calendarName) {
    return "use calendar --name " + CommandStringHelper.quote(calendarName);
  }

  @Override
  public String buildCreateSingleEventCommand(String subject,
                                              ZonedDateTime start,
                                              ZonedDateTime end,
                                              String description,
                                              String location,
                                              EventStatus status) {

    String startStr = CommandStringHelper.formatZdtLocalIso(start);
    String endStr   = CommandStringHelper.formatZdtLocalIso(end);

    StringBuilder sb = new StringBuilder();

    sb.append("create event ")
        .append(CommandStringHelper.quote(subject)).append(" ")
        .append("from ").append(startStr).append(" ")
        .append("to ").append(endStr).append(" ");

    if (description != null && !description.isEmpty()) {
      sb.append("description ")
          .append(CommandStringHelper.quote(description)).append(" ");
    }

    if (location != null && !location.isEmpty()) {
      sb.append("location ")
          .append(CommandStringHelper.quote(location)).append(" ");
    }

    if (status != null) {
      sb.append("status ").append(status.name());
    }

    return sb.toString().trim();
  }

  @Override
  public String buildCreateRecurringEventCommand(String subject,
                                                 ZonedDateTime start,
                                                 ZonedDateTime end,
                                                 String description,
                                                 String location,
                                                 EventStatus status,
                                                 String weekdayPattern,
                                                 Integer occurrences,
                                                 LocalDate endDate) {

    String startStr = CommandStringHelper.formatZdtLocalIso(start);
    String endStr   = CommandStringHelper.formatZdtLocalIso(end);

    StringBuilder sb = new StringBuilder();

    sb.append("create event ")
        .append(CommandStringHelper.quote(subject)).append(" ")
        .append("from ").append(startStr).append(" ")
        .append("to ").append(endStr).append(" ");

    // Recurrence rules
    if (weekdayPattern != null && !weekdayPattern.isEmpty()) {
      sb.append("repeats ")
          .append(weekdayPattern.toUpperCase())
          .append(" ");
    }

    if (occurrences != null) {
      sb.append("for ").append(occurrences).append(" ");
    } else if (endDate != null) {
      sb.append("until ").append(CommandStringHelper.formatDate(endDate)).append(" ");
    }

    // Optional fields
    if (description != null && !description.isEmpty()) {
      sb.append("description ")
          .append(CommandStringHelper.quote(description)).append(" ");
    }

    if (location != null && !location.isEmpty()) {
      sb.append("location ")
          .append(CommandStringHelper.quote(location)).append(" ");
    }

    if (status != null) {
      sb.append("status ").append(status.name());
    }

    return sb.toString().trim();
  }

  @Override
  public String buildEditSingleEventCommand(String subject,
                                            ZonedDateTime start,
                                            ZonedDateTime end,
                                            String property,
                                            Object newValue) {

    String prefix = CommandStringHelper.buildEditPrefix(
        "event", subject, start, end
    );

    String valueStr = CommandStringHelper.formatPropertyValue(property, newValue);

    return prefix + "with " + property.toLowerCase() + " " + valueStr;
  }

  @Override
  public String buildEditSeriesFromThisOnwardCommand(String subject,
                                                     ZonedDateTime start,
                                                     ZonedDateTime end,
                                                     String property,
                                                     Object newValue) {

    String prefix = CommandStringHelper.buildEditPrefix(
        "events", subject, start, end
    );

    String valueStr = CommandStringHelper.formatPropertyValue(property, newValue);

    return prefix + "with " + property.toLowerCase() + " " + valueStr;
  }

  @Override
  public String buildEditEntireSeriesCommand(String subject,
                                             ZonedDateTime start,
                                             ZonedDateTime end,
                                             String property,
                                             Object newValue) {

    String prefix = CommandStringHelper.buildEditPrefix(
        "series", subject, start, end
    );

    String valueStr = CommandStringHelper.formatPropertyValue(property, newValue);

    return prefix + "with " + property.toLowerCase() + " " + valueStr;
  }


}
