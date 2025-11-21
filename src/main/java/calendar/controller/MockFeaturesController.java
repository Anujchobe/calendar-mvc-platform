package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * A mock implementation of {@link Ifeatures} used exclusively for GUI development.
 *
 * <p>This class allows the View to be fully implemented and tested without the
 * actual model, controller logic, or persistence layer. All methods simply log
 * their invocation and simulate predictable data responses.</p>
 *
 * <p><b>Key benefits:</b></p>
 * <ul>
 *     <li>No dependency on model code during GUI development</li>
 *     <li>No exceptions unless input is blatantly invalid</li>
 *     <li>Fast prototyping of view interactions</li>
 *     <li>Consistent return values for rendering UI components</li>
 * </ul>
 *
 * <p>This class is intended to be replaced with a real controller implementation
 * once the GUI is completed.</p>
 */
public class MockFeaturesController implements Ifeatures {

  /**
   * In-memory list of calendar names used to fake multiple calendars.
   */
  private final List<String> calendars = new ArrayList<>();

  /**
   * Tracks the currently active calendar name.
   */
  private String activeCalendar = "Default";

  /**
   * Constructs the mock controller and seeds it with sample calendar names.
   */
  public MockFeaturesController() {
    calendars.add("Default");
    calendars.add("Work");
    calendars.add("Personal");
  }


  /**
   * {@inheritDoc}
   *
   * <p>This mock implementation simply adds the calendar name to a list and prints a log.</p>
   */
  @Override
  public void createCalendar(String name, String timezone) {
    calendars.add(name);
    this.activeCalendar = name;
    System.out.println("MOCK → Created calendar: " + name + " [" + timezone + "]");
  }

  /**
   * {@inheritDoc}
   *
   * <p>This mock implementation switches the active calendar and logs this change.</p>
   */
  @Override
  public void useCalendar(String name) {
    this.activeCalendar = name;
    System.out.println("MOCK → Switched to calendar: " + name);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The mock version returns a shallow copy to protect internal state.</p>
   */
  @Override
  public List<String> getCalendarNames() {
    return new ArrayList<>(calendars);
  }


  /**
   * {@inheritDoc}
   *
   * <p>The mock implementation logs the event parameters and performs no validation.</p>
   */
  @Override
  public void createSingleEvent(
      String subject,
      ZonedDateTime start,
      ZonedDateTime end,
      String description,
      String location,
      EventStatus status
  ) {
    System.out.println("MOCK → Created single event: " + subject);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This mock implementation prints the creation details but does not
   * generate or store any recurring entries.</p>
   */
  @Override
  public void createRecurringEvent(
      String subject,
      ZonedDateTime start,
      ZonedDateTime end,
      String description,
      String location,
      EventStatus status,
      String weekdayPattern,
      Integer occurrences,
      LocalDate until
  ) {
    System.out.println("MOCK → Created recurring event: "
        + subject + " on pattern " + weekdayPattern);
  }


  /**
   * {@inheritDoc}
   *
   * <p>Only logs the action; the event is not actually modified.</p>
   */
  @Override
  public void editSingleEvent(
      String subject,
      ZonedDateTime start,
      String property,
      Object newValue
  ) {
    System.out.println("MOCK → Edited 1 event: " + subject);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Logs that a series-from-this-onward edit was invoked.</p>
   */
  @Override
  public void editSeriesFromThisOnward(
      String subject,
      ZonedDateTime start,
      String property,
      Object newValue
  ) {
    System.out.println("MOCK → Edited future series: " + subject);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Logs that the entire series was edited.</p>
   */
  @Override
  public void editEntireSeries(
      String subject,
      ZonedDateTime start,
      String property,
      Object newValue
  ) {
    System.out.println("MOCK → Edited entire series: " + subject);
  }


  /**
   * {@inheritDoc}
   *
   * <p>This mock implementation returns a single hardcoded example event so that
   * the GUI can render event lists and daily event dialogs.</p>
   *
   * @return a list containing a single mock {@link Event}
   */
  @Override
  public List<Event> getEventsOn(LocalDate date) {
    List<Event> out = new ArrayList<>();

    ZonedDateTime s = date.atTime(10, 0).atZone(ZoneId.systemDefault());
    ZonedDateTime e = date.atTime(11, 0).atZone(ZoneId.systemDefault());

    Event event = new Event.Builder("Mock Event", s, e)
        .location("Meeting Room")
        .description("Example event for UI testing")
        .status(EventStatus.PUBLIC)
        .build();

    out.add(event);
    return out;
  }


  /**
   * {@inheritDoc}
   *
   * <p>Logs the target month. The real controller would recalculate
   * visible events or update internal UI state.</p>
   */
  @Override
  public void navigateToMonth(YearMonth month) {
    System.out.println("MOCK → Navigated to " + month);
  }
}
