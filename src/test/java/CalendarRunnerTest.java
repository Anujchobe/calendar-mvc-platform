import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the CalendarRunner class ensuring correct mode handling
 * and proper fallback behavior across GUI, interactive, and headless modes.
 */
public class CalendarRunnerTest {

  private ByteArrayOutputStream output;

  /**
   * Sets up an output stream to capture printed text.
   */
  @Before
  public void setUp() {
    output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
  }

  /**
   * Invokes a private static method on CalendarRunner via reflection.
   *
   * @param name method name
   * @param args arguments
   * @throws Exception reflection issues
   */
  private void invokePrivate(String name, Object... args) throws Exception {
    Class<?>[] types = new Class<?>[args.length];
    for (int i = 0; i < args.length; i++) {
      types[i] = args[i].getClass();
    }
    Method m = CalendarRunner.class.getDeclaredMethod(name, types);
    m.setAccessible(true);
    m.invoke(null, args);
  }

  /**
   * Ensures that providing no arguments triggers GUI launch logic
   * and the application prints the expected headless message.
   */
  @Test
  public void testNoArgsLaunchesGui() {
    System.setProperty("java.awt.headless", "true");

    CalendarRunner.main(new String[]{});

    String out = output.toString().toLowerCase();
    assertTrue(out.contains("gui mode not available in headless environment"));
  }


  /**
   * Ensures invalid flag triggers usage output.
   */
  @Test
  public void testInvalidFlagPrintsUsage() {
    CalendarRunner.main(new String[]{"wrong"});
    String out = output.toString().toLowerCase();
    assertTrue(out.contains("usage"));
  }

  /**
   * Ensures missing mode value prints usage.
   */
  @Test
  public void testMissingModeValue() {
    CalendarRunner.main(new String[]{"--mode"});
    String out = output.toString().toLowerCase();
    assertTrue(out.contains("missing mode"));
  }

  /**
   * Ensures unknown mode triggers usage message.
   */
  @Test
  public void testUnknownMode() {
    CalendarRunner.main(new String[]{"--mode", "xyz"});
    String out = output.toString().toLowerCase();
    assertTrue(out.contains("unknown mode"));
  }

  /**
   * Ensures headless mode requires a script path.
   */
  @Test
  public void testHeadlessModeMissingScript() {
    CalendarRunner.main(new String[]{"--mode", "headless"});
    String out = output.toString().toLowerCase();
    assertTrue(out.contains("missing script"));
  }

  /**
   * Ensures that headless mode prints file not found for an invalid path.
   */
  @Test
  public void testHeadlessModeFileNotFound() {
    CalendarRunner.main(new String[]{"--mode", "headless", "missing-file.txt"});
    String out = output.toString().toLowerCase();
    assertTrue(out.contains("not found"));
  }

  /**
   * Ensures headless mode processes a valid empty script file.
   *
   * @throws Exception file handling issues
   */
  @Test
  public void testHeadlessModeValidScript() throws Exception {
    File temp = File.createTempFile("script", ".txt");
    temp.deleteOnExit();
    CalendarRunner.main(new String[]{"--mode", "headless", temp.getAbsolutePath()});
    String out = output.toString().toLowerCase();
    assertFalse(out.contains("not found"));
  }

  /**
   * Ensures printUsage prints expected structure.
   *
   * @throws Exception reflection issues
   */
  @Test
  public void testPrintUsage() throws Exception {
    invokePrivate("printUsage", "err");
    String out = output.toString().toLowerCase();
    assertTrue(out.contains("usage"));
  }
}
