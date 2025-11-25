package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.EventStatus;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;

/**
 * Unit tests for the **CommandAdapter** class.
 *
 * <p>Verifies the adapter's ability to correctly translate high-level parameters
 * into well-formed, escaped, and quoted string tokens suitable for the command parser.</p>
 */
public class CommandAdapterTest {
  /**
   * The adapter instance used to convert high-level controller calls into
   * {@code String} tokens for command parsing.
   */
  private final CommandAdapter adapter = new CommandAdapter();

  /**
   * Creates a {@code ZonedDateTime} object set specifically to the Eastern Time (EST) zone.
   * This is a convenience method for creating test data or internal parameters.
   *
   * @param year   The year.
   * @param month  The month (1-12).
   * @param day    The day of the month.
   * @param hour   The hour (0-23).
   * @param minute The minute.
   * @return A {@code ZonedDateTime} object set to the specified time in EST.
   */
  private ZonedDateTime est(int year, int month, int day, int hour, int minute) {
    return ZonedDateTime.of(year, month, day, hour, minute, 0, 0,
        ZoneId.of("America/New_York"));
  }

  @Test
  public void testBuildCreateCalendarCommand() {
    String cmd = adapter.buildCreateCalendarCommand("Work", "America/New_York");
    assertEquals(
        "create calendar --name \"Work\" --timezone \"America/New_York\"",
        cmd);
  }

  @Test
  public void testBuildUseCalendarCommand() {
    String cmd = adapter.buildUseCalendarCommand("Personal");
    assertEquals("use calendar --name \"Personal\"", cmd);
  }

  @Test
  public void testBuildCreateSingleEventCommandWithAllFields() {
    ZonedDateTime start = est(2025, 1, 15, 9, 0);
    ZonedDateTime end = est(2025, 1, 15, 10, 0);

    String cmd = adapter.buildCreateSingleEventCommand(
        "Team Meeting",
        start,
        end,
        "Sprint review",
        "Zoom",
        EventStatus.PUBLIC
    );

    String expected =
        "create event \"Team Meeting\" "
            + "from 2025-01-15T09:00 "
            + "to 2025-01-15T10:00 "
            + "description \"Sprint review\" "
            + "location \"Zoom\" "
            + "status PUBLIC";

    assertEquals(expected, cmd);
  }

  @Test
  public void testBuildCreateSingleEventCommandWithMinimalFields() {
    ZonedDateTime start = est(2025, 1, 15, 9, 0);
    ZonedDateTime end = est(2025, 1, 15, 10, 0);

    String cmd = adapter.buildCreateSingleEventCommand(
        "Solo Task",
        start,
        end,
        "",
        null,
        null
    );

    assertTrue(
        cmd.startsWith("create event \"Solo Task\" from 2025-01-15T09:00 to 2025-01-15T10:00"));
    assertFalse(cmd.contains("description"));
    assertFalse(cmd.contains("location"));
    assertFalse(cmd.contains("status"));
  }

  @Test
  public void testBuildCreateRecurringEventCommandWithOccurrences() {
    ZonedDateTime start = est(2025, 1, 15, 8, 0);
    ZonedDateTime end = est(2025, 1, 15, 9, 0);

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Yoga Class",
        start,
        end,
        "Morning stretch",
        "Gym",
        EventStatus.PRIVATE,
        "mtwrf",
        5,
        null
    );

    String expected =
        "create event \"Yoga Class\" "
            + "from 2025-01-15T08:00 "
            + "to 2025-01-15T09:00 "
            + "repeats MTWRF "
            + "for 5 "
            + "description \"Morning stretch\" "
            + "location \"Gym\" "
            + "status PRIVATE";

    assertEquals(expected, cmd);
  }

  @Test
  public void testBuildCreateRecurringEventCommandWithEndDateAndNoOptionalFields() {
    ZonedDateTime start = est(2025, 1, 15, 8, 0);
    ZonedDateTime end = est(2025, 1, 15, 9, 0);

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Yoga Class",
        start,
        end,
        "",
        "",
        null,
        null,
        null,
        java.time.LocalDate.of(2025, 1, 31)
    );

    String expectedPrefix =
        "create event \"Yoga Class\" from 2025-01-15T08:00 to 2025-01-15T09:00 ";
    assertTrue(cmd.startsWith(expectedPrefix));
    assertTrue(cmd.contains("until 2025-01-31"));
    assertFalse(cmd.contains("repeats"));
    assertFalse(cmd.contains("for "));
    assertFalse(cmd.contains("description"));
    assertFalse(cmd.contains("location"));
    assertFalse(cmd.contains("status"));
  }

  @Test
  public void testBuildEditSingleEventCommand() {
    ZonedDateTime start = est(2025, 1, 15, 9, 0);
    ZonedDateTime end = est(2025, 1, 15, 10, 0);

    String cmd = adapter.buildEditSingleEventCommand(
        "Team Meeting",
        start,
        end,
        "location",
        "Zoom"
    );

    String expected =
        "edit event \"Team Meeting\" "
            + "from 2025-01-15T09:00 "
            + "to 2025-01-15T10:00 "
            + "with location \"Zoom\"";

    assertEquals(expected, cmd);
  }

  @Test
  public void testBuildEditSeriesFromThisOnwardCommand() {
    ZonedDateTime start = est(2025, 1, 15, 9, 0);
    ZonedDateTime end = est(2025, 1, 15, 10, 0);

    String cmd = adapter.buildEditSeriesFromThisOnwardCommand(
        "Yoga Class",
        start,
        end,
        "status",
        "public"
    );


    String expected =
        "edit events \"Yoga Class\" "
            + "from 2025-01-15T09:00 "
            + "to 2025-01-15T10:00 "
            + "with status \"PUBLIC\"";

    assertEquals(expected, cmd);
  }

  @Test
  public void testBuildEditEntireSeriesCommand() {
    ZonedDateTime start = est(2025, 1, 15, 9, 0);
    ZonedDateTime end = est(2025, 1, 15, 10, 0);

    String cmd = adapter.buildEditEntireSeriesCommand(
        "Yoga Class",
        start,
        end,
        "subject",
        "New Title"
    );

    String expected =
        "edit series \"Yoga Class\" "
            + "from 2025-01-15T09:00 "
            + "to 2025-01-15T10:00 "
            + "with subject \"New Title\"";

    assertEquals(expected, cmd);
  }
}
