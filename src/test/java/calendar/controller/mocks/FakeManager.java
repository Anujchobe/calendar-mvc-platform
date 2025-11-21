package calendar.controller.mocks;

import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight fake implementation of {@link IcalendarManager} for controller testing.
 * Avoids persistence and file I/O by storing calendars in memory.
 */
public class FakeManager implements IcalendarManager {

  private final Map<String, Icalendar> calendars = new HashMap<>();
  private Icalendar active;

  /**
   * Constructs the fake manager.
   * Pre-populates it with dummy calendars: "Work", "Personal", and "Travel".
   * Sets "Work" as the initial active calendar.
   */
  public FakeManager() {
    calendars.put("Work", new FakeCalendar());
    calendars.put("Personal", new FakeCalendar());
    calendars.put("Travel", new FakeCalendar());
    active = calendars.get("Work");
  }

  @Override
  public Icalendar getActiveCalendar() {
    return active;
  }

  @Override
  public Icalendar getCalendar(String name) {
    return calendars.get(name);
  }

  @Override
  public void createCalendar(String name, ZoneId zone) {
    calendars.put(name, new FakeCalendar());
  }

  @Override
  public void useCalendar(String name) {
    active = calendars.get(name);
  }

  @Override
  public void editCalendar(String name, String property, String newValue) {
    // No-op for controller tests
  }

  @Override
  public List<String> listCalendars() {
    return new ArrayList<>(calendars.keySet());
  }
}
