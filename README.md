# Overview

The Calendar MVC Platform supports the creation, management, and querying of calendar events, including both single-instance and recurring events. The system is designed around strong architectural principles rather than UI-first development, making it suitable for extension into CLI, GUI, or service-based interfaces.

# Architecture

The system follows a strict Model–View–Controller (MVC) architecture.

## Model

Owns all calendar and event data
Responsible for:
-Event creation and storage
-Recurring event expansion
-Timezone-aware event handling
-Querying events over date ranges
Completely independent of UI or command logic

## Controller

Acts as the orchestration layer
Parses and validates user commands
Converts raw input into model operations
Computes higher-level analytics and summaries
Ensures business logic does not leak into the model or view

## View

Responsible only for presentation
Supports multiple output formats (text-based, CLI-style output)
Receives pre-processed data from the controller
This separation allows:
-Multiple views over the same model
-Easy replacement of UI layers
-Independent testing of each layer

# Features

## Event Management

Create single (one-time) events
Create recurring events (daily, weekly, custom ranges)
Edit existing events and entire event series
Prevent invalid or conflicting event creation

## Time & Date Handling

Timezone-aware event representation
Accurate event expansion across date ranges
Clear distinction between logical event time and display time

## Command-Based Interaction

Text-based command parsing
Commands mapped cleanly to controller actions
Robust validation and error handling
Designed to scale into CLI or GUI frontends

## Analytics & Queries

Query events within a given date range
Compute calendar summaries and statistics
Designed so analytics logic lives in the controller (not the model)

## Testing & Correctness

Extensive unit testing
Emphasis on edge cases (overlaps, invalid ranges, recurrence boundaries)
Architecture designed for high test coverage and maintainability

# Design Principles Applied

This project intentionally applies core object-oriented and software design principles:
## Single Responsibility Principle (SRP)
Each class has one clear responsibility.
## Open/Closed Principle
New commands, views, or analytics can be added without modifying existing logic.
## Dependency Inversion
Interfaces abstract storage, exporters, and views.
## Immutability Where Appropriate
Event objects favor immutability to avoid state corruption.
## Composition Over Inheritance
Used extensively for command handling and event behavior.

# Technologies & Tools

Language: Java

Build & Testing: JUnit

Architecture: MVC

Design Patterns: Command, Adapter, Builder, Strategy

Version Control: Git & GitHub

# Incremental Development

This project was built iteratively across multiple assignments, closely mirroring how real-world systems evolve:

Initial skeleton and interfaces
Core event modeling
Command parsing and validation
Recurrence handling
Analytics and querying
Testing and refactoring for extensibility

This approach highlights:

disciplined design evolution
ability to refactor safely
long-term architectural thinking

# Future Extensions

The architecture intentionally supports future growth, including:

GUI frontend (Swing / Web)
REST API exposure
Multi-calendar support
Persistent storage (database-backed)
Import/export (ICS format)
User and permission management

# Author

Anuj K. Chobe

Graduate Student, MS in Computer Science

Khoury College of Computer Sciences, Northeastern University

This project reflects my approach to building maintainable, scalable, and well-architected software systems, with a strong focus on correctness and design quality.

# License

This project is for educational and portfolio purposes.
Reuse or extension should respect academic integrity policies and applicable licenses.

***
