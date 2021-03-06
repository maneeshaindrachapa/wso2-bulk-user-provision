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
    public static final String DEFAULT_USERNAME_FIELD = "username";
    public static final String DEFAULT_PASSWORD_FIELD = "password";
    public static final String DEFAULT_ROLE_FIELD = "role";
    public static final int DEFAULT_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN = 3000;
    public static final int DEFAULT_BULK_USER_PROVISION_POOL_SIZE = 4;

    // Bulk user provision files dir path
    public static final String BULK_USER_PROVISION_CONFIG_FILE = "bulk-user-provision-config.properties";
    public static final Path BULK_USER_PROVISION_CONFIG_DIR_PATH_FILE =
            Paths.get(CarbonUtils.getCarbonHome(), "migration", "bulk-user-provision",
                    BULK_USER_PROVISION_CONFIG_FILE);
    public static final Path BULK_USER_PROVISION_CSV_DIR_PATH =
            Paths.get(CarbonUtils.getCarbonHome(), "migration", "bulk-user-provision");

    // Config values
    public static final String CONFIG_IS_ENABLED = "isEnabled";
    public static final String CONFIG_IS_PRIMARY_USER_STORE = "isPrimaryUserStore";
    public static final String CONFIG_SECONDARY_USER_STORE_DOMAIN = "secondaryUserStoreDomain";
    public static final String CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN = "waitingTimeForSecondaryUserStore";
    public static final String CONFIG_ROWS_TO_FETCH = "noOfRowsFetch";
    public static final String CONFIG_TENANT_DOMAIN = "tenantDomain";
    public static final String CONFIG_USERNAME_FIELD = "usernameField";
    public static final String CONFIG_PASSWORD_FIELD = "passwordField";
    public static final String CONFIG_ROLE_FIELD_EXIST = "roleFieldExist";
    public static final String CONFIG_ROLE_FIELD = "roleField";
    public static final String CONFIG_CLAIMS = "claims";
    public static final String CONFIG_THREAD_POOL_SIZE = "threadPoolSize";

    // File specific configs.
    public static final String FILE_TYPE_CSV = ".csv";
    public static final char COMMA = ',';
    public static final char DOUBLE_QUOTE = '"';
    public static final char SEMI_COLON = ';';

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
        CLIENT_USER_STORE_CONFIGURATIONS_ERROR("BUP-60004",
                "Not configured to use primary user store or not provided secondary user store domain.",
                "Not configured to use primary user store or not provided secondary user store domain."),

        // Server error codes.
        SERVER_CONFIG_LOADING_ERROR("BUP-65001", "Error while loading Bulk user provision configs.",
                "Error while loading Bulk user provision configs : %s"),
        SERVER_INCOMPATIBLE_USER_STORE_MANAGER_ERROR("BUP-65002", "Incompatible user store manager.",
                "user store manager doesn't support unique Ids."),
        SERVER_UNEXPECTED_ERROR("BUP-65003", "An unexpected server error occurred.",
                "An unexpected server error occurred."),
        SERVER_CONFIG_READING_ERROR("BUP-65004", "An error occurred while reading configurations.",
                "An error occurred while reading configurations"),
        SERVER_TENANT_ERROR("BUP-65005", "An error occurred while fetching tenant.",
                "An error occurred while fetching tenant."),
        SERVER_PRIMARY_USER_STORE_FIND_ERROR("BUP-65006",
                "An error occurred while finding primary user-store.",
                "An error occurred while finding primary user-store."),
        SERVER_USER_STORE_FIND_ERROR("BUP-65007", "An error occurred while finding user-store.",
                "An error occurred while finding user-store."),
        SERVER_CSV_FILE_READ_ERROR("BUP-65008", "An error occurred while reading csv file.",
                "An error occurred while reading csv file"),
        SERVER_CSV_READER_CLOSE_ERROR("BUP-65009", "An error occurred while closing csv reader.",
                "An error occurred while closing csv reader"),
        SERVER_BUFFERED_READER_CLOSE_ERROR("BUP-65010", "An error occurred while closing buffered-reader.",
                "An error occurred while closing buffered reader."),
        SERVER_INPUT_STREAM_CLOSE_ERROR("BUP-65011", "An error occurred while closing input stream.",
                "An error occurred while closing input stream.");

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
