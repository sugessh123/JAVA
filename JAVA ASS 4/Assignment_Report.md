# CO4 Java Assignment 4
## Event-Driven GUI Design

**Student Name:** [Enter your name]  
**Register Number:** [Enter your register number]  
**GitHub Link:** [Paste your repository link]  
**Java Version:** JDK 8 or later (reference outputs taken with JDK 21)

> This is a simple student-level explanation. Read the answers, run the programs, and make sure you can explain them during the viva. Items marked **[Run and record]** need your own screenshot or measurement. A screenshot must be linked to the requirement or test it shows, so write the requirement beside each picture.
>
> **Pseudocode note:** The assignment asks for the SIMATS pseudocode standard. That standard document was not available when this report was prepared, so the pseudocode below uses plain structured steps (START, IF, FOR, STOP). Adjust the layout and keywords to the SIMATS standard before submitting.

---

## Task 1 – GUI Design Review

**Code:** `Task01/` (`FeeWindow`, `FeeService`, `FeeRepository`, `JdbcFeeRepository`, `InMemoryFeeRepository`, `FeeServiceDemo`)

### Task 1.1 – Diagnose the Design

| # | Deficiency | What the user experiences / what can go wrong |
|---|---|---|
| 1 | **Interface, logic and database code are mixed** in one method. | Nothing can be tested without opening a window and a database. Any change to the rules means editing the listener. |
| 2 | **No listener is registered and no components are added.** The buttons are created, but there is no `addActionListener(this)` and no `add(...)`, and the layout is not set. | The window shows nothing useful and no click is ever handled. |
| 3 | **`e.getActionCommand() == "Pay"` compares references, not text.** It works only while both strings are the same interned literal. | It breaks as soon as the label is changed, read from a file or translated. `equals()` should be used. |
| 4 | **No validation.** An empty or non-numeric amount makes `Double.parseDouble` throw `NumberFormatException`. Negative amounts and an empty register number are accepted. | The click appears to do nothing (exception printed on the console), or a wrong payment is stored. |
| 5 | **Database work and `Thread.sleep(3000)` run on the event-dispatch thread.** | The window freezes for 3 seconds or more: no repainting, buttons do not respond, and the title bar shows "not responding". |
| 6 | **SQL built by string concatenation.** | A register number such as `x'); DROP TABLE fees;--` changes the statement (SQL injection). A `PreparedStatement` is required. |
| 7 | **Checked exceptions are not handled.** `getConnection`, `executeUpdate` and `sleep` throw checked exceptions, but `actionPerformed` has no try-catch. | As written the class does not compile. If it were fixed carelessly, a database failure would be silent. |
| 8 | **The connection is never closed.** | Every payment leaves a connection open until the database refuses new ones. |
| 9 | **No feedback in the window.** The result is printed with `System.out.println("Paid")`. | The user never sees success or failure, and may click Pay again and pay twice (the button is not disabled). |
| 10 | **The Receipt button has no code.** The `else` branch clears the fields for *both* Clear and Receipt. | Pressing Receipt erases what the user typed. |
| 11 | **The window cannot be closed.** An AWT `Frame` needs a `WindowListener`. | Clicking the X does nothing. |

### Task 1.2 – Repair the Design

**Class structure**
```text
FeeWindow (Swing JFrame)  --uses-->  FeeService  --uses-->  FeeRepository (interface)
 only input and output               validation, payment,        |            |
 SwingWorker for long work           receipt                JdbcFeeRepository  InMemoryFeeRepository
                                                             (MySQL, PreparedStatement) (for tests)
```

**Rewritten event handling** (full code in `Task01/FeeWindow.java`):
```java
pay.addActionListener(e -> doPay());                       // one listener per button
clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); message.setText(" "); });
receipt.addActionListener(e -> showReceipt());

private void doPay() {
    pay.setEnabled(false);                                 // prevents a double payment
    message.setText("Waiting for bank confirmation...");
    new SwingWorker<String, Void>() {
        protected String doInBackground() throws Exception { return service.pay(r, a); }   // not on the EDT
        protected void done() { /* show receipt text or the error message, enable Pay */ }
    }.execute();
}
```
`FeeService.pay()` validates the input, saves with a `PreparedStatement`, and throws `FeeException` with a clear message. `JdbcFeeRepository` closes the connection with try-with-resources.

**Logic test** (`java FeeServiceDemo`, output in `Task01/expected_output.txt`):
```text
OK    -> RECEIPT: CSA2401001 paid Rs 5000.00
ERROR -> Register number is required
ERROR -> Amount must be a number  (cause: NumberFormatException)
ERROR -> Amount must be greater than zero
ERROR -> Register number must be 6-15 letters or digits       <- the SQL-injection style input is rejected
ERROR -> Payment could not be saved. Please try again.  (cause: SQLException)
OK    -> RECEIPT: CSA2401002 paid Rs 2500.50
```
**[Run and record]** a screenshot of `FeeWindow` showing (a) a successful payment, (b) an invalid amount message and (c) the Receipt dialog, each linked to the requirement it shows.

### Task 1.3 – Design Defence (under 100 words)
The window only reads fields and shows messages, while `FeeService` holds every rule and uses an interface for storage. So validation and payment can be tested with `FeeServiceDemo` and an in-memory repository, with no window and no database. Changing the database, the validation rule or the screen affects one class only. Long work runs in a `SwingWorker`, so the window stays responsive, and each button has its own small listener, so a change to one action cannot break another.

### Alternative Concept and Complexity Analysis
**One listener per button (or a Swing `Action`):** Each button gets its own object, as in the repaired code (`pay.addActionListener(e -> doPay())`). With a Swing `Action`, one object stores the name, the code and the enabled state, and can be shared by a button and a menu item.

| | One listener with if-else on the action command | One listener per button |
|---|---|---|
| Time to dispatch an event for k buttons | Up to k string comparisons, **O(k)** | The event goes straight to the registered listener, **O(1)** (in the comparison sense) |
| Space | One listener object, O(1), but one growing method | k small listener objects, **O(k)** |

For a window with a few buttons the time difference is not noticeable. The main gain is maintainability: adding a button means adding one listener, not editing a long if-else chain.

---

## Task 2 – From Requirement to Event-Driven Design

**Code:** `Task02/` (`DoctorDirectory`, `AppointmentService`, `AppointmentDesk`, `DeskDemo`)

### Task 2.1 – Model the Events

| Event source | Event object | Listener interface | Action taken |
|---|---|---|---|
| Department `JComboBox` | `ItemEvent` | `ItemListener` | Replace the doctor list with the doctors of the chosen department (react only when the state is `SELECTED`) |
| Doctor `JComboBox` | `ActionEvent` (not needed here) | – | Selection is only read when the booking button is pressed |
| Book `JButton` | `ActionEvent` | `ActionListener` | Validate and book; show confirmation or error |
| Patient ID `JTextField` (optional) | `ActionEvent` (Enter key) | `ActionListener` | Same action as the Book button |
| Frame (close button) | `WindowEvent` | `WindowListener` / `WindowAdapter` (`windowClosing`) | If a booking is in progress, ask for confirmation before `dispose()` |
| Confirmation dialog `JOptionPane` | Return value (`YES_OPTION` / `NO_OPTION`) | – | Close or stay open |

### Task 2.2 – Component Hierarchy
```text
JFrame  "Outpatient Appointment Desk"            (GridLayout 6 x 2)
 ├── JLabel "Patient ID"        ── JTextField patientId
 ├── JLabel "Department"        ── JComboBox<String> department
 ├── JLabel "Doctor"            ── JComboBox<Doctor> doctor
 ├── JLabel "Time slot"         ── JComboBox<String> slot
 └── JButton "Book"             ── JLabel message
JOptionPane (confirm dialog)    shown from windowClosing
```
Classes used: `JFrame`, `JLabel`, `JTextField`, `JComboBox`, `JButton`, `JOptionPane`. The AWT equivalents would be `Frame`, `Label`, `TextField`, `Choice` and `Button`.

### Task 2.3 – Handler Logic (pseudocode)

**(a) Refresh the doctor list when the department changes**
```text
START departmentChanged(event)
    IF event state is not SELECTED THEN
        RETURN                          // ignore the DESELECTED event
    END IF
    department <- selected item of department list
    clear the doctor list
    doctors <- directory.get(department)    // Map lookup, loaded once
    FOR each doctor in doctors
        add doctor to the doctor list
    END FOR
STOP
```

**(b) Validate and book**
```text
START bookClicked(event)
    id <- text of patient ID field
    IF id is not 5 to 8 digits THEN show "Error: patient ID must be 5 to 8 digits"; RETURN
    IF no department selected   THEN show "Error: select a department"; RETURN
    IF no doctor selected       THEN show "Error: select a doctor"; RETURN
    IF no slot selected         THEN show "Error: select a time slot"; RETURN
    key <- doctor name + slot
    IF key already in booked set THEN show "Error: doctor already booked"; RETURN
    add key to booked set
    show "Booked: patient, doctor, slot"
STOP
```
**Logic test** (`java DeskDemo`, `Task02/expected_output.txt`) shows the doctor map and all booking outcomes (normal, same doctor and slot twice, bad ID, no department, no doctor).

### Task 2.4 – Trace the Event Sequence

1. `main` → `SwingUtilities.invokeLater(...)` → constructor builds the components and registers the listeners → `setVisible(true)`.
2. On the event-dispatch thread: `windowOpened`, then `windowActivated`.
3. The user chooses a department: `itemStateChanged` is called twice (first `DESELECTED` for the old item, then `SELECTED` for the new one); only the second one refreshes the doctors.
4. The user presses Book: `actionPerformed`.
5. The user clicks the close button: `windowClosing` → confirmation dialog → `dispose()` → `windowDeactivated` and `windowClosed` (the order of these last two may differ on some platforms; **[Run and record]** the actual order by printing a line in each method of a `WindowListener`). If the window is minimised and restored, `windowIconified`/`windowDeactivated` and `windowDeiconified`/`windowActivated` occur in between.

**Compared with an applet:** An applet's life cycle is controlled by the browser or applet viewer: `init()` once when loaded, `start()` when the page becomes visible, `paint()` whenever the drawing must be refreshed, `stop()` when the page is left, and `destroy()` when the applet is unloaded. A Frame is controlled by the program itself and by window events (opened, activated, iconified, closing, closed). `paint()` exists in both, and in both the system may call it at any time. Applets are deprecated in current Java, so new programs use Swing windows.

### Alternative Concept and Complexity Analysis
**Fetching doctors from the database on each department change:**
```java
List<Doctor> doctors = dao.findDoctors(department);   // SELECT ... WHERE department = ?
```

| | Doctors loaded once into `Map<String, List<Doctor>>` | Query at every change |
|---|---|---|
| Time per department change | Hash lookup, **O(1)** average, plus O(m_d) to fill the combo box for the m_d doctors of that department | Network and query time, plus O(m_d) to read and fill the combo box (a typical database call takes milliseconds, a map lookup takes microseconds) |
| Memory for d departments and m doctors | **O(d + m)** all the time | Only the current department's list, **O(m_d)** at a time |
| Data freshness | Old until reloaded | Always current |

The map is faster and simple for a list that rarely changes. The query approach saves memory and shows new doctors immediately, but needs the database during every change and should run in a `SwingWorker`.

---

## Task 3 – From Sketch to Layout

**Code:** `Task03/` (`LibraryPanel`, `LibrarySearchScreen`, `LayoutScreenshots`)

### Task 3.1 – Choose the Layout Managers

| Container | Layout manager | Justification for the resizing requirement |
|---|---|---|
| Root panel | `BorderLayout` | NORTH and SOUTH keep their preferred height; **CENTER takes all remaining space**, so the results area grows. |
| North (title + search row) | `GridLayout(2,1)` | Stacks two rows of equal height; the height stays fixed because it is the NORTH region. |
| Title bar | `FlowLayout(CENTER)` | Centres the title at its normal size. |
| Search row | `FlowLayout(LEFT)` | Text field, Choice and Search button keep their preferred sizes; they are never stretched. |
| Results area | `JScrollPane` in CENTER | Grows and shrinks with the window and scrolls when needed. |
| South (buttons + status) | `GridLayout(2,1)` | Button row above, status message below, fixed height. |
| Button row | `FlowLayout(CENTER)` | Prev, Next and Reserve keep their size and stay centred. |

`CardLayout` is not needed because the screen is a single view (it is useful for switching between several screens).

### Task 3.2 – Nested Containers
```text
LibraryPanel (BorderLayout)
 ├── NORTH   : JPanel (GridLayout 2x1)
 │              ├── titleBar  : JPanel (FlowLayout CENTER) ── JLabel "Campus Library Search"
 │              └── searchRow : JPanel (FlowLayout LEFT)   ── JLabel, JTextField, JComboBox, JButton "Search"
 ├── CENTER  : JScrollPane ── JTextArea (results)
 └── SOUTH   : JPanel (GridLayout 2x1)
                ├── buttonRow : JPanel (FlowLayout CENTER) ── JButton Prev, Next, Reserve
                └── JLabel status
```

### Task 3.3 – Implementation
See `Task03/LibraryPanel.java` and `LibrarySearchScreen.java`. The layout code is in `LibraryPanel`, and the window only places it in a `JFrame`. The Search button (or Enter) fills the results area, Prev/Next page through the results, and Reserve updates the status message.

### Task 3.4 – Resizing Test
The layout was drawn off-screen at three sizes by `LayoutScreenshots.java` (`Task03/screenshots/`). The measured sizes are in `Task03/layout_report.txt`:

| Window size | Results area | Search button | Text field | Screenshot |
|---|---|---|---|---|
| 500 × 320 | 484 × 148 | 75 × 25 | 224 × 19 | `resize_500x320.png` |
| 800 × 500 | 784 × 328 | 75 × 25 | 224 × 19 | `resize_800x500.png` |
| 1100 × 700 | 1084 × 528 | 75 × 25 | 224 × 19 | `resize_1100x700.png` |

**Result:** The results area grew from 484 × 148 to 1084 × 528 pixels, while the search row controls and the button row kept exactly the same size at every window size. This is the designed behaviour. These pictures show the layout managers at work, but they were drawn off-screen. **[Run and record]** your own three screenshots of the real window by dragging its corner, and write the requirement ("results area grows, other regions keep their size") next to each.

### Alternative Concept and Complexity Analysis
**GridBagLayout** could build the same screen in one container: the search row, results and buttons are cells with weights (`weighty = 1` for the results). **Absolute positioning** (`setLayout(null)` with `setBounds`) fixes each component at a pixel position.

| | Nested BorderLayout / FlowLayout / GridLayout | GridBagLayout | Absolute positioning |
|---|---|---|---|
| Resizing behaviour | Correct with no extra code | Correct if weights and fills are set correctly | **Nothing moves or grows**: the results area stays the same size unless resize code is written |
| Layout computation | Each container does a simple pass over its children, **O(n)** in total for n components; BorderLayout alone handles at most 5 children | One grid calculation, usually **O(n)** with a larger constant (it needs a constraint object per component) | **O(1)**, no calculation, because `setBounds` fixes the position |
| Container objects (space) | 6 panels in this design | 1 panel, but n `GridBagConstraints` objects | 1 panel |
| Other issues | More panels, simple code | Code is long and easy to get wrong | Breaks with different screen sizes, fonts and languages |

---

## Task 4 – Event-Handling Forensics

**Code:** `Task04/` (`buggy/DrawPad.java`, `DrawPad.java`, `compile_error_output.txt`)

### Task 4.1 – Locate the Defect
- **Why the click is never handled:** The method is written as `mouseclicked` (small `c`). `MouseAdapter` has a method called `mouseClicked` (capital `C`). Java is case sensitive, so `mouseclicked` is just a new, unrelated method that nobody calls. The empty `mouseClicked` of `MouseAdapter` runs instead. There is no compiler error, because adding a new method is legal.
- **Why the label never changes:** The handler body (even if it ran) only does `count++` and draws; there is **no `status.setText(...)` call anywhere**, so the label can never change. There are therefore two defects: the wrong method name, and the missing label update.
- (Also: the dot would be drawn with `getGraphics()`, which is the next problem; and the frame has no window listener, so it cannot be closed with the X button.)

### Task 4.2 – A Failing Interaction
**Interaction:** After fixing the method name, click in several places on the window (dots appear), then minimise the window and restore it (or drag another window over it). The dots are gone, although the label still shows the old count.

**Why:** `getGraphics()` draws directly on the screen once. Java does not remember what was drawn. When the window is restored, the system calls `paint(Graphics)` to redraw it, and `paint()` does not know about the dots, so they are not redrawn. Drawing must be done inside `paint()` from stored data.

### Task 4.3 – Repair
Completed program: `Task04/DrawPad.java`. The points are stored in a `List<Point>`.
```java
@Override public void mouseClicked(MouseEvent e) {      // TODO 1
    points.add(e.getPoint());
    status.setText("Clicks: " + points.size());
    repaint();
}
@Override public void paint(Graphics g) {               // TODO 2
    super.paint(g);
    for (Point p : points) g.fillOval(p.x - 3, p.y - 3, 6, 6);   // 6 x 6 circle centred on the click
}
```
`repaint()` asks the system to call `paint()` again, and `paint()` redraws every stored point, so the dots survive minimising, restoring and resizing. The program was compiled successfully, but it could not be displayed when this report was prepared. **[Run and record]** screenshots (1) after several clicks and (2) after minimising and restoring, each linked to the requirement "dots survive repainting".

### Task 4.4 – Prevent Recurrence
The `@Override` annotation tells the compiler "this method must replace a method of the superclass". With `@Override` on the misspelt method, the compiler stops. The message produced on JDK 21 (saved in `Task04/compile_error_output.txt`):
```text
DrawPad.java:11: error: method does not override or implement a method from a supertype
            @Override public void mouseclicked(MouseEvent e) {
            ^
1 error
```
The same experiment as the assignment asks: correct spelling and `@Override` compile; the misspelt name with `@Override` fails. Without `@Override` it compiles and fails silently.

### Alternative Concept and Complexity Analysis
**Off-screen `BufferedImage`:** Create an image the size of the window. In the click handler, draw the dot onto the image (`img.getGraphics().fillOval(...)`) and call `repaint()`. In `paint()`, only `g.drawImage(img, 0, 0, null)` is needed.

| | Store points, redraw in `paint()` | Draw once on a `BufferedImage` |
|---|---|---|
| Time per repaint for n clicks | **O(n)** (one `fillOval` per point) | Copying one image, **O(W × H)** pixels, independent of n |
| Space | **O(n)** points (about 24 bytes per `Point` object plus the list reference) | **O(W × H)** pixels: 4 bytes per pixel, about 480 KB for 400 × 300 |
| Resizing | Works at any size | The image must be re-created and copied when the window grows |
| Extra feature | Easy to undo or remove a point | Cannot remove one dot without redrawing everything |

The stored-points design is cheaper while n is small. After roughly 15,000–20,000 points (an estimate, using the sizes above), the image copy becomes cheaper per repaint.

---

## Task 5 – Same Screen, Different Event-Handling Styles

**Code:** `Task05/` (`ConverterA`–`ConverterD`, `part2/`, `Converter.java`, `ConverterLogic`)

### Task 5.1 – Four Implementations
All four use the same `ConverterLogic.convert(amount, from, to)` and handle closing with a `WindowAdapter`.

| Style | File | How the listener is written |
|---|---|---|
| (a) Frame implements `ActionListener` | `ConverterA.java` | `convert.addActionListener(this);` and `actionPerformed` in the frame |
| (b) Named inner class | `ConverterB.java` | `private class ConvertListener implements ActionListener` |
| (c) Anonymous inner class | `ConverterC.java` | `new ActionListener() { public void actionPerformed(...) { ... } }` |
| (d) Lambda | `ConverterD.java` | `convert.addActionListener(e -> result.setText(...))` |

**Logic test** (`java ConverterLogicDemo`, `Task05/expected_output.txt`):
```text
12.00 USD
11.00 EUR
1000.00 INR
12.00 USD
Invalid amount
Invalid amount
```
All four files compile (also in `part2/`), but the windows could not be displayed when this report was prepared. **[Run and record]** a screenshot of each style working.

### Task 5.2 – Change the Requirement (Enter key and a dialog)
The changed programs are in `Task05/part2/`. The number of changed lines was measured with `diff` (`Task05/change_effort.txt`):

| Style | Lines removed | Lines added | What had to change |
|---|---|---|---|
| (a) Frame implements | 1 | 2 | Add `amount.addActionListener(this)`; replace `setText` by `JOptionPane.showMessageDialog` |
| (b) Named inner class | 1 | 2 | Add `amount.addActionListener(new ConvertListener())`; change the dialog line |
| (c) Anonymous class | 3 | 5 | The listener had to be stored in a variable so that it could be registered twice |
| (d) Lambda | 1 | 3 | Store the lambda in a variable, register it on the button and the field |

All four needed only a small change. The anonymous class needed the most, because it has no name and cannot be reused without first putting it in a variable.

### Task 5.3 – Compare the Styles

| | (a) Frame implements | (b) Named inner class | (c) Anonymous | (d) Lambda |
|---|---|---|---|---|
| Readability | Poor with many buttons (one big method) | Good, each handler has a name | Good for short code, noisy for long code | Very good for short code |
| Access to the frame's fields | Direct | Direct (inner class) | Direct | Direct |
| Reuse of the handler | One handler for the whole frame | Easy: `new ConvertListener()` anywhere | Hard without a variable | Easy with a variable or method |
| Ease of change | Needs an if-else chain for more buttons | Easy | Moderate | Easy |

**For a large application** I would use lambdas that only call a method of a controller (`e -> controller.convert()`), or `Action` objects when the same command is used from several places (button, menu, keyboard). The listener code stays one line, and the real logic is in a named, testable method. Inner classes remain suitable when a handler needs its own fields or is long. Having the frame implement the listener is the least suitable choice for a large screen, because all buttons share one `actionPerformed`.

### Complete the Code
`Task05/Converter.java` has both TODOs completed:
```java
ActionListener listener = e -> convert();       // TODO 1: one lambda for the button and the Enter key
convertButton.addActionListener(listener);
amount.addActionListener(listener);

try {                                           // TODO 2
    double inr = Double.parseDouble(amount.getText().trim());
    String currency = (String) target.getSelectedItem();
    result.setText(String.format("%.2f %s", inr * rate(currency), currency));
} catch (NumberFormatException ex) {
    result.setText("Invalid amount");
}
```
Expected behaviour: 1000 with USD gives `12.00 USD`; 1000 with EUR gives `11.00 EUR`; "abc" gives `Invalid amount`. **[Run and record]** screenshots of a valid conversion and an invalid amount. (The formula is the same as in `ConverterLogic`, which was tested, but the window itself could not be displayed here.)

### Alternative Concept and Complexity Analysis
- **Method reference `this::convert`:** `ActionListener.actionPerformed` takes an `ActionEvent`, so `this::convert` compiles only if `convert` is written as `convert(ActionEvent e)`. In the given program `convert()` has no parameter, so `e -> convert()` is needed (or the method signature must change).
- **One shared `Action` object:** `Action convertAction = new AbstractAction("Convert") { public void actionPerformed(ActionEvent e) { convert(); } };` can be given to a button (`new JButton(convertAction)`), a menu item and a key binding.

| | Styles (a)–(d) as implemented | Method reference | Shared `Action` |
|---|---|---|---|
| Listener objects (space) | 1 per registration, or 1 if stored in a variable | 1 per registration | **One object** for all triggers, O(1) |
| Work to add a new trigger (menu item, shortcut) | Register the listener again, or create another listener | One new registration line | `new JMenuItem(convertAction)` – one line; the name and the enabled state are shared |

The `Action` approach needs the least work when more triggers are added.

---

## Task 6 – Design Challenge: Campus Event Registration Desktop Application

**Code:** `Task06/` (see the README there). SQL: `Task06/sql/schema.sql`.

### Stage 1 – Interpret the Problem

**Users:** Students (give their details), committee members (view, filter, export).  
**Screens:** (1) Main window with a registration form, a filter, and a table; (2) dialogs for messages, About, and the Export file chooser.  
**Data:** `events(name)`; `registrations(id, student_id, student_name, event_name, registered_at)` with a UNIQUE key on (`student_id`, `event_name`).  
**Rules and testable requirements**

| # | Requirement (testable) |
|---|---|
| R1 | A registration with a valid student ID, name and event is stored and appears in the table. |
| R2 | An empty or invalid student ID (not 6–15 letters/digits), an invalid name, or an unknown event is rejected with a clear message and nothing is stored. |
| R3 | The same student cannot register twice for the same event (message shown, no second row); the same student can register for a different event. |
| R4 | Choosing an event in the filter shows only that event's registrations. |
| R5 | The table loads one page (20 rows) at a time; Prev/Next work. |
| R6 | While data is loading or saving, the window still responds. |
| R7 | The filtered list can be exported to a CSV file. |

### Stage 2 – Screens and Event Flow

**Main window**
```text
+-------------------------------------------------------------------------+
| File  Help                                                              |   JMenuBar
+-------------------------------------------------------------------------+
| Student ID [_______]  Name [___________]  Event [v]  [Register]         |   form (FlowLayout)
| Show event: [All events v]                                              |   filter
+-------------------------------------------------------------------------+
| ID | Student ID | Name | Event | Registered at                          |   JTable in JScrollPane
| ...                                                                     |
+-------------------------------------------------------------------------+
|               [Prev]   Page 1 of 3 (45 rows)   [Next]                   |   paging
| Status message                                                          |
+-------------------------------------------------------------------------+
```
Menu: **File** → Export CSV..., Exit; **Help** → About.

**Flow between screen and dialogs**
```text
Main window --Register--> (success: status message + table refresh) / (error: status message)
Main window --Filter / Prev / Next--> table reloads (page 1 after a new filter)
Main window --File > Export CSV...--> JFileChooser --OK--> CSV written (status message)
Main window --Help > About--> message dialog --OK--> Main window
Main window --File > Exit or close button--> application closes
```

### Stage 3 – Critical Design Decisions

| Decision | Where it happens |
|---|---|
| Input validation | `RegistrationService.register()`: the window does not repeat the rules. |
| Duplicate check | The **database** UNIQUE constraint is the final guard (safe even with two desks registering at the same moment). `JdbcRegistrationDao` converts MySQL error 1062 into `DuplicateRegistrationException`, which the window shows as a clear message. A "check first, then insert" would not be safe on its own, because two users could pass the check at the same time. |
| Database access | Only in `JdbcRegistrationDao`, with `PreparedStatement` and try-with-resources. |
| Keeping long work off the event-dispatch thread | Every call from the window (load events, register, load page, export) runs in `SwingWorker.doInBackground()`. The table and labels are updated only in `done()`, which runs on the event-dispatch thread. The Register button is disabled while a registration is saving. |

### Stage 4 – Classes and Handlers

**Class diagram (three layers)**
```text
 UI layer                    Service layer                     Data-access layer
+------------------+      +-----------------------+       +-----------------------------+
| RegistrationApp  | ---> | RegistrationService   | --->  | <<interface>> RegistrationDao|
| (JFrame, JTable, |      | register()            |       | events() insert() page()    |
|  JMenuBar,       |      | page()  exportCsv()   |       | count()                     |
|  SwingWorker)    |      | validation rules      |       +--------------+--------------+
+------------------+      +-----------------------+                      |      |
                                   |                        JdbcRegistrationDao  InMemoryRegistrationDao
                          Registration (model)              (MySQL, JDBC)         (tests)
                          RegistrationException <- ValidationException, DuplicateRegistrationException, DataAccessException
```

**Pseudocode – Register action**
```text
START registerClicked
    disable Register button
    read id, name, event from the form
    start background task:
        IF id is empty or not 6-15 letters/digits THEN throw ValidationException
        IF name is empty or not 2-60 letters/spaces/dots THEN throw ValidationException
        IF event is not in the list of events THEN throw ValidationException
        INSERT (id, name, event) using a prepared statement
        IF database reports a duplicate key THEN throw DuplicateRegistrationException
        IF any other database error THEN throw DataAccessException
    when the task finishes (on the event-dispatch thread):
        IF it succeeded THEN show success message, clear the fields, reload the table
        ELSE show the error message
        enable Register button
STOP
```
**Pseudocode – Filter action**
```text
START filterChanged
    pageIndex <- 0
    filter <- selected event (or none for "All events")
    start background task:
        rows  <- SELECT ... [WHERE event_name = filter] ORDER BY id LIMIT 20 OFFSET pageIndex * 20
        total <- SELECT COUNT(*) ... [WHERE event_name = filter]
    when the task finishes (on the event-dispatch thread):
        replace the table rows with rows
        update "Page x of y", enable or disable Prev and Next
STOP
```

### Stage 5 – Implementation and Demonstration
The application uses `JFrame`, `JPanel`, `JTextField`, `JComboBox`, `JTable`, `JMenuBar`, `SwingWorker` and JDBC. All code compiles.

**Demonstration of the normal, invalid and duplicate cases** with the same service and an in-memory DAO (`java RegistrationServiceDemo`, `Task06/expected_output.txt`):

| Test | Result |
|---|---|
| Normal (R1) | `OK CSA2401001 -> Hackathon` |
| Duplicate (R3) | `DUPLICATE Student CSA2401001 is already registered for Hackathon` |
| Same student, other event (R3) | `OK CSA2401001 -> Music Night` |
| Empty ID (R2) | `INVALID Student ID is required` |
| ID too short (R2) | `INVALID Student ID must be 6-15 letters or digits` |
| Bad name (R2) | `INVALID Name must be 2-60 letters, spaces or dots` |
| Unknown event (R2) | `INVALID Select a valid event` |
| Paging (R5) | 4 rows, page size 3 → 2 pages (3 rows, then 1 row) |
| Filter (R4) | Hackathon only: 2 rows |
| Export (R7) | CSV text with a header and the 2 Hackathon rows |

**Not tested here:** The Swing window and the MySQL code (`JdbcRegistrationDao`) could not be run when this project was prepared (no desktop and no MySQL server were available). They compile, and they follow standard JDBC rules (prepared statements, try-with-resources, MySQL duplicate-key error 1062). **[Run and record]** with a real MySQL database: run `schema.sql`, start the application, and take screenshots of (1) a normal registration appearing in the table (R1), (2) an invalid input message (R2), (3) a duplicate message (R3), (4) the filter (R4), (5) the Export file chooser and the resulting file (R7). Also run `SELECT * FROM registrations;` in MySQL to prove the duplicate was not stored.

### Alternative Concept and Complexity Analysis
**Load everything at once** (`SELECT * FROM registrations` into the `JTable`) instead of one page at a time with `LIMIT 20 OFFSET n` in a `SwingWorker`.

**Measurement** (`java LoadAllVsPage`, `Task06/load_measurement_output.txt`, in-memory data):
```text
Load all 100,000 rows : 48 ms, about 9 MB extra heap, rows in table = 100000
Load one page of 50   : 0.42 ms, about 0 MB extra heap, rows in table = 50
```
This measures only the time to fill the table model. The rows reuse the same strings as the source list, so the memory figure is a **lower bound**; rows read from a real database would create new strings for every cell, and the memory would be higher. Network and database time are not included, and they would be added for real MySQL.

| | Load all 100,000 rows | One page at a time (`LIMIT`/`OFFSET`) |
|---|---|---|
| Loading time | Query and transfer of all N rows, **O(N)**, plus filling the model (48 ms here) before the first row can be seen | Only one page of P rows per request, **O(P)** for the transfer. A large `OFFSET` makes MySQL skip the earlier rows, so a deep page costs more, **O(offset + P)** inside the database |
| Memory (N = 100,000) | **O(N)** objects in the program all the time (tens of MB for real rows) | **O(P)**: only about 20–50 rows |
| Responsiveness | If the query runs on the event-dispatch thread the window freezes until it finishes; even in a `SwingWorker`, putting 100,000 rows into the model at once happens on the event-dispatch thread and can pause the window | The window stays responsive: each page loads quickly in a `SwingWorker` |
| Other | Sorting and searching in the table are instant after loading | Each filter or page change needs a new query |

Paging is the better choice for 100,000 registrations. If deep pages become slow, "keyset paging" (`WHERE id > last_id LIMIT 20`) avoids the large offset.

---

## MARK DISTRIBUTION

| Task | Competency Assessed | Bloom's Level | Marks |
|---|---|---|---|
| Task 1 | Diagnose and repair a poor event-handling design | K4 – Analyse | 6 |
| Task 2 | Design an event-driven interface from a requirement | K4 – Analyse | 8 |
| Task 3 | Design and implement layouts | K4 – Analyse | 9 |
| Task 4 | Debug event handling using failing interactions | K4 – Analyse | 7 |
| Task 5 | Compare event-handling styles under changing requirements | K5 – Evaluate | 7 |
| Task 6 | Independently design and build a layered GUI–JDBC application | K5 – Evaluate | 13 |
| | **TOTAL** | | **50** |

**Assessment verification:** You may be asked to explain any part of your design or code, trace an event through your handlers, or modify a handler during a viva.
