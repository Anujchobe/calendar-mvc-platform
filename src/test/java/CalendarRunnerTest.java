import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import org.junit.Test;

/**
 * Tests for {@link CalendarRunner}.
 * Updated to match the new behavior:
 *  - No arguments → GUI mode (no usage printed)
 *  - --mode missing → usage printed
 *  - --mode nonsense → unknown mode + usage
 *  - --mode headless missing file → missing script path
 *  - --mode headless bad file → file not found
 *  - Valid script → executes parser
 */
public class CalendarRunnerTest {

  private String runWithArgs(String... args) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;

    try {
      System.setOut(new PrintStream(out));
      CalendarRunner.main(args);
    } finally {
      System.setOut(originalOut);
    }
    return out.toString();
  }

  @Test
  public void testNoArgsStartsGui() {
    String output = runWithArgs();
    // GUI mode should not print usage
    assertFalse(output.contains("Usage"));
    assertFalse(output.toLowerCase().contains("error"));
  }

  @Test
  public void testInvalidModePrintsUsage() {
    // "--mode" but no mode value
    String output = runWithArgs("--mode");
    assertTrue(output.contains("Usage"));
  }

  @Test
  public void testUnknownMode() {
    String output = runWithArgs("--mode", "nonsense");
    assertTrue(output.contains("Unknown mode"));
    assertTrue(output.contains("Usage"));
  }

  @Test
  public void testHeadlessModeMissingFilename() {
    String output = runWithArgs("--mode", "headless");
    // Should indicate missing script path
    assertTrue(output.toLowerCase().contains("missing"));
    assertTrue(output.contains("Usage"));
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

    // CommandParser prints "exiting..." or might print output for exit
    String lower = output.toLowerCase();
    assertTrue(lower.contains("exit")
        || lower.contains("goodbye")
        || lower.isEmpty());
  }
}
