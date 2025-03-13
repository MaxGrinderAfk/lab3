package idespring.lab3.exceptions;

public class SubjectNotAssignedException extends RuntimeException {
    public SubjectNotAssignedException(String message) {
        super(message);
    }
}
