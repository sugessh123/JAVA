# CO4 Java Assignment 4 – Event-Driven GUI Design

**Student Name:** [Enter your name]  
**Register Number:** [Enter your register number]  
**Java Version:** JDK 8 or later (tested with JDK 21)  
**Repository name:** `CSA09-<RegisterNumber>-Assignment4`

## Project contents
- `Assignment_Report.md` – diagnoses, tables, diagrams, pseudocode and comparisons for Tasks 1–6.
- `Task01` – repaired fee-payment design (window, service, repository).
- `Task02` – outpatient appointment desk.
- `Task03` – library search screen and resizing screenshots.
- `Task04` – DrawPad defect, compiler message and completed program.
- `Task05` – four event-handling styles, changed versions, completed lambda converter.
- `Task06` – layered Swing + JDBC event registration application and SQL script.
- Each task folder contains a short `README.md` and output text where applicable.

## How to compile and run
Open a terminal in a task folder.
```bash
cd Task01
javac *.java
java FeeServiceDemo        # logic test, no window or database needed
```

| Task | Logic test (no window) | Window to run |
|---|---|---|
| Task01 | `java FeeServiceDemo` | `java FeeWindow` (needs MySQL + connector jar) |
| Task02 | `java DeskDemo` | `java AppointmentDesk` |
| Task03 | `java LayoutScreenshots` | `java LibrarySearchScreen` |
| Task04 | – | `java DrawPad` |
| Task05 | `java ConverterLogicDemo` | `java ConverterA`, `ConverterB`, `ConverterC`, `ConverterD`, `Converter` |
| Task06 | `java RegistrationServiceDemo`, `java LoadAllVsPage` | `java -cp .:mysql-connector-j.jar RegistrationApp <url> <user> <password>` |

## Before submitting
1. Replace the student name and register number in this file and in the report, and add your GitHub link.
2. **Run every program on your own computer.** The windows could not be opened when this project was prepared (no desktop was available), so every window program was compiled but not displayed. Run each one and take your own screenshots.
3. Each screenshot must be linked to the requirement or test it shows. Write the requirement next to the picture in the report.
4. Run `Task06/sql/schema.sql` in MySQL and test Task 6 against the real database.
5. Create the GitHub repository with the required name, share it with the faculty and make at least one commit for each task.

**Note:** The `*_output.txt` files and `expected_output.txt` are reference outputs of the logic tests. Do not claim that screenshots were taken if you have not taken them.
