package calendar.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.time.ZoneId;
import org.junit.Test;

/**
 * Test suite for {@link EventCopierFactory}.
 *
 * <p>Ensures proper copier creation, error handling, and utility class protection.</p>
 */
public class EventCopierFactoryTest {

  /** Verifies that a valid copier is created for a non-null calendar. */
  @Test
  public void testCreateCopierWithValidCalendarReturnsValidCopier() {
    Icalendar sourceCalendar = new CalendarModel(new InMemoryEventStorage(),
        ZoneId.of("America/New_York"));

    IeventCopier copier = EventCopierFactory.createCopier(sourceCalendar);

    assertNotNull("Copier should not be null", copier);
    assertTrue("Copier should be an instance of IeventCopier", copier instanceof IeventCopier);
  }

  /** Ensures each call returns a new copier instance. */
  @Test
  public void testCreateCopierReturnsNewInstanceEachTime() {
    Icalendar sourceCalendar = new CalendarModel(new InMemoryEventStorage(),
        ZoneId.of("America/New_York"));

    IeventCopier copier1 = EventCopierFactory.createCopier(sourceCalendar);
    IeventCopier copier2 = EventCopierFactory.createCopier(sourceCalendar);

    assertNotSame("Each call should return a new copier instance", copier1, copier2);
  }

  /** Ensures createCopier throws IllegalArgumentException for null input. */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCopierWithNullCalendarThrowsException() {
    EventCopierFactory.createCopier(null);
  }

  /** Verifies exception message when null calendar is passed. */
  @Test
  public void testCreateCopierWithNullCalendarThrowsExceptionWithCorrectMessage() {
    try {
      EventCopierFactory.createCopier(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Source calendar cannot be null.", e.getMessage());
    }
  }

  /** Verifies copier creation works for multiple calendar instances. */
  @Test
  public void testCreateCopierWithDifferentCalendarImplementations() {
    Icalendar calendar1 = new CalendarModel(new InMemoryEventStorage(),
        ZoneId.of("America/New_York"));
    Icalendar calendar2 = new CalendarModel(new InMemoryEventStorage(),
        ZoneId.of("Asia/Kolkata"));

    IeventCopier copier1 = EventCopierFactory.createCopier(calendar1);
    IeventCopier copier2 = EventCopierFactory.createCopier(calendar2);

    assertNotNull("First copier should not be null", copier1);
    assertNotNull("Second copier should not be null", copier2);
    assertNotSame("Copiers for different calendars should be different", copier1, copier2);
  }

  /** Ensures constructor is private and throws AssertionError via reflection. */
  @Test
  public void testConstructorThrowsAssertionError() {
    try {
      Constructor<EventCopierFactory> constructor =
          EventCopierFactory.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Expected AssertionError");
    } catch (Exception e) {
      Throwable cause = e.getCause();
      assertTrue(cause instanceof AssertionError);
      assertEquals("Utility class should not be instantiated", cause.getMessage());
    }
  }

  /** Confirms the factory returns an EventCopier implementation. */
  @Test
  public void testCreateCopierReturnsEventCopierInstance() {
    Icalendar sourceCalendar = new CalendarModel(new InMemoryEventStorage(),
        ZoneId.of("America/New_York"));

    IeventCopier copier = EventCopierFactory.createCopier(sourceCalendar);
    assertTrue("Copier should be an instance of EventCopier", copier instanceof EventCopier);
  }
}
