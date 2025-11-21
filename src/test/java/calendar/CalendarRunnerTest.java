package calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import org.junit.Test;

/**
 * Tests for {@link calendar.CalendarRunner}.
 */
public class CalendarRunnerTest {

  /**
   * Captures System.out while invoking CalendarRunner.main().
   */
  private String runWithArgs(String... args) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;

    try {
      System.setOut(new PrintStream(out));
      calendar.CalendarRunner.main(args);   // <- IMPORTANT: matches your actual package
    } finally {
      System.setOut(originalOut);
    }

    return out.toString();
  }

  @Test
  public void testMissingModePrintsUsage() {
    String output = runWithArgs();
    assertTrue(output.contains("Usage: java -jar calendar.jar --mode"));
  }

  @Test
  public void testInvalidModePrintsUsage() {
    String output = runWithArgs("--mode");
    assertTrue(output.contains("Usage: java -jar calendar.jar --mode"));
  }

  @Test
  public void testUnknownMode() {
    String output = runWithArgs("--mode", "nonsense");
    assertTrue(output.contains("Unknown mode"));
  }

  @Test
  public void testHeadlessModeMissingFilename() {
    String output = runWithArgs("--mode", "headless");
    assertTrue(output.contains("Missing command file"));
  }

  @Test
  public void testHeadlessModeFileNotFound() {
    String output = runWithArgs("--mode", "headless", "no_such_file.txt");
    assertTrue(output.contains("Command file not found"));
  }

  @Test
  public void testHeadlessModeValidFileRuns() throws Exception {
    File tmp = File.createTempFile("calrunner_test", ".txt");
    tmp.deleteOnExit();
    java.nio.file.Files.writeString(tmp.toPath(), "exit\n");

    String output = runWithArgs("--mode", "headless", tmp.getAbsolutePath());

    assertFalse(output.contains("Fatal error"));
    assertTrue(output.contains("exiting..."));
  }
}
