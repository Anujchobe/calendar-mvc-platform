# Calendar Application — Design & Architecture (MVC)

---

## 1. High-Level Overview

- **Goal:** A multi-calendar virtual scheduling system with:
    - Month view
    - Single & recurring events
    - Editing (single / from-this-onward / entire series)
- **Architecture:** Classic **Model–View–Controller (MVC)**:
    - **Model:** Calendar + event data, recurrence logic, queries.
    - **View:** Java Swing GUI components (no model calls).
    - **Controller:** Glue between View and Model via a clean interface (`Ifeatures`).

---

## 2. Architecture Summary

### 2.1 Model (Conceptual — not all files shown here)

- **Responsibilities:**
    - Store calendars, events, recurrence rules.
    - Validate and enforce business constraints.
    - Provide query APIs, e.g.:
        - `queryEventsOn(LocalDate)`
        - `queryEventsBetween(ZonedDateTime, ZonedDateTime)`
        - `createEvent(...)`, `createSeries(...)`, `editEvent(...)`, etc.
- **Design Goals:**
    - No Swing imports.
    - No dependency on the controller or view.

---

### 2.2 Controller Layer

**Key Interfaces & Classes:**

- `Ifeatures`
    - The **only** interface the view talks to.
    - Methods:
        - `createCalendar`, `useCalendar`, `getCalendarNames`, `getCurrentCalendarName`
        - `createSingleEvent`, `createRecurringEvent`
        - `editSingleEvent`, `editSeriesFromThisOnward`, `editEntireSeries`
        - `getEventsOn(LocalDate)`
        - `navigateToMonth(YearMonth)`, `selectDate(LocalDate)`

- Command infrastructure (for text/CLI mode):
    - `Command`, `AbstractCommand`, `CommandFactory`, `CommandParser`, etc.
    - Example commands:
        - `PrintEventsOnCommand`
        - `PrintEventsRangeCommand`
        - `QueryCommand`
        - `ShowStatusCommand`
        - `UseCalendarCommand`
    - All operate on `IcalendarManager` / `Icalendar` (model interfaces).

- Utility:
    - `ParseUtils`
        - Parsing of dates, date-times, subjects, weekday patterns.
        - Shared by command layer and (logically) by controller.

**Controller Responsibility:**

- Accept user intent from the view (`Ifeatures` methods) or CLI
- Invoke appropriate model methods.
- Report back to the view via and handle CLI commands appropriately.
    - `showSuccess(String)`
    - `showError(String)`
    - `view.refresh()`

---

### 2.3 View Layer (Swing GUI)

**Top-level View Interface:**

- `Icalendarview`
    - `display()`
    - `refresh()`
    - `showError(String)`
    - `showSuccess(String)`
    - `setFeatures(Ifeatures)`

**Main GUI Frame:**

- `SwingCalendarView` (implements `Icalendarview`)
    - Composition:
        - `CalendarListPanel` (left)
        - `MonthViewPanel` (center)
        - Event list + action buttons (right)
    - Uses:
        - `CreateCalendarDialog`
        - `CreateEventDialog`
        - `RecurringEventDialog`
        - `EditEventDialog`
        - `ViewEventsDialog`
    - Holds the **only** reference to the controller: `private Ifeatures features`.

**Reusable Panels / Dialogs:**

- `CalendarListPanel`
    - Displays list of calendar names.
    - Uses `CalendarSelectionListener` (functional interface) to notify selection.
- `MonthViewPanel`
    - 7×7 grid of days + weekday header row.
    - Highlights:
        - Today (blue border)
        - Selected date (black border)
    - Uses `DateSelectionListener` callback.
- `CreateCalendarDialog`
    - Input: calendar name + timezone.
    - Does not talk to the model or controller.
- `CreateEventDialog`
    - Input: subject, location, description, status, start/end times.
    - Returns data via getters; controller invoked by `SwingCalendarView`.
- `RecurringEventDialog`
    - Adds:
        - Weekday checkboxes (M–U mapped to `MTWRFSU` pattern).
        - Radio buttons: by occurrences vs by end date.
- `EditEventDialog`
    - Pre-populated from an `Event`.
    - Lets user select edit scope:
        - SINGLE
        - FROM_THIS_ONWARD
        - ENTIRE_SERIES
    - Returns `EditMode` and changed fields.
- `ViewEventsDialog`
    - Read-only list of events for a date.
    - Purely display; no model or controller calls.

**View–Controller Binding:**

- `IviewEventFirer`
    - Any dialog/panel that may need to fire events implements this.
    - Single method: `setFeatures(Ifeatures)`.

    

## 3. Advantages and Limitations of This Design

### 3.1 Advantages

- **Strong MVC separation**
    - View is replaceable (e.g., could add JavaFX or web client later).
- **Low coupling, high cohesion**
    - Panels and dialogs each have a single responsibility.
- **Testability**
    - Mock controllers and simple view contracts.
- **Extensibility**
      - New features can be plugged in via `Ifeatures` and new dialogs/panels.If a new view is added.All you have to do is implement the Ifeatures interface and use the ICommandAdapter that already builds the required commands.
- 
- **Flow of execution**
    - Clear flow: View → Ifeatures →IcommandAdapter-> Controller → Model.

### 3.2 Limitations

- **More classes and files**
    - Clean separation increases number of classes; overhead for small teams.
- **Event editing is multi-step**
    - Multiple calls to `editSingleEvent`/`editSeries...` to update all fields can be verbose.

Despite these limitations, the chosen design prioritizes **clarity**, **extensibility**, and **adherence to MVC**.

