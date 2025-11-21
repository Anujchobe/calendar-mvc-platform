package calendar.controller;

import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Command that prints "busy" or "available" at a given date/time.
 * Expected args passed from ShowDispatch:
 *   ["on", "dateTime"]
 */
public class ShowStatusCommand extends AbstractCommand {

  /**
   * Constructs the command.
   *
   * @param args ["on", "dateTime"]
   */
  public ShowStatusCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null.");
    }

    Icalendar model = manager.getActiveCalendar();
    if (model == null) {
      throw new IllegalStateException(
          "No active calendar selected. Use 'use calendar --name <name>' first.");
    }

    ensureArgCountAtLeast(2, "Usage: show status on <dateTime>");

    if (!"on".equalsIgnoreCase(args.get(0))) {
      throw new IllegalArgumentException("Usage: show status on <dateTime>");
    }

    String dateTime = args.get(1);

    ZonedDateTime ts;
    try {
      ts = ParseUtils.parseDateTimeEst(dateTime);
    } catch (DateTimeParseException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + dateTime);
    }

    boolean busy = model.isBusy(ts);
    System.out.println(busy ? "busy" : "available");
  }
}
