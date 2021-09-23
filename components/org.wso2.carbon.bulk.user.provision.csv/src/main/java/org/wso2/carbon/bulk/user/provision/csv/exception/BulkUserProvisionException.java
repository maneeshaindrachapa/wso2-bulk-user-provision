package org.wso2.carbon.bulk.user.provision.csv.exception;

/**
 * Bulk user provision exception.
 */
public class BulkUserProvisionException extends Exception {

    private String errorCode;
    private String description;

    public BulkUserProvisionException(String errorCode, String description) {

        this.errorCode = errorCode;
        this.description = description;
    }

    public BulkUserProvisionException(String errorCode, String description, String message) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    public BulkUserProvisionException(String errorCode, String description, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    public BulkUserProvisionException(String errorCode, String description, Throwable cause) {

        super(cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    public BulkUserProvisionException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace, String errorCode, String description) {

        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
        this.description = description;
    }
}
