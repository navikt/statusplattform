package nav.portal.core.exceptionHandling;

public class UserFeedbackException extends RuntimeException {
    public UserFeedbackException(String message) { super(message); }
    public UserFeedbackException(String message, Throwable e) { super(message, e); }
}
