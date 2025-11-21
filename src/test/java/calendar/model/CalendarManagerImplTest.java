package calendar.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for {@link CalendarManagerImpl}.
 *
 * <p>Ensures correctness, robustness, and full JaCoCo coverage by testing
 * calendar creation, editing, switching, and retrieval operations under
 * multiple valid and invalid scenarios.</p>
 */
public class CalendarManagerImplTest {

  private CalendarManagerImpl manager;
  private ZoneId estZone;
  private ZoneId pstZone;
  private ZoneId istZone;

  /**
   * Initializes fresh CalendarManager and zones before each test.
   */
  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
    estZone = ZoneId.of("America/New_York");
    pstZone = ZoneId.of("America/Los_Angeles");
    istZone = ZoneId.of("Asia/Kolkata");
  }

  /**
   * Verifies that a new CalendarManager is initialized empty.
   */
  @Test
  public void testConstructor() {
    CalendarManagerImpl newManager = new CalendarManagerImpl();
    assertNotNull("Manager should be created", newManager);
    assertTrue("Should have no calendars initially", newManager.listCalendars().isEmpty());
  }


  /**
   * Verifies that a calendar can be created successfully.
   */
  @Test
  public void testCreateCalendarSuccess() {
    manager.createCalendar("Work", estZone);

    assertNotNull(manager.getCalendar("Work"));
    assertEquals(1, manager.listCalendars().size());
    assertTrue(manager.listCalendars().contains("Work"));
  }

  /**
   * Ensures the first created calendar automatically becomes active.
   */
  @Test
  public void testCreateCalendarFirstCalendarBecomesActive() {
    manager.createCalendar("Work", estZone);

    Icalendar activeCal = manager.getActiveCalendar();
    assertNotNull(activeCal);
    assertEquals(estZone, activeCal.getZone());
  }

  /**
   * Tests creating multiple calendars with different zones.
   */
  @Test
  public void testCreateCalendarMultipleCalendars() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);
    manager.createCalendar("Family", istZone);

    assertEquals(3, manager.listCalendars().size());
    assertNotNull(manager.getCalendar("Work"));
    assertNotNull(manager.getCalendar("Personal"));
    assertNotNull(manager.getCalendar("Family"));
  }

  /**
   * Verifies only the first created calendar becomes active.
   */
  @Test
  public void testCreateCalendarSecondCalendarDoesNotBecomeActive() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);

    assertEquals(estZone, manager.getActiveCalendar().getZone());
  }

  /**
   * Ensures null name throws exception when creating a calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarNullName() {
    manager.createCalendar(null, estZone);
  }

  /**
   * Ensures blank name throws exception when creating a calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarBlankName() {
    manager.createCalendar("   ", estZone);
  }

  /**
   * Ensures empty name throws exception when creating a calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarEmptyName() {
    manager.createCalendar("", estZone);
  }

  /**
   * Ensures null timezone throws exception when creating a calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarNullTimezone() {
    manager.createCalendar("Work", null);
  }

  /**
   * Ensures duplicate calendar names are not allowed.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarDuplicateName() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Work", pstZone);
  }

  /**
   * Confirms calendar names are case-sensitive.
   */
  @Test
  public void testCreateCalendarCaseSensitiveNames() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("work", pstZone);

    assertEquals(2, manager.listCalendars().size());
    assertNotNull(manager.getCalendar("Work"));
    assertNotNull(manager.getCalendar("work"));
  }


  /**
   * Verifies successful renaming of an existing calendar.
   */
  @Test
  public void testEditCalendarChangeNameSuccess() {
    manager.createCalendar("OldName", estZone);
    manager.editCalendar("OldName", "name", "NewName");

    assertNull(manager.getCalendar("OldName"));
    assertNotNull(manager.getCalendar("NewName"));
  }

  /**
   * Ensures renaming does not change total calendar count.
   */
  @Test
  public void testEditCalendarChangeNameToSameName() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);

    manager.editCalendar("Work", "name", "Office");
    assertNotNull(manager.getCalendar("Office"));
    assertEquals(2, manager.listCalendars().size());
  }

  /**
   * Throws exception when renaming to an existing name.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeNameToExistingName() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);
    manager.editCalendar("Work", "name", "Personal");
  }

  /**
   * Throws exception when renaming to null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeNameToNull() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "name", null);
  }

  /**
   * Throws exception when renaming to blank string.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeNameToBlank() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "name", "   ");
  }

  /**
   * Throws exception when renaming to empty string.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeNameToEmpty() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "name", "");
  }

  /**
   * Throws exception when editing a non-existent calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNonExistentCalendar() {
    manager.editCalendar("DoesNotExist", "name", "NewName");
  }


  /**
   * Verifies timezone change works correctly.
   */
  @Test
  public void testEditCalendarChangeTimezoneSuccess() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", "America/Los_Angeles");

    assertEquals(pstZone, manager.getCalendar("Work").getZone());
  }

  /**
   * Verifies timezone can be updated to IST.
   */
  @Test
  public void testEditCalendarChangeTimezoneToIst() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", "Asia/Kolkata");

    assertEquals(istZone, manager.getCalendar("Work").getZone());
  }

  /**
   * Tests multiple timezone changes on same calendar.
   */
  @Test
  public void testEditCalendarChangeTimezoneMultipleTimes() {
    manager.createCalendar("Work", estZone);

    manager.editCalendar("Work", "timezone", "America/Los_Angeles");
    assertEquals(pstZone, manager.getCalendar("Work").getZone());

    manager.editCalendar("Work", "timezone", "Asia/Kolkata");
    assertEquals(istZone, manager.getCalendar("Work").getZone());

    manager.editCalendar("Work", "timezone", "America/New_York");
    assertEquals(estZone, manager.getCalendar("Work").getZone());
  }

  /**
   * Throws exception when timezone is set to null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeTimezoneToNull() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", null);
  }

  /**
   * Throws exception when timezone is blank.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeTimezoneToBlank() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", "   ");
  }

  /**
   * Throws exception when timezone is empty.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeTimezoneToEmpty() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", "");
  }

  /**
   * Throws exception when timezone string is invalid.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidTimezone() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", "Invalid/Timezone");
  }

  /**
   * Throws exception when timezone format is non-IANA.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidTimezoneFormat() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "timezone", "EST");
  }


  /**
   * Throws exception for unsupported property.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarUnsupportedProperty() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "color", "blue");
  }

  /**
   * Throws exception for unknown property name.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidPropertyName() {
    manager.createCalendar("Work", estZone);
    manager.editCalendar("Work", "invalid", "value");
  }


  /**
   * Verifies name property is editable with mixed casing.
   */
  @Test
  public void testEditCalendarPropertyNameCaseInsensitiveName() {
    manager.createCalendar("Work", estZone);

    manager.editCalendar("Work", "NAME", "NewName1");
    assertNotNull(manager.getCalendar("NewName1"));

    manager.editCalendar("NewName1", "Name", "NewName2");
    assertNotNull(manager.getCalendar("NewName2"));

    manager.editCalendar("NewName2", "nAmE", "NewName3");
    assertNotNull(manager.getCalendar("NewName3"));
  }

  /**
   * Verifies timezone property is editable with mixed casing.
   */
  @Test
  public void testEditCalendarPropertyNameCaseInsensitiveTimezone() {
    manager.createCalendar("Work", estZone);

    manager.editCalendar("Work", "TIMEZONE", "America/Los_Angeles");
    assertEquals(pstZone, manager.getCalendar("Work").getZone());

    manager.editCalendar("Work", "TimeZone", "Asia/Kolkata");
    assertEquals(istZone, manager.getCalendar("Work").getZone());

    manager.editCalendar("Work", "tImEzOnE", "America/New_York");
    assertEquals(estZone, manager.getCalendar("Work").getZone());
  }


  /**
   * Verifies switching active calendars works correctly.
   */
  @Test
  public void testUseCalendarSuccess() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);

    manager.useCalendar("Personal");
    assertEquals(pstZone, manager.getActiveCalendar().getZone());
  }

  /**
   * Verifies switching between multiple calendars.
   */
  @Test
  public void testUseCalendarSwitchBetweenMultipleCalendars() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);
    manager.createCalendar("Family", istZone);

    manager.useCalendar("Personal");
    assertEquals(pstZone, manager.getActiveCalendar().getZone());

    manager.useCalendar("Family");
    assertEquals(istZone, manager.getActiveCalendar().getZone());

    manager.useCalendar("Work");
    assertEquals(estZone, manager.getActiveCalendar().getZone());
  }

  /**
   * Verifies using same calendar twice returns same instance.
   */
  @Test
  public void testUseCalendarSameCalendarTwice() {
    manager.createCalendar("Work", estZone);
    manager.useCalendar("Work");
    Icalendar first = manager.getActiveCalendar();

    manager.useCalendar("Work");
    Icalendar second = manager.getActiveCalendar();

    assertSame(first, second);
  }

  /**
   * Throws exception when switching to a non-existent calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarNonExistentCalendar() {
    manager.useCalendar("DoesNotExist");
  }

  /**
   * Throws exception when using calendar name is null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarNullName() {
    manager.useCalendar(null);
  }

  /**
   * Verifies switching after renaming calendar behaves correctly.
   */
  @Test
  public void testUseCalendarAfterDeletingCalendar() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);

    manager.useCalendar("Work");
    assertEquals(estZone, manager.getActiveCalendar().getZone());

    manager.editCalendar("Work", "name", "Office");

    try {
      manager.useCalendar("Work");
      fail("Should throw exception - Work no longer exists");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    manager.useCalendar("Office");
    assertEquals(estZone, manager.getActiveCalendar().getZone());
  }


  /**
   * Ensures active calendar is non-null after first creation.
   */
  @Test
  public void testGetActiveCalendarAfterFirstCalendarCreated() {
    manager.createCalendar("Work", estZone);
    assertNotNull(manager.getActiveCalendar());
    assertEquals(estZone, manager.getActiveCalendar().getZone());
  }

  /**
   * Throws exception when no calendars exist yet.
   */
  @Test(expected = IllegalStateException.class)
  public void testGetActiveCalendarNoCalendarCreated() {
    manager.getActiveCalendar();
  }

  /**
   * Verifies active calendar updates after useCalendar().
   */
  @Test
  public void testGetActiveCalendarAfterUseCalendar() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);

    manager.useCalendar("Personal");
    assertEquals(pstZone, manager.getActiveCalendar().getZone());
  }

  /**
   * Ensures repeated calls return same active instance.
   */
  @Test
  public void testGetActiveCalendarReturnsSameInstance() {
    manager.createCalendar("Work", estZone);
    assertSame(manager.getActiveCalendar(), manager.getActiveCalendar());
  }


  /**
   * Verifies getting an existing calendar by name works.
   */
  @Test
  public void testGetCalendarExistingCalendar() {
    manager.createCalendar("Work", estZone);
    assertEquals(estZone, manager.getCalendar("Work").getZone());
  }

  /**
   * Ensures null is returned for missing calendar.
   */
  @Test
  public void testGetCalendarNonExistentCalendar() {
    assertNull(manager.getCalendar("DoesNotExist"));
  }

  /**
   * Ensures null is returned when name argument is null.
   */
  @Test
  public void testGetCalendarNullName() {
    assertNull(manager.getCalendar(null));
  }

  /**
   * Verifies multiple calendars can coexist and be retrieved.
   */
  @Test
  public void testGetCalendarMultipleCalendars() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);
    manager.createCalendar("Family", istZone);

    assertEquals(estZone, manager.getCalendar("Work").getZone());
    assertEquals(pstZone, manager.getCalendar("Personal").getZone());
    assertEquals(istZone, manager.getCalendar("Family").getZone());
  }

  /**
   * Ensures renamed calendars are retrievable under new names only.
   */
  @Test
  public void testGetCalendarAfterNameChange() {
    manager.createCalendar("OldName", estZone);
    manager.editCalendar("OldName", "name", "NewName");

    assertNull(manager.getCalendar("OldName"));
    assertNotNull(manager.getCalendar("NewName"));
  }

  /**
   * Confirms calendar name lookups are case-sensitive.
   */
  @Test
  public void testGetCalendarCaseSensitive() {
    manager.createCalendar("Work", estZone);

    assertNotNull(manager.getCalendar("Work"));
    assertNull(manager.getCalendar("work"));
    assertNull(manager.getCalendar("WORK"));
  }


  /**
   * Verifies listing calendars returns empty list initially.
   */
  @Test
  public void testListCalendarsEmpty() {
    List<String> calendars = manager.listCalendars();
    assertTrue(calendars.isEmpty());
  }

  /**
   * Ensures list contains created calendar.
   */
  @Test
  public void testListCalendarsSingleCalendar() {
    manager.createCalendar("Work", estZone);

    List<String> calendars = manager.listCalendars();
    assertEquals(1, calendars.size());
    assertTrue(calendars.contains("Work"));
  }

  /**
   * Verifies list contains all created calendars.
   */
  @Test
  public void testListCalendarsMultipleCalendars() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);
    manager.createCalendar("Family", istZone);

    List<String> calendars = manager.listCalendars();
    assertEquals(3, calendars.size());
    assertTrue(calendars.contains("Work"));
    assertTrue(calendars.contains("Personal"));
    assertTrue(calendars.contains("Family"));
  }

  /**
   * Ensures renamed calendars reflect updated names in list.
   */
  @Test
  public void testListCalendarsAfterNameChange() {
    manager.createCalendar("OldName", estZone);
    manager.editCalendar("OldName", "name", "NewName");

    List<String> calendars = manager.listCalendars();
    assertTrue(calendars.contains("NewName"));
    assertFalse(calendars.contains("OldName"));
  }

  /**
   * Confirms listCalendars returns a new immutable list copy.
   */
  @Test
  public void testListCalendarsReturnsNewListEachTime() {
    manager.createCalendar("Work", estZone);

    List<String> first = manager.listCalendars();
    List<String> second = manager.listCalendars();

    assertNotSame("Should return different list instances", first, second);
    assertEquals("Lists should have same content", first, second);
  }

  @Test
  public void testListCalendarsModifyingReturnedListDoesNotAffectManager() {
    manager.createCalendar("Work", estZone);

    List<String> calendars = manager.listCalendars();
    calendars.add("Fake");
    calendars.remove("Work");

    // Original manager should be unchanged
    List<String> actualCalendars = manager.listCalendars();
    assertEquals("Should still have 1 calendar", 1, actualCalendars.size());
    assertTrue("Should still contain 'Work'", actualCalendars.contains("Work"));
    assertFalse("Should not contain 'Fake'", actualCalendars.contains("Fake"));
  }

  /**
   * Verifies combined create, edit, use, and list operations work correctly.
   */
  @Test
  public void testComplexScenarioCreateEditUseList() {
    manager.createCalendar("Work", estZone);
    manager.createCalendar("Personal", pstZone);
    assertEquals(2, manager.listCalendars().size());

    manager.editCalendar("Work", "name", "Office");
    assertNull(manager.getCalendar("Work"));
    assertNotNull(manager.getCalendar("Office"));

    manager.editCalendar("Personal", "timezone", "Asia/Kolkata");
    assertEquals(istZone, manager.getCalendar("Personal").getZone());

    manager.useCalendar("Personal");
    assertEquals(istZone, manager.getActiveCalendar().getZone());

    List<String> calendars = manager.listCalendars();
    assertTrue(calendars.contains("Office"));
    assertTrue(calendars.contains("Personal"));
    assertFalse(calendars.contains("Work"));
  }

  /**
   * Ensures multiple name and timezone edits update correctly.
   */
  @Test
  public void testComplexScenarioMultipleEdits() {
    manager.createCalendar("Calendar1", estZone);

    manager.editCalendar("Calendar1", "name", "Calendar2");
    manager.editCalendar("Calendar2", "name", "Calendar3");
    manager.editCalendar("Calendar3", "name", "FinalName");

    assertNotNull(manager.getCalendar("FinalName"));
    assertNull(manager.getCalendar("Calendar1"));
    assertNull(manager.getCalendar("Calendar2"));
    assertNull(manager.getCalendar("Calendar3"));

    manager.editCalendar("FinalName", "timezone", "America/Los_Angeles");
    manager.editCalendar("FinalName", "timezone", "Asia/Kolkata");
    manager.editCalendar("FinalName", "timezone", "America/New_York");

    assertEquals(estZone, manager.getCalendar("FinalName").getZone());
  }

  /**
   * Checks that active calendar remains valid after renaming.
   */
  @Test
  public void testComplexScenarioActiveCalendarPersistence() {
    manager.createCalendar("Cal1", estZone);
    manager.createCalendar("Cal2", pstZone);
    manager.createCalendar("Cal3", istZone);

    manager.useCalendar("Cal2");
    assertEquals(pstZone, manager.getActiveCalendar().getZone());

    manager.editCalendar("Cal2", "name", "Cal2Renamed");
    assertEquals(pstZone, manager.getActiveCalendar().getZone());

    try {
      manager.useCalendar("Cal2");
      fail("Should throw - Cal2 no longer exists");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    manager.useCalendar("Cal2Renamed");
    assertEquals(pstZone, manager.getActiveCalendar().getZone());
  }
}
