package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import calendar.model.IcalendarManager;
import calendar.view.Icalendarview;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * GUI Features Controller that uses IcommandAdapter to build command strings
 * and passes them through the existing CLI command parsing pipeline.
 *
 * <p><b>Architecture Flow:</b></p>
 * <pre>
 * 1. View fires event via Ifeatures (e.g., createSingleEvent(...))
 * 2. This controller receives parameters
 * 3. Calls IcommandAdapter to build command string
 * 4. Tokenizes command string using CommandUtils
 * 5. Parses using CommandFactory (existing CLI parser)
 * 6. Executes resulting Command object against model
 * </pre>
 *
 * <p><b>Benefits:</b></p>
 * <ul>
 *     <li>100% reuse of CLI command infrastructure</li>
 *     <li>GUI and CLI guaranteed identical behavior</li>
 *     <li>Single source of truth for all operations</li>
 *     <li>Loose coupling via interfaces (follows DIP)</li>
 * </ul>
 *
 * <p><b>SOLID Principles:</b></p>
 * <ul>
 *     <li><b>SRP:</b> Single responsibility - coordinate GUI events to commands</li>
 *     <li><b>OCP:</b> Open for extension (new commands), closed for modification</li>
 *     <li><b>LSP:</b> Implements Ifeatures interface correctly</li>
 *     <li><b>ISP:</b> Depends only on needed interfaces</li>
 *     <li><b>DIP:</b> Depends on abstractions
 *     (IcalendarManager, IcommandAdapter, Icalendarview)</li>
 * </ul>
 */
public class GuiFeaturesController implements Ifeatures {

  private final IcalendarManager manager;
  private final IcommandAdapter commandAdapter;
  private final Icalendarview view;
  private String currentCalendarName;

  /**
   * Constructs the GUI features controller with dependency injection.
   *
   * <p><b>Dependency Inversion:</b> All dependencies are injected as interfaces,
   * not concrete classes, ensuring loose coupling.</p>
   *
   * @param manager        the calendar manager (model interface)
   * @param commandAdapter adapter for building command strings (interface)
   * @param view           the GUI view (interface)
   * @throws NullPointerException if any parameter is null
   */
  public GuiFeaturesController(IcalendarManager manager,
                               IcommandAdapter commandAdapter,
                               Icalendarview view) {
    this.manager = Objects.requireNonNull(manager, "Manager cannot be null");
    this.commandAdapter = Objects.requireNonNull(commandAdapter, "Command adapter cannot be null");
    this.view = Objects.requireNonNull(view, "View cannot be null");

    initializeDefaultCalendar();

    view.setFeatures(this);
    view.refresh();
  }

  /**
   * Ensures at least one calendar exists and is active.
   */
  private void initializeDefaultCalendar() {
    List<String> names = manager.listCalendars();

    if (names.isEmpty()) {

      String cmd = commandAdapter.buildCreateCalendarCommand("Default", "America/New_York");
      executeCommand(cmd);


      String useCmd = commandAdapter.buildUseCalendarCommand("Default");
      executeCommand(useCmd);

      currentCalendarName = "Default";
    } else {
      currentCalendarName = names.get(0);
      String useCmd = commandAdapter.buildUseCalendarCommand(currentCalendarName);
      executeCommand(useCmd);
    }
  }

  /**
   * Executes a command string through the CLI parsing pipeline.
   *
   * <p><b>Process:</b></p>
   * <ol>
   *     <li>Tokenize command string using CommandUtils</li>
   *     <li>Parse tokens into Command object using CommandFactory</li>
   *     <li>Execute command against model</li>
   * </ol>
   *
   * <p>This ensures GUI and CLI use identical execution path.</p>
   *
   * @param commandString CLI-format command string
   * @throws IllegalStateException if command execution fails
   */
  private void executeCommand(String commandString) {
    try {

      List<String> tokens = CommandUtils.tokenize(commandString);


      Command command = CommandFactory.parseCommand(tokens);


      command.execute(manager);

    } catch (ExitCommand.ExitSignal exit) {

      //no-op
    } catch (Exception e) {
      throw new IllegalStateException("Command execution failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void createCalendar(String name, String timezone) {
    try {

      String cmd = commandAdapter.buildCreateCalendarCommand(name, timezone);
      executeCommand(cmd);


      String useCmd = commandAdapter.buildUseCalendarCommand(name);
      executeCommand(useCmd);

      currentCalendarName = name;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create calendar: " + e.getMessage(), e);
    }
  }

  @Override
  public void useCalendar(String calendarName) {
    try {

      String cmd = commandAdapter.buildUseCalendarCommand(calendarName);
      executeCommand(cmd);

      currentCalendarName = calendarName;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to switch calendar: " + e.getMessage(), e);
    }
  }

  @Override
  public List<String> getCalendarNames() {
    return manager.listCalendars();
  }

  @Override
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  @Override
  public void createSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                                String description, String location, EventStatus status) {
    try {

      String cmd = commandAdapter.buildCreateSingleEventCommand(
          subject, start, end, description, location, status
      );


      executeCommand(cmd);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create event: " + e.getMessage());
    }
  }

  @Override
  public void createRecurringEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                                   String description, String location, EventStatus status,
                                   String weekdayPattern, Integer occurrences,
                                   LocalDate endDate) {
    try {

      String cmd = commandAdapter.buildCreateRecurringEventCommand(
          subject, start, end, description, location, status,
          weekdayPattern, occurrences, endDate
      );


      executeCommand(cmd);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create recurring event: " + e.getMessage(), e);
    }
  }

  @Override
  public void editSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                              String property, Object newValue) {
    try {

      String cmd = commandAdapter.buildEditSingleEventCommand(
          subject, start, end, property, newValue
      );


      executeCommand(cmd);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to edit event: " + e.getMessage(), e);
    }
  }

  @Override
  public void editSeriesFromThisOnward(String subject, ZonedDateTime start, ZonedDateTime end,
                                       String property, Object newValue) {
    try {

      String cmd = commandAdapter.buildEditSeriesFromThisOnwardCommand(
          subject, start, end, property, newValue
      );


      executeCommand(cmd);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to edit series: " + e.getMessage(), e);
    }
  }

  @Override
  public void editEntireSeries(String subject, ZonedDateTime start, ZonedDateTime end,
                               String property, Object newValue) {
    try {

      String cmd = commandAdapter.buildEditEntireSeriesCommand(
          subject, start, end, property, newValue
      );


      executeCommand(cmd);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to edit entire series: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    try {
      return manager.getActiveCalendar().queryEventsOn(date);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load events: " + e.getMessage(), e);
    }
  }

  @Override
  public void navigateToMonth(YearMonth month) {
    // No-op: View handles month display
  }

  @Override
  public void selectDate(LocalDate date) {
    // No-op: View calls getEventsOn when needed
  }


}