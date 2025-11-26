# Misc.md

The Project-Architecture file has the entire Architecture explanation.
These are the brief design changes done for this particular assignment.

## 1. Design Changes & Justifications

| Change Made                                                              |  Justification                                                                       |
|--------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| Introduced `IcalendarView` interface                                     | Ensures view is fully decoupled from Swing; controller interacts only via abstraction (MVC). |
| Implemented listener-based controller (`Ifeatures`)                      | View fires actions, controller listens and invokes model logic.                              |
| Created `SwingCalendarView` as the main GUI                              | Clean separation of GUI code; Only use of Java Swing without external libs.                  |
| Designed `MonthPanel` + `DayCell` components                             | Modular UI architecture; separates rendering logic and improves maintainability.             |
| Added `CreateCalendarDialog`, `CreateEventDialog`, and `EditEventDialog` | Reduces invalid user input; improves UX by avoiding text-based commands.                     |
| Added color-coding for calendars                                         | User must clearly see which calendar is currently selected.                                  |
| Added top-bar showing active calendar and timezone                       | Ensures clarity and usability.                                                               |
| Added automatic system-timezone default calendar                         | User should not be forced to create a calendar first.                                        |
| Implemented Day-Selection logic from MonthView                           | Enables viewing events on any selected date directly from the GUI.                           |
| Added timezone selector for new calendars                                | Multi-timezone support.                                                                      |
| Error feedback handled via JOptionPane                                   | Graceful handling without leaking technical details.                                         |
| Refactored recurring event creation dialog                               | Supports weekday selection, frequency, and end conditions.                                   |
| Split UI into specialized components                                     | Improves readability and maintainability.                                                    |

## 2. Features Working / Not Implemented

### Fully Working
- Create calendar (custom timezone)
- Default system-timezone calendar auto-created
- Switch between calendars (color-coded)
- Month view navigation (next/prev)
- Day selection + event list view
- Create event dialog (single + recurring)
- Edit event dialog (single + series)
- View all events on selected day
- Controller-listener architecture functional
- Error handling for invalid inputs
- Headless + interactive mode from previous iteration
- GUI mode launches when no arguments supplied
- JAR runs all modes correctly

### Not Implemented
- Weekly/daily view.


## Information
- Swing UI kept simple per requirement.
- All business logic remains in the model; GUI only collects inputs.
- Dialogs validate inputs and prevent malformed entries.
- No Swing references appear in controller.
- Layout proportioned to meet usability and grading guidelines.
- View acts as a event firer and controller listens to the events being fired.
- We created a Ifeature interface layer above the Icommandadapter which builds the commands and gives to the already exisiting controller.Proving extensibility of our project.
- Absolutely no changes were made to the already existing controller layer.We made a layer that could be extended to support multiple views and also multiple scripts.

