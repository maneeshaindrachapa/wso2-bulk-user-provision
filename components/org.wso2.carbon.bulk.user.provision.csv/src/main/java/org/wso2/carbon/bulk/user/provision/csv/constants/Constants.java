/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bulk.user.provision.csv.constants;

import org.wso2.carbon.utils.CarbonUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This contains the constants need for bulk user provision.
 */
public class Constants {

    public static final String BULK_USER_PROVISION = "bulkUserProvision";
    public static final String BULK_USER_PROVISION_LOG_PREFIX = "[CUSTOM BULK USER UPLOADER]=====";

    // Default configs
    public static final String DEFAULT_TENANT_DOMAIN = "carbon.super";
    public static final int DEFAULT_NO_OF_ROWS_FETCH = 100;

    // Thread pool configs
    public static final int DEFAULT_BULK_USER_PROVISION_POOL_SIZE = 4;
    public static final int DEFAULT_TIME_TO_WAIT_FOR = 30000;

    // Bulk user provision files dir path
    public static final String BULK_USER_PROVISION_CONFIG_FILE = "bulk-user-provision-config.properties";
    public static final Path BULK_USER_PROVISION_CONFIG_DIR_PATH =
            Paths.get(CarbonUtils.getCarbonHome(), "migration", "bulk-user-provision",
                    BULK_USER_PROVISION_CONFIG_FILE);

    // Config values
    public static final String CONFIG_IS_ENABLED = "isEnabled";
    public static final String CONFIG_IS_PRIMARY_USER_STORE = "isPrimaryUserStore";
    public static final String CONFIG_SECONDARY_USER_STORE_DOMAIN = "secondaryUserStoreDomain";
    public static final String CONFIG_ROWS_TO_FETCH = "noOfRowsFetch";
    public static final String CONFIG_TENANT_DOMAIN = "tenantDomain";
    public static final String CONFIG_USERNAME_FIELD = "usernameField";
    public static final String CONFIG_PASSWORD_FIELD = "passwordField";
    public static final String CONFIG_ROLE_FIELD_EXIST = "roleFieldExist";

    /**
     * Bulk user provision error codes.
     */
    public enum ErrorMessage {

        // Client error codes.
        CLIENT_CONFIG_FILE_NOT_FOUND("BUP-60001", "Bulk user provision config file not found.",
                "Bulk user provision config file not found."),
        CLIENT_CSV_FILES_NOT_FOUND("BUP-60002", "CSV files not found.", "CSV files not found."),
        CLIENT_CSV_FILE_FOLDER_NOT_FOUND("BUP-60003", "Provided user ID is empty.",
                "Provided user ID is empty."),

        // Server error codes.
        SERVER_EVENT_CONFIG_LOADING_ERROR("BUP-65001", "Error while loading Bulk user provision configs.",
                "Error while loading Bulk user provision configs : %s"),
        SERVER_INCOMPATIBLE_USER_STORE_MANAGER_ERROR("BUP-65002", "Incompatible user store manager.",
                "user store manager doesn't support unique Ids."),
        SERVER_UNEXPECTED_ERROR("BUP-65003", "An unexpected server error occurred.",
                "An unexpected server error occurred."),
        SERVER_CONFIG_READING_ERROR("BUP-65004", "An error occurred while reading configurations",
                "An error occurred while reading configurations");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessage(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }

        public String toString() {

            return getCode() + " | " + message;
        }
    }
}
