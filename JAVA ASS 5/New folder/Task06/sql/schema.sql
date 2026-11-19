-- Campus Event Registration - MySQL schema
CREATE DATABASE IF NOT EXISTS campus_events;
USE campus_events;

CREATE TABLE IF NOT EXISTS events (
    name VARCHAR(40) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS registrations (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    student_id    VARCHAR(15) NOT NULL,
    student_name  VARCHAR(60) NOT NULL,
    event_name    VARCHAR(40) NOT NULL,
    registered_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- The database itself refuses a second registration of the same student for the same event
    CONSTRAINT uq_student_event UNIQUE (student_id, event_name),
    CONSTRAINT fk_event FOREIGN KEY (event_name) REFERENCES events(name)
);

CREATE INDEX idx_reg_event ON registrations(event_name);

INSERT IGNORE INTO events (name) VALUES ('Music Night'), ('Hackathon'), ('Dance Fest'), ('Quiz Bowl');

-- Sample rows (optional)
INSERT IGNORE INTO registrations (student_id, student_name, event_name) VALUES
 ('CSA2401001', 'Asha Rao',   'Hackathon'),
 ('CSA2401002', 'Ravi Kumar', 'Music Night');
