package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for the {@link calendar.model.CalendarModel} class.
 *
 * <p>This test suite verifies core calendar operations such as creating,
 * editing, querying, and exporting events. It uses in-memory fake
 * implementations of {@link IeventStorage} and {@link Iexporter} to isolate
 * the model logic from external dependencies.</p>
 */

public class CalendarModelTest {
  private CalendarModel model;
  private FakeStorage storage;
  private ZonedDateTime baseStart;
  private ZonedDateTime baseEnd;
  private Event baseEvent;

  /**
   * Initializes the test environment before each test case.
   *
   * <p>Creates fresh instances of {@link FakeStorage}, {@link FakeExporter},
   * and {@link CalendarModel} to ensure isolation and prevent test
   * interference between runs.</p>
   */

  @Before
  public void setup() {
    storage = new FakeStorage();
    model = new CalendarModel(storage, ZoneId.of("America/New_York"));
    baseStart = ZonedDateTime.of(2025, 5, 5, 10, 0, 0, 0, ZoneId.of("America/New_York"));
    baseEnd = baseStart.plusHours(1);
    baseEvent = new Event.Builder("SeriesSubj", baseStart, baseEnd)
        .description("desc")
        .location("loc")
        .status(EventStatus.PUBLIC)
        .seriesId("series1")
        .build();


  }

  static class FakeStorage implements IeventStorage {
    List<Event> events = new ArrayList<>();

    @Override
    public boolean addEvent(Event e) {
      for (Event ev : events) {
        if (ev.getSubject().equals(e.getSubject())
            &&
            ev.getStart().equals(e.getStart())
            &&
            ev.getEnd().equals(e.getEnd())) {
          return false;
        }
      }
      events.add(e);
      return true;
    }

    @Override
    public boolean removeEvent(EventKey key) {
      return events.removeIf(e -> e.matchesKey(key));
    }

    @Override
    public List<Event> getAllEvents() {
      return new ArrayList<>(events);
    }

    @Override
    public List<Event> getEventsOn(LocalDate d) {
      return events.stream()
          .filter(e -> e.occursOn(d))
          .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsBetween(ZonedDateTime s, ZonedDateTime e) {
      return events.stream()
          .filter(ev -> ev.overlaps(s, e))
          .collect(Collectors.toList());
    }
  }


  private Event event(String subj) {
    ZonedDateTime now = ZonedDateTime.now();
    return new Event.Builder(subj, now, now.plusHours(1))
        .description("desc")
        .location("loc")
        .status(EventStatus.PUBLIC)
        .seriesId("series")
        .build();
  }

  @Test
  public void testConstructorDefaultsToEst() {
    CalendarModel model2 = new CalendarModel(storage, null);
    assertNotNull(model2);
  }

  @Test
  public void testCreateEventSuccess() {
    model.createEvent(event("Meeting"));
    assertEquals(1, storage.getAllEvents().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventDuplicateThrows() {
    Event e = event("A");
    model.createEvent(e);
    model.createEvent(e);
  }

  @Test
  public void testCreateSeriesSuccess() {
    Set<DayOfWeek> days = EnumSet.of(LocalDate.now().getDayOfWeek());
    RecurrenceRule rule = new RecurrenceRule(days, 2, null);
    model.createSeries(event("Daily"), rule);
    assertTrue(storage.getAllEvents().size() >= 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesDuplicateThrows() {
    Event e = event("Dup");
    storage.addEvent(e);
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(LocalDate.now().getDayOfWeek()), 1, null);
    model.createSeries(e, rule);
  }

  @Test
  public void testEditEventSuccess() {
    Event e = event("EditMe");
    model.createEvent(e);
    model.editEvent(e.getKey(), "subject", "NewSubj");
    assertEquals("NewSubj", storage.getAllEvents().get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventNotFound() {
    model.editEvent(new EventKey("X", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1)),
        "subject", "Y");
  }

  /**
   * Verifies that when editing a series with {@link EditMode#ENTIRE_SERIES},
   * all events sharing the same seriesId are updated.
   */
  @Test
  public void testEditSeriesEntireSeriesUpdatesAllWithSameSeriesId() {
    Event e1 = new Event.Builder("SeriesSubj", baseStart, baseEnd)
        .description("d1").location("loc1")
        .status(EventStatus.PUBLIC).seriesId("SAME_SERIES").build();

    Event e2 = new Event.Builder("SeriesSubj", baseStart.plusDays(1), baseEnd.plusDays(1))
        .description("d2").location("loc2")
        .status(EventStatus.PUBLIC).seriesId("SAME_SERIES").build();


    storage.addEvent(e1);
    storage.addEvent(e2);


    model.editSeries(e1.getKey(), "location", "newloc", EditMode.ENTIRE_SERIES);


    List<Event> all = storage.getAllEvents();
    assertTrue(all.stream()
        .filter(e -> "SAME_SERIES".equals(e.getSeriesId()))
        .allMatch(e -> "newloc".equals(e.getLocation())));
  }


  @Test
  public void testQueryOnAndBetweenAndBusy() {
    Event e = event("X");
    storage.addEvent(e);
    assertEquals(1, model.queryEventsOn(e.getStart().toLocalDate()).size());
    assertEquals(1, model.queryEventsBetween(e.getStart(), e.getEnd()).size());
    assertTrue(model.isBusy(e.getStart().plusMinutes(10)));
    assertFalse(model.isBusy(e.getEnd().plusHours(2)));
  }


  /**
   * Tests SINGLE mode where event matches the exact key.
   */
  @Test
  public void testShouldModifySingleModeMatchAndNoMatch() {
    Event event = new Event.Builder("Yoga", baseStart, baseEnd)
        .status(EventStatus.PUBLIC)
        .seriesId("Yoga")
        .build();
    EventKey matchingKey = event.getKey();
    assertTrue(event.shouldModifyAccordingToMode(matchingKey, EditMode.SINGLE));

    EventKey differentKey = new EventKey("Yoga", baseStart.plusDays(1), baseEnd.plusDays(1));
    assertFalse(event.shouldModifyAccordingToMode(differentKey, EditMode.SINGLE));
  }

  /**
   * Tests FROM_THIS_ONWARD mode where events should be modified if
   * same series and start is >= key.start.
   */
  @Test
  public void testShouldModifyFromThisOnward() {
    Event event = new Event.Builder("Yoga", baseStart.plusDays(2), baseEnd.plusDays(2))
        .status(EventStatus.PUBLIC)
        .seriesId("Yoga").build();
    EventKey key = new EventKey("Yoga", baseStart, baseEnd);

    assertTrue(event.shouldModifyAccordingToMode(key, EditMode.FROM_THIS_ONWARD));

    Event earlier = new Event.Builder("Yoga", baseStart.minusDays(1), baseEnd.minusDays(1))
        .status(EventStatus.PUBLIC).seriesId("Yoga").build();
    assertFalse(earlier.shouldModifyAccordingToMode(key, EditMode.FROM_THIS_ONWARD));

    Event diffSeries = new Event.Builder("Yoga", baseStart.plusDays(1), baseEnd.plusDays(1))
        .status(EventStatus.PUBLIC).seriesId("Other").build();
    assertFalse(diffSeries.shouldModifyAccordingToMode(key, EditMode.FROM_THIS_ONWARD));

    Event noSeries = new Event.Builder("Yoga", baseStart.plusDays(1), baseEnd.plusDays(1))
        .status(EventStatus.PUBLIC).build();
    assertFalse(noSeries.shouldModifyAccordingToMode(key, EditMode.FROM_THIS_ONWARD));
  }

  /**
   * Tests ENTIRE_SERIES mode where all with same seriesId are modified.
   */
  @Test
  public void testShouldModifyEntireSeries() {
    Event event = new Event.Builder("Yoga", baseStart, baseEnd)
        .status(EventStatus.PUBLIC).seriesId("Yoga").build();
    EventKey key = new EventKey("Yoga", baseStart, baseEnd);
    assertTrue(event.shouldModifyAccordingToMode(key, EditMode.ENTIRE_SERIES));

    Event diff = new Event.Builder("Yoga", baseStart, baseEnd)
        .status(EventStatus.PUBLIC).seriesId("Other").build();
    assertFalse(diff.shouldModifyAccordingToMode(key, EditMode.ENTIRE_SERIES));

    Event noSeries = new Event.Builder("Yoga", baseStart, baseEnd)
        .status(EventStatus.PUBLIC).build();
    assertFalse(noSeries.shouldModifyAccordingToMode(key, EditMode.ENTIRE_SERIES));
  }

  /**
   * Tests null mode branch returns false.
   */
  @Test
  public void testShouldModifyDefaultCase() {
    Event event = new Event.Builder("Yoga", baseStart, baseEnd)
        .status(EventStatus.PUBLIC).seriesId("Yoga").build();
    EventKey key = event.getKey();
    assertFalse(event.shouldModifyAccordingToMode(key, null));
  }

  /**
   * Verifies that an event equals itself (reflexive property of equals()).
   */
  @Test
  public void testEqualsSameObject() {
    Event e =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    assertTrue(e.equals(e));
  }

  /**
   * Verifies that comparing an event to an object of another type returns false.
   */
  @Test
  public void testEqualsDifferentType() {
    Event e =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    assertFalse(e.equals("NotAnEvent"));
  }

  /**
   * Ensures that comparing an event to null returns false.
   */
  @Test
  public void testEqualsNull() {
    Event e =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    assertFalse(e.equals(null));
  }

  /**
   * Confirms that two events with identical subject, start, and end times are equal.
   * Also verifies that their hash codes match for consistency.
   */
  @Test
  public void testEqualsSameValues() {
    Event e1 =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    Event e2 =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    assertTrue(e1.equals(e2));
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  /**
   * Checks that two events with different subjects are not considered equal.
   */
  @Test
  public void testNotEqualsDifferentSubject() {
    Event e1 =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    Event e2 =
        new Event.Builder("Review", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    assertFalse(e1.equals(e2));
  }

  /**
   * Checks that two events with different start times are not equal.
   */
  @Test
  public void testNotEqualsDifferentStartTime() {
    Event e1 =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    ZonedDateTime laterStart = baseStart.plusHours(1);
    Event e2 =
        new Event.Builder("Meeting", laterStart, baseEnd.plusHours(1))
            .status(EventStatus.PUBLIC)
            .build();
    assertFalse(e1.equals(e2));
  }

  /**
   * Checks that two events with different end times are not equal.
   */
  @Test
  public void testNotEqualsDifferentEndTime() {
    Event e1 =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    Event e2 =
        new Event.Builder("Meeting", baseStart, baseEnd.plusHours(2))
            .status(EventStatus.PUBLIC)
            .build();
    assertFalse(e1.equals(e2));
  }

  /**
   * Ensures that hashCode() returns consistent values for the same event object.
   */
  @Test
  public void testHashCodeConsistency() {
    Event e =
        new Event.Builder("Meeting", baseStart, baseEnd)
            .status(EventStatus.PUBLIC)
            .build();
    int hash1 = e.hashCode();
    int hash2 = e.hashCode();
    assertEquals(hash1, hash2);
  }

  /**
   * Verifies that a single event is correctly updated by
   * replacing it with a modified copy.
   */
  @Test
  public void testUpdateSingleEventModifiesEventSuccessfully() {
    storage.addEvent(baseEvent);
    EventKey key = baseEvent.getKey();
    model.editSeries(key, "location", "newloc", EditMode.SINGLE);

    List<Event> result = storage.getAllEvents();
    assertEquals(1, result.size());
    assertEquals("newloc", result.get(0).getLocation());
  }


  /**
   * Ensures non-series events fall back to single edit in FROM_THIS_ONWARD mode.
   */
  @Test
  public void testUpdateFromThisOnwardNonSeriesFallsBackToSingle() {
    Event single =
        new Event.Builder("Solo", baseStart, baseEnd)
            .description("d")
            .location("l")
            .status(EventStatus.PUBLIC)
            .build();
    storage.addEvent(single);
    EventKey key = single.getKey();
    model.editSeries(key, "subject", "Changed", EditMode.FROM_THIS_ONWARD);
    assertTrue(
        storage.getAllEvents().stream().anyMatch(e -> e.getSubject().equals("Changed")));
  }

  /**
   * Covers invalid key case — ensures exception thrown if event not found.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesNoEventFoundthrowsException() {
    EventKey fakeKey = new EventKey("NotExist", baseStart, baseEnd);
    model.editSeries(fakeKey, "subject", "X", EditMode.SINGLE);
  }

  /**
   * Tests that FROM_THIS_ONWARD updates all later events in the same series.
   */
  @Test
  public void testUpdateFromThisOnward_seriesUpdatesFutureEvents() {
    Event e2 =
        new Event.Builder("SeriesSubj", baseStart.plusDays(1), baseEnd.plusDays(1))
            .description("desc")
            .location("loc")
            .status(EventStatus.PUBLIC)
            .seriesId("series1")
            .build();
    Event e3 =
        new Event.Builder("SeriesSubj", baseStart.plusDays(2), baseEnd.plusDays(2))
            .description("desc")
            .location("loc")
            .status(EventStatus.PUBLIC)
            .seriesId("series1")
            .build();

    storage.addEvent(e2);
    storage.addEvent(e3);

    EventKey key = e2.getKey();
    model.editSeries(key, "description", "UpdatedDesc", EditMode.FROM_THIS_ONWARD);

    long count =
        storage.getAllEvents().stream()
            .filter(e -> "UpdatedDesc".equals(e.getDescription()))
            .count();
    assertEquals(2, count);
  }

  /**
   * Tests ENTIRE_SERIES mode updates all events sharing the same seriesId.
   */
  @Test
  public void testUpdateEntireSeriesUpdatesAllInSeries() {
    Event e2 =
        new Event.Builder("SeriesSubj", baseStart.plusDays(1), baseEnd.plusDays(1))
            .description("desc")
            .location("loc")
            .status(EventStatus.PUBLIC)
            .seriesId("series1")
            .build();
    storage.addEvent(e2);

    EventKey key = e2.getKey();
    model.editSeries(key, "status", "PRIVATE", EditMode.ENTIRE_SERIES);
    assertTrue(
        storage.getAllEvents().stream().allMatch(e -> e.getStatus() == EventStatus.PRIVATE));
  }

  /**
   * ENTIRE_SERIES falls back to single update if event has no seriesId.
   */
  @Test
  public void testUpdateEntireSeriesNonSeriesFallbackToSingle() {
    Event nonSeries =
        new Event.Builder("Solo", baseStart, baseEnd)
            .description("desc")
            .location("loc")
            .status(EventStatus.PUBLIC)
            .build();
    storage.addEvent(nonSeries);
    EventKey key = nonSeries.getKey();
    model.editSeries(key, "description", "Modified", EditMode.ENTIRE_SERIES);
    assertEquals("Modified", storage.getAllEvents().get(0).getDescription());
  }

  @Test
  public void testConstructorThrowsWhenSubjectIsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Event.Builder(null, baseStart, baseEnd).status(EventStatus.PUBLIC).build());
  }

  @Test
  public void testConstructorThrowsWhenSubjectIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Event.Builder("   ", baseStart, baseEnd).status(EventStatus.PUBLIC).build());
  }

  @Test
  public void testConstructorThrowsWhenStartIsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Event.Builder("Meeting", null, baseEnd).status(EventStatus.PUBLIC).build());
  }

  @Test
  public void testConstructorThrowsWhenEndBeforeStart() {
    ZonedDateTime endBefore = baseStart.minusHours(1);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Event.Builder("Meeting", baseStart, endBefore)
                .status(EventStatus.PUBLIC)
                .build());
  }

  @Test
  public void testEqualsReturnsTrueForSameValues() {
    EventKey key1 = new EventKey("Meeting", baseStart, baseEnd);
    EventKey key2 = new EventKey("Meeting", baseStart, baseEnd);
    assertEquals(key1, key2);
  }

  @Test
  public void testEqualsIsCaseInsensitiveForSubject() {
    EventKey key1 = new EventKey("MEETING", baseStart, baseEnd);
    EventKey key2 = new EventKey("meeting", baseStart, baseEnd);
    assertEquals(key1, key2);
  }

  @Test
  public void testEqualsReturnsFalseForDifferentStartTimes() {
    EventKey key1 = new EventKey("Meeting", baseStart, baseEnd);
    EventKey key2 = new EventKey("Meeting", baseStart.plusHours(1), baseEnd.plusHours(1));
    assertNotEquals(key1, key2);
  }

  @Test
  public void testEqualsReturnsFalseForDifferentEndTimes() {
    EventKey key1 = new EventKey("Meeting", baseStart, baseEnd);
    EventKey key2 = new EventKey("Meeting", baseStart, baseEnd.plusHours(2));
    assertNotEquals(key1, key2);
  }

  @Test
  public void testEqualsReturnsFalseForDifferentSubject() {
    EventKey key1 = new EventKey("Meeting", baseStart, baseEnd);
    EventKey key2 = new EventKey("Workshop", baseStart, baseEnd);
    assertNotEquals(key1, key2);
  }

  @Test
  public void testEqualsReturnsFalseForDifferentType() {
    EventKey key = new EventKey("Meeting", baseStart, baseEnd);
    String notAkey = "Meeting";
    assertNotEquals(key, notAkey);
  }

  @Test
  public void testEqualsSameReferenceReturnsTrue() {
    EventKey key = new EventKey("Meeting", baseStart, baseEnd);
    assertEquals(key, key);
  }


  @Test
  public void testHashCodeIsConsistentForEqualObjects() {
    EventKey key1 = new EventKey("Meeting", baseStart, baseEnd);
    EventKey key2 = new EventKey("meeting", baseStart, baseEnd);
    assertEquals(key1.hashCode(), key2.hashCode());
  }

  @Test
  public void testHashCodeDiffersForDifferentEvents() {
    EventKey key1 = new EventKey("Meeting", baseStart, baseEnd);
    EventKey key2 = new EventKey("Workshop", baseStart, baseEnd);
    assertNotEquals(key1.hashCode(), key2.hashCode());
  }


  @Test
  public void testToStringFormat() {
    EventKey key = new EventKey("Meeting", baseStart, baseEnd);
    String str = key.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("EventKey["));
    assertTrue(str.contains("→"));
  }

  /**
   * Tests that when allDay is true, the event start and end times
   * are set to 8:00 AM and 5:00 PM respectively.
   */
  @Test
  public void testBuildAllDayEventSetsDefaultTimes() {
    ZonedDateTime baseStart = ZonedDateTime.of(
        2025, 11, 12, 0, 0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime baseEnd = baseStart.plusHours(1);

    Event event = new Event.Builder("Team Meeting", baseStart, baseEnd)
        .allDay(true)
        .build();

    ZonedDateTime expectedStart = baseStart.withHour(8).withMinute(0);
    ZonedDateTime expectedEnd = baseStart.withHour(17).withMinute(0);

    assertEquals(expectedStart, event.getStart());
    assertEquals(expectedEnd, event.getEnd());
  }

  /**
   * Tests that building an Event with a null end time
   * throws an IllegalArgumentException.
   */
  @Test
  public void testBuilderNullEndTimeThrowsException() {
    ZonedDateTime start = ZonedDateTime.now();

    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", start, null);
    });
  }

  /**
   * Tests that copyWith("start", newStart) correctly updates the start time
   * while ensuring the end time remains after the new start.
   */
  @Test
  public void testCopyWithStartPropertyUpdatesStartTime() {
    ZonedDateTime oldStart = ZonedDateTime.now();
    ZonedDateTime oldEnd = oldStart.plusHours(2);
    ZonedDateTime newStart = oldStart.minusHours(1); // earlier, so end > start

    Event original = new Event.Builder("Meeting", oldStart, oldEnd)
        .description("Weekly sync")
        .location("Room A")
        .build();

    Event updated = original.copyWith("start", newStart);

    assertEquals(newStart, updated.getStart());
    assertEquals(oldEnd, updated.getEnd());
    assertEquals("Meeting", updated.getSubject());
    assertEquals("Room A", updated.getLocation());
  }


  /**
   * Tests that copyWith("end", newEnd) correctly updates the end time
   * while keeping the rest of the event properties unchanged.
   */
  @Test
  public void testCopyWithEndPropertyUpdatesEndTime() {
    ZonedDateTime oldStart = ZonedDateTime.now();
    ZonedDateTime oldEnd = oldStart.plusHours(2);
    ZonedDateTime newEnd = oldEnd.plusHours(1);

    Event original = new Event.Builder("Workshop", oldStart, oldEnd)
        .description("Tech session")
        .build();

    Event updated = original.copyWith("end", newEnd);

    assertEquals(newEnd, updated.getEnd());
    assertEquals(oldStart, updated.getStart());
    assertEquals("Workshop", updated.getSubject());
  }


  /**
   * Tests that copyWith("seriesId", newId) correctly updates the seriesId
   * while keeping other properties intact.
   */
  @Test
  public void testCopyWithSeriesIdUpdatesSeriesId() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    Event original = new Event.Builder("Yoga Class", start, end)
        .seriesId("A1")
        .build();

    Event updated = original.copyWith("seriesId", "B2");

    assertEquals("B2", updated.getSeriesId());
    assertEquals("Yoga Class", updated.getSubject());
    assertEquals(start, updated.getStart());
  }


  /**
   * Tests that copyWith() throws an IllegalArgumentException
   * when an unknown property name is passed.
   */
  @Test
  public void testCopyWithUnknownPropertyThrowsException() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    Event original = new Event.Builder("Lunch", start, end).build();

    assertThrows(IllegalArgumentException.class, () -> {
      original.copyWith("invalidProperty", "someValue");
    });
  }

  /**
   * Tests that constructing an EventKey with a null subject
   * throws an IllegalArgumentException.
   */
  @Test
  public void testEventKeyNullSubjectThrowsException() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    assertThrows(IllegalArgumentException.class, () -> {
      new EventKey(null, start, end);
    });
  }

  /**
   * Tests that constructing an EventKey with a blank subject
   * throws an IllegalArgumentException.
   */
  @Test
  public void testEventKeyBlankSubjectThrowsException() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    assertThrows(IllegalArgumentException.class, () -> {
      new EventKey("   ", start, end);
    });
  }

  /**
   * Tests that constructing an EventKey with null start or end times
   * throws an IllegalArgumentException.
   */
  @Test
  public void testEventKeyNullTimesBehavior() {
    ZonedDateTime start = ZonedDateTime.now();

    // Null start SHOULD still throw (invalid)
    assertThrows(IllegalArgumentException.class, () -> {
      new EventKey("Meeting", null, start.plusHours(1));
    });

    // Null end SHOULD NOT throw (valid wildcard for series lookup)
    try {
      new EventKey("Meeting", start, null);
    } catch (Exception e) {
      fail("Expected no exception when end time is null, but got: " + e);
    }
  }

  /**
   * Tests that editing a series from this onward with "start" property
   * splits the series and assigns new seriesIds internally.
   */
  @Test
  public void testEditSeriesSplitsSeriesOnEndProperty() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);
    String originalSeriesId = UUID.randomUUID().toString();

    // Create two events in the same series
    Event anchor = new Event.Builder("Team Meeting", start, end)
        .description("Weekly sync")
        .location("Conference Room")
        .status(EventStatus.PUBLIC)
        .seriesId(originalSeriesId)
        .build();

    Event future = new Event.Builder("Team Meeting",
        start.plusDays(1), end.plusDays(1))
        .description("Weekly sync")
        .location("Conference Room")
        .status(EventStatus.PUBLIC)
        .seriesId(originalSeriesId)
        .build();

    // Use in-memory storage for the model
    IeventStorage storage = new InMemoryEventStorage();
    storage.addEvent(anchor);
    storage.addEvent(future);

    CalendarModel model = new CalendarModel(storage, ZoneId.of("America/New_York"));

    // Act — extend the "end" time of the series onward (triggers shouldSplitSeries = true)
    ZonedDateTime newEnd = end.plusHours(2); // valid end > start
    EventKey anchorKey = anchor.getKey();

    model.editSeries(anchorKey, "end", newEnd, EditMode.FROM_THIS_ONWARD);

    // Assert — at least one event should now have a new seriesId
    List<Event> updatedEvents = storage.getAllEvents();
    boolean newSeriesIdExists = updatedEvents.stream()
        .anyMatch(e -> e.getSeriesId() != null && !e.getSeriesId().equals(originalSeriesId));

    assertTrue("Expected new seriesId assigned after split", newSeriesIdExists);

    // Optional: ensure subject and location are preserved
    for (Event e : updatedEvents) {
      assertEquals("Team Meeting", e.getSubject());
      assertEquals("Conference Room", e.getLocation());
    }
  }

  /**
   * Verifies that getEventsOn() returns only events that occur on the given date.
   */
  @Test
  public void testGetEventsOnToday() {
    ZoneId zone = ZoneId.of("America/New_York");
    InMemoryEventStorage storage
        = new InMemoryEventStorage();
    ZonedDateTime todayStart
        = ZonedDateTime.now(zone).withHour(9).withMinute(0).withSecond(0).withNano(0);
    ZonedDateTime todayEnd = todayStart.plusHours(2);
    ZonedDateTime tomorrowStart = todayStart.plusDays(1);
    ZonedDateTime tomorrowEnd = tomorrowStart.plusHours(1);

    Event meetingToday = new Event.Builder("Team Sync", todayStart, todayEnd)
        .description("Daily meeting")
        .status(EventStatus.PUBLIC)
        .build();

    Event meetingTomorrow = new Event.Builder("Code Review", tomorrowStart, tomorrowEnd)
        .description("Sprint review")
        .status(EventStatus.PRIVATE)
        .build();

    // Multi-day event spanning today and tomorrow
    Event multiDay = new Event.Builder("Hackathon", todayEnd, tomorrowEnd)
        .description("Overnight project")
        .status(EventStatus.PUBLIC)
        .build();

    storage.addEvent(meetingToday);
    storage.addEvent(meetingTomorrow);
    storage.addEvent(multiDay);

    LocalDate today = LocalDate.now(zone);
    List<Event> result = storage.getEventsOn(today);

    assertTrue(result.contains(meetingToday));
    assertTrue(result.contains(multiDay)); // spans into today
    assertFalse(result.contains(meetingTomorrow));
    assertEquals(2, result.size());
  }

  /**
   * Verifies that getEventsOn() correctly finds events on the next day.
   */
  @Test
  public void testGetEventsOnTomorrow() {
    ZoneId zone
        = ZoneId.of("America/New_York");
    InMemoryEventStorage storage
        = new InMemoryEventStorage();
    ZonedDateTime base
        = ZonedDateTime.now(zone).withHour(9).withMinute(0).withSecond(0).withNano(0);

    Event today
        = new Event.Builder("Team Sync", base, base.plusHours(1))
        .status(EventStatus.PUBLIC)
        .build();
    Event tomorrow
        = new Event.Builder("Code Review", base.plusDays(1), base.plusDays(1).plusHours(1))
        .status(EventStatus.PRIVATE)
        .build();
    Event multiDay
        = new Event.Builder("Hackathon", base.plusHours(3), base.plusDays(1).plusHours(2))
        .status(EventStatus.PUBLIC)
        .build();

    storage.addEvent(today);
    storage.addEvent(tomorrow);
    storage.addEvent(multiDay);

    LocalDate tomorrowDate = LocalDate.now(zone).plusDays(1);
    List<Event> result = storage.getEventsOn(tomorrowDate);

    assertTrue(result.contains(tomorrow));
    assertTrue(result.contains(multiDay)); // still ongoing
    assertFalse(result.contains(today));
    assertEquals(2, result.size());
  }

  /**
   * Verifies that getEventsBetween() finds all events overlapping the given range.
   */
  @Test
  public void testGetEventsBetweenFullRange() {
    ZoneId zone = ZoneId.of("America/New_York");
    InMemoryEventStorage storage = new InMemoryEventStorage();
    ZonedDateTime base = ZonedDateTime.now(zone).withHour(8);

    Event a
        =
        new Event.Builder("Meeting A", base, base.plusHours(1)).build();
    Event b
        = new Event.Builder("Meeting B", base.plusDays(1), base.plusDays(1).plusHours(1)).build();
    Event c
        = new Event.Builder("Hackathon", base.plusHours(2), base.plusDays(1).plusHours(3)).build();

    storage.addEvent(a);
    storage.addEvent(b);
    storage.addEvent(c);

    List<Event> result = storage.getEventsBetween(base, base.plusDays(2));
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.contains(c));
    assertEquals(3, result.size());
  }

  /**
   * Verifies that getEventsBetween() excludes events completely outside the range.
   */
  @Test
  public void testGetEventsBetweenRestrictedRange() {
    ZoneId zone = ZoneId.of("America/New_York");
    InMemoryEventStorage storage = new InMemoryEventStorage();
    ZonedDateTime base = ZonedDateTime.now(zone).withHour(8);

    Event inside = new Event.Builder("Morning Sync", base, base.plusHours(1)).build();
    Event outside = new Event.Builder("Evening Call", base.plusHours(6), base.plusHours(7)).build();

    storage.addEvent(inside);
    storage.addEvent(outside);

    ZonedDateTime rangeStart = base.minusMinutes(30);
    ZonedDateTime rangeEnd = base.plusHours(2);
    List<Event> result = storage.getEventsBetween(rangeStart, rangeEnd);

    assertTrue(result.contains(inside));
    assertFalse(result.contains(outside));
    assertEquals(1, result.size());
  }



  /**
   * Verifies that providing neither occurrences nor until
   * also triggers an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsWhenBothNull() {
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.TUESDAY);
    // Both null → invalid
    new RecurrenceRule(weekdays, null, null);
  }

  /**
   * Tests that getWeekdays() returns the same set of weekdays
   * that was provided in the constructor.
   */
  @Test
  public void testGetWeekdaysReturnsExpectedValues() {
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    RecurrenceRule rule = new RecurrenceRule(weekdays, 5, null);

    Set<DayOfWeek> result = rule.getWeekdays();
    assertEquals("Returned weekdays should match input", weekdays, result);
  }

  /**
   * Tests that getOccurrences() returns the correct number
   * when provided during construction.
   */
  @Test
  public void testGetOccurrencesReturnsExpectedValue() {
    int occurrences = 10;
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.THURSDAY);
    RecurrenceRule rule = new RecurrenceRule(weekdays, occurrences, null);

    assertEquals("Occurrences should match the input", occurrences, (int) rule.getOccurrences());
    assertNull("Until should be null when occurrences is provided", rule.getUntil());
  }

  /**
   * Tests that getUntil() returns the correct LocalDate
   * when provided during construction.
   */
  @Test
  public void testGetUntilReturnsExpectedValue() {
    LocalDate untilDate = LocalDate.now().plusWeeks(2);
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.TUESDAY);
    RecurrenceRule rule = new RecurrenceRule(weekdays, null, untilDate);

    assertEquals("Until date should match input", untilDate, rule.getUntil());
    assertNull("Occurrences should be null when until is provided", rule.getOccurrences());
  }

  /**
   * Tests that getWeekdays() returns an unmodifiable copy
   * and that modifying the original set does not affect the rule.
   */
  @Test
  public void testGetWeekdaysIsImmutableCopy() {
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.MONDAY);
    RecurrenceRule rule = new RecurrenceRule(weekdays, 3, null);

    weekdays.add(DayOfWeek.TUESDAY);

    assertEquals("Rule's weekdays should be unaffected by external modification",
        EnumSet.of(DayOfWeek.MONDAY), rule.getWeekdays());
  }

  /**
   * Verifies that the series generation stops when the current date
   * goes beyond the specified {@code until} date.
   */
  @Test
  public void testGenerateSeriesStopsWhenUntilDateExceeded() {
    Set<DayOfWeek> weekdays = EnumSet.of(LocalDate.now().getDayOfWeek());
    LocalDate untilDate = LocalDate.now().plusDays(2);
    RecurrenceRule rule = new RecurrenceRule(weekdays, null, untilDate);

    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);
    Event seed = new Event.Builder("Test Event", start, end)
        .description("desc")
        .location("loc")
        .status(EventStatus.PUBLIC)
        .build();

    List<Event> series = rule.generateSeries(seed);

    assertFalse("Series should not be empty", series.isEmpty());
    LocalDate lastDate = series.get(series.size() - 1).getStart().toLocalDate();
    assertTrue("Last event should be on or before the until date",
        !lastDate.isAfter(untilDate));
    assertNull("Occurrences is null so stop reason was 'until' check", rule.getOccurrences());
  }

  /**
   * Verifies that {@link InMemoryEventStorage#addEvent(Event)} returns false
   * when attempting to add a duplicate event with the same subject,
   * start time, and end time as an existing event.
   */
  @Test
  public void testAddEventReturnsFalseForDuplicateEvent() {
    InMemoryEventStorage storage = new InMemoryEventStorage();

    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    Event original = new Event.Builder("Team Meeting", start, end)
        .description("Weekly sync")
        .location("Conference Room")
        .status(EventStatus.PUBLIC)
        .build();

    Event duplicate = new Event.Builder("Team Meeting", start, end)
        .description("Weekly sync")
        .location("Conference Room")
        .status(EventStatus.PUBLIC)
        .build();

    boolean firstAdd = storage.addEvent(original);
    boolean secondAdd = storage.addEvent(duplicate);

    // Assert
    assertTrue("First event should be added successfully", firstAdd);
    assertFalse("Duplicate event should not be added", secondAdd);
  }

  /**
   * Verifies that {@link Event#toString()} returns a properly formatted,
   * human-readable string containing the subject and formatted start/end times.
   */
  @Test
  public void testToStringFormatsEventDetailsCorrectly() {
    ZonedDateTime start = ZonedDateTime.parse("2025-11-12T10:00:00Z");
    ZonedDateTime end = ZonedDateTime.parse("2025-11-12T11:00:00Z");

    Event event = new Event.Builder("Team Meeting", start, end)
        .description("Weekly sync")
        .location("Conference Room")
        .status(EventStatus.PUBLIC)
        .build();

    String result = event.toString();

    String expectedDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .format(start);
    assertTrue("String should contain event subject", result.contains("Team Meeting"));
    assertTrue("String should contain formatted start time", result.contains(expectedDateFormat));
    assertTrue("String should contain end time in correct format",
        result.contains(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(end)));
  }

  /**
   * Verifies that {@link Event#belongsToSeries(String)} correctly identifies
   * whether the event belongs to the given series ID.
   */
  @Test
  public void testBelongsToSeriesReturnsTrueWhenMatchingSeriesId() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);
    String seriesId = "abc123";

    Event event = new Event.Builder("Workshop", start, end)
        .seriesId(seriesId)
        .status(EventStatus.PRIVATE)
        .build();

    assertTrue("Should return true for matching seriesId", event.belongsToSeries("abc123"));
    assertFalse("Should return false for non-matching seriesId", event.belongsToSeries("xyz999"));
    assertFalse("Should return false when input is null", event.belongsToSeries(null));
  }

  /**
   * Verifies that {@link Event#shouldModifyAccordingToMode(EventKey, EditMode)}
   * returns false when an unexpected or unsupported EditMode value is provided,
   * ensuring the default branch is covered.
   */
  @Test
  public void testShouldModifyAccordingToModeDefaultCaseReturnsFalse() {
    ZonedDateTime start = ZonedDateTime.now();
    ZonedDateTime end = start.plusHours(1);

    Event event = new Event.Builder("Lecture", start, end)
        .status(EventStatus.PUBLIC)
        .build();

    EventKey key = new EventKey("Lecture", start, end);

    EditMode invalidMode = null;
    try {
      java.lang.reflect.Constructor<EditMode> constructor =
          EditMode.class.getDeclaredConstructor(String.class, int.class);
      constructor.setAccessible(true);
      invalidMode = constructor.newInstance("UNKNOWN_MODE", 999);
    } catch (Exception ignored) {
      // Ignore exceptions, fallback to null safety
    }

    boolean result = event.shouldModifyAccordingToMode(key, invalidMode);

    assertFalse("Unexpected enum value should trigger default branch", result);
  }


}









