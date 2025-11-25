package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.EditMode;
import calendar.model.Event;
import calendar.model.EventKey;
import calendar.model.EventStatus;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import calendar.model.RecurrenceRule;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 * Tests for GuiFeaturesController focused on controller behavior, not actual model logic.
 * We deliberately use a fake IcommandAdapter that returns very simple
 * commands ("exit" or "invalid") so that CommandFactory + Command execution
 * are cheap and no real command implementations are needed.
 */
public class GuiFeaturesControllerTest {


  /**
   * Simple fake view that records interactions.
   */
  private static class FakeView implements calendar.view.Icalendarview {
    int refreshCount = 0;
    String lastError;
    String lastSuccess;
    calendar.controller.Ifeatures features;

    @Override
    public void display() {
      // not used in tests
    }

    @Override
    public void refresh() {
      refreshCount++;
    }

    @Override
    public void showError(String message) {
      lastError = message;
    }

    @Override
    public void showSuccess(String message) {
      lastSuccess = message;
    }

    @Override
    public void setFeatures(calendar.controller.Ifeatures features) {
      this.features = features;
    }
  }

  /**
   * Very small fake Icalendar that just returns a preconfigured list of events
   * from queryEventsOn.
   */
  private static class FakeCalendar implements Icalendar {

    private final List<Event> events;

    FakeCalendar(List<Event> events) {
      this.events = events;
    }

    @Override
    public void createEvent(Event e) {
      // No-op
    }

    @Override
    public void createSeries(Event e, RecurrenceRule rule) {
      // No-op
    }

    @Override
    public void editEvent(EventKey key, String property, Object newValue) {
      // No-op
    }

    @Override
    public void editSeries(EventKey key, String property, Object newValue, EditMode mode) {
      // No-op
    }

    @Override
    public List<Event> queryEventsOn(LocalDate date) {
      return events;
    }

    @Override
    public List<Event> queryEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      return List.of();
    }

    @Override
    public boolean isBusy(ZonedDateTime timestamp) {
      return false;
    }

    @Override
    public List<Event> getAllEvents() {
      return List.of();
    }

    @Override
    public ZoneId getZone() {
      return null;
    }

    @Override
    public void setZone(ZoneId zone) {
      //no-op
    }
  }

  /**
   * Fake manager that returns a fixed list of calendar names and an optional active calendar.
   */
  private static class FakeManager implements IcalendarManager {

    private final List<String> names;
    private final Icalendar active;

    FakeManager(List<String> names, Icalendar active) {
      this.names = names;
      this.active = active;
    }

    @Override
    public void createCalendar(String name, java.time.ZoneId timezone) {
      // not used because our fake adapter returns "exit"
    }

    @Override
    public void editCalendar(String name, String property, String newValue) {
      // not used
    }

    @Override
    public void useCalendar(String name) {
      // not used because "exit" commands throw ExitSignal and are caught
    }

    @Override
    public Icalendar getActiveCalendar() {
      if (active == null) {
        throw new IllegalStateException("No active calendar");
      }
      return active;
    }

    @Override
    public Icalendar getCalendar(String name) {
      return active;
    }

    @Override
    public List<String> listCalendars() {
      return new ArrayList<>(names);
    }
  }

  /**
   * Fake manager that always throws on getActiveCalendar – used to drive the
   * failure path of getEventsOn.
   */
  private static class ThrowingManager extends FakeManager {
    ThrowingManager() {
      super(Collections.emptyList(), null);
    }

    @Override
    public Icalendar getActiveCalendar() {
      throw new IllegalStateException("boom");
    }
  }

  /**
   * Fake adapter.
   * - normally returns "exit" for all commands (ExitCommand is safe & caught)
   * - can be configured to return "invalid" on specific next call to force errors
   */
  private static class FakeCommandAdapter implements IcommandAdapter {

    boolean failNextCreateCalendar = false;
    boolean failNextCreateSingleEvent = false;
    boolean failNextCreateRecurringEvent = false;
    boolean failNextEditSingle = false;
    boolean failNextEditFromThis = false;
    boolean failNextEditSeries = false;

    @Override
    public String buildCreateCalendarCommand(String name, String timezone) {
      if (failNextCreateCalendar) {
        failNextCreateCalendar = false;
        return "invalid";
      }
      return "exit";
    }

    @Override
    public String buildUseCalendarCommand(String calendarName) {
      return "exit";
    }

    @Override
    public String buildCreateSingleEventCommand(String subject,
                                                ZonedDateTime start,
                                                ZonedDateTime end,
                                                String description,
                                                String location,
                                                EventStatus status) {
      if (failNextCreateSingleEvent) {
        failNextCreateSingleEvent = false;
        return "invalid";
      }
      return "exit";
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
      if (failNextCreateRecurringEvent) {
        failNextCreateRecurringEvent = false;
        return "invalid";
      }
      return "exit";
    }

    @Override
    public String buildEditSingleEventCommand(String subject,
                                              ZonedDateTime start,
                                              ZonedDateTime end,
                                              String property,
                                              Object newValue) {
      if (failNextEditSingle) {
        failNextEditSingle = false;
        return "invalid";
      }
      return "exit";
    }

    @Override
    public String buildEditSeriesFromThisOnwardCommand(String subject,
                                                       ZonedDateTime start,
                                                       ZonedDateTime end,
                                                       String property,
                                                       Object newValue) {
      if (failNextEditFromThis) {
        failNextEditFromThis = false;
        return "invalid";
      }
      return "exit";
    }

    @Override
    public String buildEditEntireSeriesCommand(String subject,
                                               ZonedDateTime start,
                                               ZonedDateTime end,
                                               String property,
                                               Object newValue) {
      if (failNextEditSeries) {
        failNextEditSeries = false;
        return "invalid";
      }
      return "exit";
    }
  }


  private ZonedDateTime est(int y, int m, int d, int h, int min) {
    return ZonedDateTime.of(y, m, d, h, min, 0, 0, ZoneId.of("America/New_York"));
  }


  @Test
  public void testConstructorInitializesDefaultCalendarWhenNoneExist() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    assertEquals("Default", controller.getCurrentCalendarName());
    assertEquals(1, view.refreshCount);
    assertEquals(controller, view.features);
  }

  @Test
  public void testConstructorUsesExistingCalendarWhenPresent() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    List<String> names = Collections.singletonList("Work");
    FakeManager manager = new FakeManager(names, null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    assertEquals("Work", controller.getCurrentCalendarName());
    assertEquals(1, view.refreshCount);
  }

  @Test
  public void testCreateCalendarSuccessPath() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    controller.createCalendar("NewCal", "America/New_York");


    assertEquals("NewCal", controller.getCurrentCalendarName());
    assertEquals(1, view.refreshCount);
    assertTrue(view.lastError == null);
  }

  @Test
  public void testCreateCalendarErrorPathViaInvalidCommand() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    adapter.failNextCreateCalendar = true;


    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controller.createCalendar("Broken", "America/New_York"));

    assertTrue(e.getMessage().startsWith("Failed to create calendar: Command execution failed:"));
    assertEquals(1, view.refreshCount);
    assertTrue(view.lastError == null);
  }

  @Test
  public void testCreateSingleEventSuccessPath() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    controller.createSingleEvent(
        "Meeting",
        est(2025, 1, 15, 9, 0),
        est(2025, 1, 15, 10, 0),
        "desc",
        "loc",
        EventStatus.PUBLIC
    );


    assertEquals(1, view.refreshCount);
    assertTrue(view.lastError == null);
  }

  @Test
  public void testCreateSingleEventErrorPath() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    adapter.failNextCreateSingleEvent = true;


    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controller.createSingleEvent(
            "Meeting",
            est(2025, 1, 15, 9, 0),
            est(2025, 1, 15, 10, 0),
            "desc",
            "loc",
            EventStatus.PUBLIC
        ));

    assertEquals(1, view.refreshCount);
    assertTrue(e.getMessage().startsWith("Failed to create event: Command execution failed:"));
    assertTrue(view.lastError == null);
  }

  @Test
  public void testCreateRecurringEventSuccessAndErrorPaths() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);


    controller.createRecurringEvent(
        "Yoga",
        est(2025, 1, 15, 8, 0),
        est(2025, 1, 15, 9, 0),
        "desc",
        "Gym",
        EventStatus.PRIVATE,
        "MTWRF",
        5,
        null
    );
    assertEquals(1, view.refreshCount);
    assertTrue(view.lastError == null);


    adapter.failNextCreateRecurringEvent = true;

    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controller.createRecurringEvent(
            "Yoga",
            est(2025, 1, 15, 8, 0),
            est(2025, 1, 15, 9, 0),
            "desc",
            "Gym",
            EventStatus.PRIVATE,
            "MTWRF",
            5,
            null
        ));
    assertEquals(1, view.refreshCount);
    assertTrue(e.getMessage().startsWith(
        "Failed to create recurring event: Command execution failed:"));
    assertTrue(view.lastError == null);
  }

  @Test
  public void testEditSingleEventSuccessAndErrorPaths() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);


    controller.editSingleEvent(
        "Meeting",
        est(2025, 1, 15, 9, 0),
        est(2025, 1, 15, 10, 0),
        "location",
        "Zoom"
    );
    assertEquals(1, view.refreshCount);


    adapter.failNextEditSingle = true;

    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controller.editSingleEvent(
            "Meeting",
            est(2025, 1, 15, 9, 0),
            est(2025, 1, 15, 10, 0),
            "location",
            "Zoom"
        ));
    assertEquals(1, view.refreshCount);
    assertTrue(e.getMessage().startsWith("Failed to edit event: Command execution failed:"));
    assertTrue(view.lastError == null);
  }

  @Test
  public void testEditSeriesFromThisOnwardSuccessAndErrorPaths() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);


    controller.editSeriesFromThisOnward(
        "Yoga",
        est(2025, 1, 15, 8, 0),
        est(2025, 1, 15, 9, 0),
        "status",
        "public"
    );
    assertEquals(1, view.refreshCount);


    adapter.failNextEditFromThis = true;

    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controller.editSeriesFromThisOnward(
            "Yoga",
            est(2025, 1, 15, 8, 0),
            est(2025, 1, 15, 9, 0),
            "status",
            "public"
        ));
    assertEquals(1, view.refreshCount);
    assertTrue(e.getMessage().startsWith("Failed to edit series: Command execution failed:"));
    assertTrue(view.lastError == null);
  }

  @Test
  public void testEditEntireSeriesSuccessAndErrorPaths() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);


    controller.editEntireSeries(
        "Yoga",
        est(2025, 1, 15, 8, 0),
        est(2025, 1, 15, 9, 0),
        "subject",
        "New Title"
    );
    assertEquals(1, view.refreshCount);


    adapter.failNextEditSeries = true;

    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controller.editEntireSeries(
            "Yoga",
            est(2025, 1, 15, 8, 0),
            est(2025, 1, 15, 9, 0),
            "subject",
            "New Title"
        ));
    assertEquals(1, view.refreshCount);
    assertTrue(e.getMessage().startsWith(
        "Failed to edit entire series: Command execution failed:"));
    assertTrue(view.lastError == null);
  }

  @Test
  public void testGetEventsOnSuccessAndFailurePaths() {

    Event sample = new Event.Builder(
        "Meeting",
        est(2025, 1, 15, 9, 0),
        est(2025, 1, 15, 10, 0)
    ).description("desc").location("Zoom").status(EventStatus.PUBLIC).build();

    List<Event> events = Collections.singletonList(sample);
    FakeCalendar cal = new FakeCalendar(events);

    FakeView viewSuccess = new FakeView();
    FakeCommandAdapter adapterSuccess = new FakeCommandAdapter();
    FakeManager managerSuccess = new FakeManager(Collections.singletonList("Work"), cal);

    GuiFeaturesController controllerSuccess =
        new GuiFeaturesController(managerSuccess, adapterSuccess, viewSuccess);

    List<Event> result = controllerSuccess.getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(1, result.size());
    assertEquals("Meeting", result.get(0).getSubject());
    assertTrue(viewSuccess.lastError == null);


    FakeView viewFail = new FakeView();
    FakeCommandAdapter adapterFail = new FakeCommandAdapter();
    ThrowingManager throwingManager = new ThrowingManager();

    GuiFeaturesController controllerFail =
        new GuiFeaturesController(throwingManager, adapterFail, viewFail);


    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> controllerFail.getEventsOn(LocalDate.of(2025, 1, 15)));

    assertTrue(e.getMessage().startsWith("Failed to load events: boom"));
    assertTrue(viewFail.lastError == null); // showError is not called
  }

  @Test
  public void testNavigateToMonthAndSelectDateNoOp() {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    controller.navigateToMonth(YearMonth.of(2025, 1));
    controller.selectDate(LocalDate.of(2025, 1, 15));

    assertEquals(1, view.refreshCount);
  }

  @Test
  public void testExecuteCommandNormalExitAndInvalidCommandViaReflection() throws Exception {
    FakeView view = new FakeView();
    FakeCommandAdapter adapter = new FakeCommandAdapter();
    FakeManager manager = new FakeManager(Collections.emptyList(), null);

    GuiFeaturesController controller =
        new GuiFeaturesController(manager, adapter, view);

    Method m = GuiFeaturesController.class.getDeclaredMethod("executeCommand", String.class);
    m.setAccessible(true);


    m.invoke(controller, "exit");

    try {
      m.invoke(controller, "foobar");
      throw new AssertionError("Expected IllegalStateException");
    } catch (InvocationTargetException ite) {
      Throwable cause = ite.getCause();
      assertTrue(cause instanceof IllegalStateException);
      assertTrue(cause.getMessage().startsWith("Command execution failed: Unknown command"));
    }
  }
}