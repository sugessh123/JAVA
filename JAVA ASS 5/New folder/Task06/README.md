Task 6 – Campus Event Registration (Swing + JDBC + MySQL), in three layers.

- UI: `RegistrationApp.java` – service: `RegistrationService.java` – data access: `RegistrationDao.java`, `JdbcRegistrationDao.java` (MySQL), `InMemoryRegistrationDao.java` (tests).
- SQL script: `sql/schema.sql` (the UNIQUE constraint prevents duplicate registrations).
- Test without a window or MySQL: `javac *.java` then `java RegistrationServiceDemo` (output in `expected_output.txt`).
- `java LoadAllVsPage` measures filling a table with 100,000 rows versus one page (`load_measurement_output.txt`).
- Run the real application: run `sql/schema.sql` in MySQL, download MySQL Connector/J, then
  `java -cp .:mysql-connector-j-<version>.jar RegistrationApp jdbc:mysql://localhost:3306/campus_events root <password>` (use `;` instead of `:` on Windows).
  The JDBC code was written to standard JDBC and MySQL rules but could not be run against a real MySQL server when this project was prepared, so test it yourself and record the result.
