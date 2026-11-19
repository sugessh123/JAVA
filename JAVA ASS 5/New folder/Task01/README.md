Task 1 – Repaired fee-payment design. `FeeWindow` (Swing) only collects input; `FeeService` validates and pays; `FeeRepository` / `JdbcFeeRepository` / `InMemoryFeeRepository` handle storage.

- Test the logic without a window or database: `javac *.java` then `java FeeServiceDemo` (output in `expected_output.txt`).
- Run the window: create the table with `fees.sql`, add the MySQL connector jar to the classpath, edit the URL/user/password in `FeeWindow.main`, then `java -cp .:mysql-connector-j.jar FeeWindow` (use `;` instead of `:` on Windows).
- Diagnosis of the original code and the design defence are in Assignment_Report.md, Task 1.
