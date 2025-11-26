package calendar.model;




import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for TreeSetEventStorage.
 *
 * <p>This test class validates all functionality of the TreeSetEventStorage class,
 * including adding events, removing events, querying events by date and time range,
 * and retrieving all events. Edge cases and boundary conditions are thoroughly tested.
 */
public class TreeSetEventStorageTest {

  private TreeSetEventStorage storage;
  private Event event1;
  private Event event2;
  private Event event3;
  private EventKey key1;
  private EventKey key2;

  /**
   * Sets up test fixtures before each test method.
   * Creates a fresh TreeSetEventStorage instance and initializes test events
   * with different dates and times for comprehensive testing.
   */
  @Before
  public void setUp() {
    storage = new TreeSetEventStorage();

    ZonedDateTime start1 = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end1 = ZonedDateTime.of(2025, 1, 15, 11, 0, 0, 0, ZoneId.systemDefault());
    event1 = new Event.Builder("Meeting 1", start1, end1)
        .location("Office")
        .build();

    ZonedDateTime start2 = ZonedDateTime.of(2025, 1, 16, 14, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end2 = ZonedDateTime.of(2025, 1, 16, 15, 0, 0, 0, ZoneId.systemDefault());
    event2 = new Event.Builder("Meeting 2", start2, end2)
        .location("Home")
        .build();

    ZonedDateTime start3 = ZonedDateTime.of(2025, 1, 15, 9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end3 = ZonedDateTime.of(2025, 1, 15, 10, 30, 0, 0, ZoneId.systemDefault());
    event3 = new Event.Builder("Meeting 3", start3, end3)
        .location("Conference Room")
        .build();

    key1 = event1.getKey();
    key2 = event2.getKey();
  }


  /**
   * Tests that the TreeSetEventStorage constructor creates an empty storage.
   * Verifies that a newly instantiated storage contains no events.
   */
  @Test
  public void testConstructorCreatesEmptyStorage() {
    TreeSetEventStorage newStorage = new TreeSetEventStorage();
    assertEquals("New storage should be empty", 0, newStorage.getAllEvents().size());
  }


  /**
   * Tests adding a single event to empty storage.
   * Verifies that the method returns true and the event is stored.
   */
  @Test
  public void testAddEventSingleEventReturnsTrue() {
    assertTrue("Adding new event should return true", storage.addEvent(event1));
    assertEquals("Storage should contain 1 event", 1, storage.getAllEvents().size());
  }

  /**
   * Tests adding a duplicate event.
   * Verifies that TreeSet prevents duplicates and returns false when
   * attempting to add an event that already exists.
   */
  @Test
  public void testAddEventDuplicateEventReturnsFalse() {
    storage.addEvent(event1);
    assertFalse("Adding duplicate event should return false", storage.addEvent(event1));
    assertEquals("Storage should still contain 1 event", 1, storage.getAllEvents().size());
  }

  /**
   * Tests that multiple events are maintained in sorted order.
   * Adds events in non-chronological order and verifies that the TreeSet
   * maintains proper sorting based on the Event.compareTo() implementation.
   */
  @Test
  public void testAddEventMultipleEventsMaintainsSortedOrder() {
    storage.addEvent(event2); // Later event
    storage.addEvent(event1); // Earlier event

    List<Event> events = storage.getAllEvents();
    assertEquals(2, events.size());
    // Verify events are sorted (assuming Event.compareTo sorts by start time)
    assertTrue(events.get(0).getStart().isBefore(events.get(1).getStart())
        ||
        events.get(0).getStart().equals(events.get(1).getStart()));
  }


  /**
   * Tests removing an existing event from storage.
   * Verifies that the method returns true and the event is successfully removed.
   */
  @Test
  public void testRemoveEventExistingEventReturnsTrue() {
    storage.addEvent(event1);
    assertTrue("Removing existing event should return true", storage.removeEvent(key1));
    assertEquals("Storage should be empty after removal", 0, storage.getAllEvents().size());
  }

  /**
   * Tests attempting to remove a non-existent event.
   * Verifies that the method returns false and storage remains unchanged.
   */
  @Test
  public void testRemoveEventNonExistentEventReturnsFalse() {
    storage.addEvent(event1);
    assertFalse("Removing non-existent event should return false", storage.removeEvent(key2));
    assertEquals("Storage should still contain 1 event", 1, storage.getAllEvents().size());
  }

  /**
   * Tests removing an event from empty storage.
   * Verifies that the method handles empty storage gracefully and returns false.
   */
  @Test
  public void testRemoveEventFromEmptyStorageReturnsFalse() {
    assertFalse("Removing from empty storage should return false", storage.removeEvent(key1));
  }

  /**
   * Tests removing one event from storage containing multiple events.
   * Verifies that only the specified event is removed and others remain intact.
   */
  @Test
  public void testRemoveEventOneOfMultipleRemovesCorrectEvent() {
    storage.addEvent(event1);
    storage.addEvent(event2);

    assertTrue(storage.removeEvent(key1));
    assertEquals(1, storage.getAllEvents().size());
    assertFalse(storage.getAllEvents().contains(event1));
    assertTrue(storage.getAllEvents().contains(event2));
  }


  /**
   * Tests querying for events on a date with no events.
   * Verifies that an empty list is returned when no events occur on the specified date.
   */
  @Test
  public void testGetEventsOnNoEventsOnDateReturnsEmptyList() {
    storage.addEvent(event1);
    LocalDate differentDate = LocalDate.of(2025, 2, 1);

    List<Event> events = storage.getEventsOn(differentDate);
    assertNotNull(events);
    assertEquals("Should return empty list for date with no events", 0, events.size());
  }

  /**
   * Tests querying for events on a date with a single event.
   * Verifies that the correct event is returned for the specified date.
   */
  @Test
  public void testGetEventsOnSingleEventOnDateReturnsEvent() {
    storage.addEvent(event1);
    LocalDate date = event1.getStart().toLocalDate();

    List<Event> events = storage.getEventsOn(date);
    assertEquals(1, events.size());
    assertTrue(events.contains(event1));
  }

  /**
   * Tests querying for events on a date with multiple events.
   * Verifies that all events occurring on the specified date are returned,
   * and events on other dates are excluded.
   */
  @Test
  public void testGetEventsOnMultipleEventsOnDateReturnsAllEvents() {
    storage.addEvent(event1);
    storage.addEvent(event3); // Same date as event1
    storage.addEvent(event2); // Different date

    LocalDate date = event1.getStart().toLocalDate();
    List<Event> events = storage.getEventsOn(date);

    assertEquals(2, events.size());
    assertTrue(events.contains(event1));
    assertTrue(events.contains(event3));
    assertFalse(events.contains(event2));
  }

  /**
   * Tests querying for events from empty storage.
   * Verifies that an empty list is returned when storage contains no events.
   */
  @Test
  public void testGetEventsOnEmptyStorageReturnsEmptyList() {
    LocalDate date = LocalDate.of(2025, 1, 15);
    List<Event> events = storage.getEventsOn(date);

    assertNotNull(events);
    assertEquals(0, events.size());
  }


  /**
   * Tests querying for events in a time range with no overlapping events.
   * Verifies that an empty list is returned when no events overlap the specified range.
   */
  @Test
  public void testGetEventsBetweenNoOverlappingEventsReturnsEmptyList() {
    storage.addEvent(event1);

    ZonedDateTime start = ZonedDateTime.of(2025, 2, 1, 10, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = ZonedDateTime.of(2025, 2, 1, 11, 0, 0, 0, ZoneId.systemDefault());

    List<Event> events = storage.getEventsBetween(start, end);
    assertEquals(0, events.size());
  }

  /**
   * Tests querying for events in a time range with a single overlapping event.
   * Verifies that the overlapping event is correctly identified and returned.
   */
  @Test
  public void testGetEventsBetweenSingleOverlappingEventReturnsEvent() {
    storage.addEvent(event1);

    ZonedDateTime start = event1.getStart().minusMinutes(30);
    ZonedDateTime end = event1.getStart().plusMinutes(30);

    List<Event> events = storage.getEventsBetween(start, end);
    assertEquals(1, events.size());
    assertTrue(events.contains(event1));
  }

  /**
   * Tests querying for events in a time range with multiple overlapping events.
   * Verifies that all events overlapping the specified range are returned,
   * and events outside the range are excluded.
   */
  @Test
  public void testGetEventsBetweenMultipleOverlappingEventsReturnsAllEvents() {
    storage.addEvent(event1);
    storage.addEvent(event2);
    storage.addEvent(event3);

    ZonedDateTime start = ZonedDateTime.of(2025, 1, 15, 8, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = ZonedDateTime.of(2025, 1, 15, 12, 0, 0, 0, ZoneId.systemDefault());

    List<Event> events = storage.getEventsBetween(start, end);
    assertEquals(2, events.size());
    assertTrue(events.contains(event1));
    assertTrue(events.contains(event3));
    assertFalse(events.contains(event2));
  }

  /**
   * Tests querying for events in a time range from empty storage.
   * Verifies that an empty list is returned when storage contains no events.
   */
  @Test
  public void testGetEventsBetweenEmptyStorageReturnsEmptyList() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    List<Event> events = storage.getEventsBetween(start, end);
    assertNotNull(events);
    assertEquals(0, events.size());
  }

  /**
   * Tests querying for events with exact time match.
   * Verifies that an event is returned when the query range exactly matches
   * the event's start and end times.
   */
  @Test
  public void testGetEventsBetweenExactTimeMatchReturnsEvent() {
    storage.addEvent(event1);

    List<Event> events = storage.getEventsBetween(event1.getStart(), event1.getEnd());
    assertEquals(1, events.size());
    assertTrue(events.contains(event1));
  }


  /**
   * Tests retrieving all events from empty storage.
   * Verifies that an empty list is returned when no events are stored.
   */
  @Test
  public void testGetAllEventsEmptyStorageReturnsEmptyList() {
    List<Event> events = storage.getAllEvents();
    assertNotNull(events);
    assertEquals(0, events.size());
  }

  /**
   * Tests retrieving all events from storage containing multiple events.
   * Verifies that all stored events are returned in the list.
   */
  @Test
  public void testGetAllEventsWithEventsReturnsAllEvents() {
    storage.addEvent(event1);
    storage.addEvent(event2);
    storage.addEvent(event3);

    List<Event> events = storage.getAllEvents();
    assertEquals(3, events.size());
    assertTrue(events.contains(event1));
    assertTrue(events.contains(event2));
    assertTrue(events.contains(event3));
  }

  /**
   * Tests that getAllEvents returns a new list instance each time.
   * Verifies defensive copying to prevent external modification of internal storage.
   */
  @Test
  public void testGetAllEventsReturnsNewListInstance() {
    storage.addEvent(event1);

    List<Event> events1 = storage.getAllEvents();
    List<Event> events2 = storage.getAllEvents();

    assertNotSame("Each call should return a new list instance", events1, events2);
  }

  /**
   * Tests that modifying the returned list does not affect storage.
   * Verifies that the defensive copy prevents external modification
   * of the internal event collection.
   */
  @Test
  public void testGetAllEventsModifyingReturnedListDoesNotAffectStorage() {
    storage.addEvent(event1);

    List<Event> events = storage.getAllEvents();
    events.clear(); // Modify returned list

    assertEquals("Storage should still contain event", 1, storage.getAllEvents().size());
  }

  /**
   * Tests a complex workflow involving multiple operations.
   * This integration test adds multiple events, removes some, and queries
   * the remaining events to verify that all operations work correctly together.
   */
  @Test
  public void testComplexWorkflowAddMultipleRemoveSomeQueryRest() {
    // Add multiple events
    storage.addEvent(event1);
    storage.addEvent(event2);
    storage.addEvent(event3);
    assertEquals(3, storage.getAllEvents().size());

    // Remove one event
    storage.removeEvent(key1);
    assertEquals(2, storage.getAllEvents().size());

    // Query remaining events
    LocalDate date = event3.getStart().toLocalDate();
    List<Event> eventsOnDate = storage.getEventsOn(date);
    assertEquals(1, eventsOnDate.size());
    assertTrue(eventsOnDate.contains(event3));
  }

  @Test
  public void testRemoveEventRemovesMatchingEvent() {
    InMemoryEventStorage storage = new InMemoryEventStorage();

    ZonedDateTime s = ZonedDateTime.now();
    ZonedDateTime e = s.plusHours(1);

    Event event = new Event.Builder("A", s, e).build();
    storage.addEvent(event);

    boolean removed = storage.removeEvent(event.getKey());

    assertTrue("Event should be removed", removed);
    assertEquals("Storage should now be empty", 0, storage.getAllEvents().size());
  }

  @Test
  public void testRemoveEventDoesNotRemoveWhenKeyDoesNotMatch() {
    InMemoryEventStorage storage = new InMemoryEventStorage();

    ZonedDateTime s = ZonedDateTime.now();
    ZonedDateTime e = s.plusHours(1);

    Event event = new Event.Builder("A", s, e).build();
    storage.addEvent(event);

    EventKey fakeKey = new EventKey("B", s.plusHours(3), e.plusHours(3));

    boolean removed = storage.removeEvent(fakeKey);

    assertFalse("No event should be removed", removed);
    assertEquals("Storage should still contain original event", 1, storage.getAllEvents().size());
  }

  @Test
  public void testRemoveEventReturnTrueOnlyWhenRemoved() {
    InMemoryEventStorage storage = new InMemoryEventStorage();

    ZonedDateTime s = ZonedDateTime.now();
    ZonedDateTime e = s.plusHours(1);

    Event event = new Event.Builder("A", s, e).build();
    storage.addEvent(event);

    assertTrue("Should return true when removed", storage.removeEvent(event.getKey()));

    EventKey fakeKey = new EventKey("A", s.plusDays(1), e.plusDays(1));
    assertFalse("Should return false when nothing removed", storage.removeEvent(fakeKey));
  }

  @Test
  public void testRemoveEventReturnFalseWhenStorageEmpty() {
    InMemoryEventStorage storage = new InMemoryEventStorage();

    EventKey key = new EventKey("A", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1));

    assertFalse("Empty storage cannot remove anything", storage.removeEvent(key));
  }


}