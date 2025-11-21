/**
 * Serves as a simple **wrapper** to launch the core application runner.
 * This class delegates execution to the {@code main} method located in the
 * primary application package ({@code calendar.CalendarRunner}).
 */
public class CalendarRunner {

  /**
   * The main method that launches the application by calling the core runner.
   *
   * @param args Command-line arguments passed to the application.
   */
  public static void main(String[] args) {
    calendar.CalendarRunner.main(args);
  }
}
