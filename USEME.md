# USEME.md
## Calendar Application — How to Run & Use the Application

This document explains how to run the Calendar System in all supported modes (GUI, interactive, and headless) and provides a clear, step-by-step guide for using the application

---

## Requirements
- Java 11 or higher installed
- The file `calendar-1.0.jar` located in the `libs/` directory (or your build output folder)

## 1. Interactive Mode
Interactive mode lets you type commands manually.

Run:
```bash
java -jar calendar-1.0.jar --mode interactive
```

Example interactive session:

create calendar --name Work --timezone America/New_York
use calendar --name Work
create event "Team Meeting" from 2025-11-10T10:00 to 2025-11-10T11:00 description "Weekly sync"
print events on 2025-11-10
exit

## 2. Headless Mode
Headless mode executes commands from a file.

Create a plain text file, for example `commands.txt`, with contents such as:

create calendar --name Work --timezone America/New_York
use calendar --name Work
create event "Team Meeting" from 2025-11-10T10:00 to 2025-11-10T11:00
print events on 2025-11-10
exit

Run headless mode using:

```bash
java -jar calendar-1.0.jar --mode headless commands.txt
```

## 3. Graphical Mode (Default)

Launching the application without arguments opens the graphical interface.

```bash
java -jar calendar-1.0.jar
```

This provides the full Swing month-view user interface.

# a. Using the Graphical User Interface

The GUI supports all required calendar operations.

---

## a.1 Creating a Calendar

1. Click “+ New Calendar”.
2. Enter a calendar name and timezone.
3. Press **Create**.
4. The calendar appears in the sidebar.

---

## a.2 Switching Between Calendars

- All calendars appear in the left sidebar.
- Click a calendar name to switch to it.
- The month view and event list refresh immediately.

---

## a.3 Navigating the Month View

Use the top navigation bar:

- **◀ Previous** — go to the previous month
- **Next ▶** — go to the next month
- **Today** — return to the current month

---

## a.4 Selecting a Day

- Click any day in the month grid.
- The right panel shows all events for that date.

---

## a.5 Viewing Events on a Date

The event list shows:
- Title
- Start & End time
- Location

If no events exist, the panel indicates the day is empty.

---

## a.6 Creating a Single Event

1. Select a date.
2. Click **Create Event**.
3. Enter the subject, location, description, status, and start/end times.
4. Click **Create**.

---

## a.7 Creating a Recurring Event

1. Select a date.
2. Click **Create Recurring Event**.
3. Enter event details.
4. Select weekdays (e.g., Mon/Wed/Fri).
5. Choose number of occurrences or an end date.
6. Click **Create**.

---

## a.8 Editing an Event

1. Select a date and choose an event.
2. Click **Edit Event**.
3. Choose an edit scope:
    - Only this event
    - From this onward
    - Entire series
4. Modify the event fields.
5. Click **Save Changes**.

---

## a.9 Viewing All Events

Click **View All Events** to open a read-only dialog showing all events for the selected date.

---

## 3.Important Notes
- End headless command files with `exit` to terminate cleanly.
- Export commands generate files in the working directory (e.g., CSV/ICAL).
- Timezones must follow the `Area/Location` format.
- Invalid input triggers helpful error dialogs.
- Timezones must follow IANA format (e.g., `Asia/Kolkata`, `America/New_York`).
