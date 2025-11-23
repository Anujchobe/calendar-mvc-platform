import calendar.controller.Ifeatures;
import calendar.controller.MockFeaturesController;
import calendar.view.Icalendarview;
import calendar.view.SwingCalendarView;

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
    // Create the view
    Icalendarview view = new SwingCalendarView();

    // Create the mock controller (for logging GUI events)
    Ifeatures controller = new MockFeaturesController();

    // Wire controller → view
    view.setFeatures(controller);

    // Display GUI
    view.display();
  }
}
