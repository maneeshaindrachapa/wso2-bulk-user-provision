package org.wso2.carbon.bulk.user.provision.csv.exception;

/**
 * Bulk user provision server exception.
 */
public class BulkUserProvisionServerException extends BulkUserProvisionException {

    public BulkUserProvisionServerException(String errorCode, String description) {

        super(errorCode, description);
    }

    public BulkUserProvisionServerException(String errorCode, String description, String message) {

        super(errorCode, description, message);
    }

    public BulkUserProvisionServerException(String errorCode, String description, String message, Throwable cause) {

        super(errorCode, description, message, cause);
    }

    public BulkUserProvisionServerException(String errorCode, String description, Throwable cause) {

        super(errorCode, description, cause);
    }

    public BulkUserProvisionServerException(String message, Throwable cause, boolean enableSuppression,
                                            boolean writableStackTrace, String errorCode, String description) {

        super(message, cause, enableSuppression, writableStackTrace, errorCode, description);
    }
}
