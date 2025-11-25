package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of {@link Ifeatures} for GUI testing.
 *
 * <p>This controller performs no real business logic. Instead, it logs
 * each method call so developers can verify correct GUI-to-controller
 * wiring without interacting with the actual model.</p>
 *
 * <p>It maintains a simple in-memory list of calendar names so that
 * calendar switching UI elements behave realistically.</p>
 */
public class MockFeaturesController implements Ifeatures {

  private final List<String> calendars = new ArrayList<>();
  private String activeCalendar = "Default";

  // For atomic edits:
  public String atomicOriginalSubject;
  public ZonedDateTime atomicOriginalStart;
  public ZonedDateTime atomicOriginalEnd;

  public String atomicNewSubject;
  public ZonedDateTime atomicNewStart;
  public ZonedDateTime atomicNewEnd;
  public String atomicNewLocation;
  public String atomicNewDescription;
  public String atomicNewStatus;

  // Last requested date
  public LocalDate lastQueriedDate;

  /**
   * Constructs a mock controller with a default calendar already created.
   */
  public MockFeaturesController() {
    calendars.add("Default");
  }


  @Override
  public void createCalendar(String name, String timezone) {
    System.out.println("[MOCK] createCalendar -> name=" + name + ", tz=" + timezone);
    calendars.add(name);
  }

  @Override
  public void useCalendar(String calendarName) {
    System.out.println("[MOCK] useCalendar -> " + calendarName);
    this.activeCalendar = calendarName;
  }

  @Override
  public List<String> getCalendarNames() {
    System.out.println("[MOCK] getCalendarNames");
    return new ArrayList<>(calendars);
  }

  @Override
  public String getCurrentCalendarName() {
    System.out.println("[MOCK] getCurrentCalendarName");
    return activeCalendar;
  }

  // ============================================================
  // Event Creation
  // ============================================================

  @Override
  public void createSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                                String description, String location, EventStatus status) {
    System.out.println("[MOCK] createSingleEvent -> "
        + subject + " @ " + start + " to " + end
        + " location=" + location + ", status=" + status);
  }

  @Override
  public void createRecurringEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                                   String description, String location, EventStatus status,
                                   String weekdayPattern, Integer occurrences, LocalDate endDate) {
    System.out.println("[MOCK] createRecurringEvent -> " + subject
        + " weekdays=" + weekdayPattern
        + " occurrences=" + occurrences
        + " endDate=" + endDate);
  }

  @Override
  public void editSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                              String property, Object newValue) {
    System.out.println("[MOCK] editSingleEvent -> "
        + "key=(" + subject + ", " + start + ", " + end + ") "
        + "property=" + property + " newValue=" + newValue);
  }

  @Override
  public void editSeriesFromThisOnward(String subject, ZonedDateTime start, ZonedDateTime end,
                                       String property, Object newValue) {
    System.out.println("[MOCK] editSeriesFromThisOnward -> "
        + subject + " from " + start + " property=" + property
        + " newValue=" + newValue);
  }

  @Override
  public void editEntireSeries(String subject, ZonedDateTime start, ZonedDateTime end,
                               String property, Object newValue) {
    System.out.println("[MOCK] editEntireSeries -> "
        + subject + " entire series property=" + property
        + " newValue=" + newValue);
  }


  @Override
  public List<Event> getEventsOn(LocalDate date) {
    System.out.println("[MOCK] getEventsOn -> " + date);

    // Return empty list — GUI should handle empty case.
    return new ArrayList<>();
  }


  @Override
  public void navigateToMonth(YearMonth month) {
    System.out.println("[MOCK] navigateToMonth -> " + month);
  }

  @Override
  public void selectDate(LocalDate date) {
    System.out.println("[MOCK] selectDate -> " + date);
  }

}
