package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;

/**
 * Unit tests for the {@link CommandAdapter} or similar helper class responsible
 * for converting high-level parameters into formatted command {@code String}s.
 *
 * <p>Verifies correct quoting, escaping, and formatting of command arguments.</p>
 */
public class CommandStringHelperTest {

  @Test
  public void testQuoteHandlesNullAndEmptyAndEscapes() {

    assertEquals("\"\"", CommandStringHelper.quote(null));


    assertEquals("\"\"", CommandStringHelper.quote(""));


    assertEquals("\"Hello\"", CommandStringHelper.quote("Hello"));


    String input = "He said \"hi\"";
    String expected = "\"He said \\\"hi\\\"\"";
    assertEquals(expected, CommandStringHelper.quote(input));
  }

  @Test
  public void testFormatDateNullAndNonNull() {
    assertEquals("", CommandStringHelper.formatDate(null));
    assertEquals("2025-01-15", CommandStringHelper.formatDate(LocalDate.of(2025, 1, 15)));
  }

  @Test
  public void testFormatZdtLocalIsoNullAndConversionToEst() {

    assertEquals("", CommandStringHelper.formatZdtLocalIso(null));


    ZonedDateTime utc = ZonedDateTime.of(
        2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC"));

    String formatted = CommandStringHelper.formatZdtLocalIso(utc);

    assertEquals("2025-01-15T05:00", formatted);
  }

  @Test
  public void testFormatPropertyValueStartEndZonedDateTime() {
    ZonedDateTime utcStart = ZonedDateTime.of(
        2025, 1, 15, 10, 0, 0, 0, ZoneId.of("UTC"));

    String value = CommandStringHelper.formatPropertyValue("start", utcStart);

    assertEquals("\"2025-01-15T05:00\"", value);
  }

  @Test
  public void testFormatPropertyValueStartEndLocalDateTime() {
    LocalDateTime ldt = LocalDateTime.of(2025, 1, 15, 9, 30);
    String value = CommandStringHelper.formatPropertyValue("end", ldt);
    assertEquals("\"2025-01-15T09:30\"", value);
  }

  @Test
  public void testFormatPropertyValueStartEndFallbackNonDate() {
    String value = CommandStringHelper.formatPropertyValue("start", "foo");
    assertEquals("\"foo\"", value);
  }

  @Test
  public void testFormatPropertyValueStatusUppercases() {
    String value = CommandStringHelper.formatPropertyValue("status", "public");
    assertEquals("\"PUBLIC\"", value);
  }

  @Test
  public void testFormatPropertyValueOtherProperties() {
    assertEquals("\"Room 1\"",
        CommandStringHelper.formatPropertyValue("location", "Room 1"));
    assertEquals("\"Meeting\"",
        CommandStringHelper.formatPropertyValue("subject", "Meeting"));
  }

  @Test
  public void testFormatPropertyValueNull() {
    assertEquals("\"\"",
        CommandStringHelper.formatPropertyValue("subject", null));
  }

  @Test
  public void testBuildEditPrefixForDifferentScopes() {
    ZonedDateTime start = ZonedDateTime.of(
        2025, 1, 15, 9, 0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime end = ZonedDateTime.of(
        2025, 1, 15, 10, 0, 0, 0, ZoneId.of("America/New_York"));

    String prefixEvent = CommandStringHelper.buildEditPrefix("event", "Team Meeting", start, end);
    String prefixEvents = CommandStringHelper.buildEditPrefix("events", "Team Meeting", start, end);
    String prefixSeries = CommandStringHelper.buildEditPrefix("series", "Team Meeting", start, end);

    assertTrue(prefixEvent.startsWith("edit event \"Team Meeting\" from "));
    assertTrue(prefixEvents.startsWith("edit events \"Team Meeting\" from "));
    assertTrue(prefixSeries.startsWith("edit series \"Team Meeting\" from "));

    String expectedTimePart = "from 2025-01-15T09:00 to 2025-01-15T10:00 ";
    assertTrue(prefixEvent.endsWith(expectedTimePart));
  }
}
