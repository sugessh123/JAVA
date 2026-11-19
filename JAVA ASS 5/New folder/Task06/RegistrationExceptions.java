// One base exception so the window can catch everything with one catch block and show the message.
class RegistrationException extends Exception {
    RegistrationException(String message) { super(message); }
    RegistrationException(String message, Throwable cause) { super(message, cause); }
}

class ValidationException extends RegistrationException {
    ValidationException(String message) { super(message); }
}

class DuplicateRegistrationException extends RegistrationException {
    DuplicateRegistrationException(String message) { super(message); }
}

class DataAccessException extends RegistrationException {
    DataAccessException(String message, Throwable cause) { super(message, cause); }
}
