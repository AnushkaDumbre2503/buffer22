package exceptions;

public class AdEngineException extends RuntimeException {
    private String errorCode;
    
    public AdEngineException(String message) {
        super(message);
    }
    
    public AdEngineException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AdEngineException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AdEngineException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    // Common error codes
    public static class ErrorCodes {
        public static final String DATABASE_ERROR = "DB_ERROR";
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
        public static final String ADVERTISER_NOT_FOUND = "ADVERTISER_NOT_FOUND";
        public static final String AD_NOT_FOUND = "AD_NOT_FOUND";
        public static final String INSUFFICIENT_BUDGET = "INSUFFICIENT_BUDGET";
        public static final String INVALID_INPUT = "INVALID_INPUT";
        public static final String ALLOCATION_FAILED = "ALLOCATION_FAILED";
        public static final String SESSION_EXPIRED = "SESSION_EXPIRED";
        public static final String CONFLICT_ERROR = "CONFLICT_ERROR";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    }
}
