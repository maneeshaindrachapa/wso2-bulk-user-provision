package org.wso2.carbon.bulk.user.provision.csv.exception;

/**
 * Bulk user provision client exception.
 */
public class BulkUserProvisionClientException extends BulkUserProvisionException {

    public BulkUserProvisionClientException(String errorCode, String description) {

        super(errorCode, description);
    }

    public BulkUserProvisionClientException(String errorCode, String description, String message) {

        super(errorCode, description, message);
    }

    public BulkUserProvisionClientException(String errorCode, String description, String message, Throwable cause) {

        super(errorCode, description, message, cause);
    }

    public BulkUserProvisionClientException(String errorCode, String description, Throwable cause) {

        super(errorCode, description, cause);
    }

    public BulkUserProvisionClientException(String message, Throwable cause, boolean enableSuppression,
                                            boolean writableStackTrace, String errorCode, String description) {

        super(message, cause, enableSuppression, writableStackTrace, errorCode, description);
    }
}
