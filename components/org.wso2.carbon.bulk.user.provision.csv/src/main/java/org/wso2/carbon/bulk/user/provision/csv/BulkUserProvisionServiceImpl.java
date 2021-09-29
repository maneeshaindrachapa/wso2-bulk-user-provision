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

package org.wso2.carbon.bulk.user.provision.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bulk.user.provision.csv.constants.Constants;
import org.wso2.carbon.bulk.user.provision.csv.internal.BulkUserProvisionDataHolder;
import org.wso2.carbon.bulk.user.provision.csv.util.Utils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * BulkUserProvisionService implementation.
 */
public class BulkUserProvisionServiceImpl implements BulkUserProvisionService, Callable<Boolean> {

    private static final Log log = LogFactory.getLog(BulkUserProvisionServiceImpl.class);

    // Take all the csv files in the directory.
    private File[] files = null;
    private ArrayList<String[]> columnNames = new ArrayList<>();
    private Map<Integer, Map<String, Integer>> fileIndexToSpecialColumns = new LinkedHashMap<>();
    private LinkedHashMap<Integer, LinkedHashSet<String[]>> fileIndexToUserHashMap = new LinkedHashMap<>();
    private LinkedHashSet<String[]> userSet = null;

    private UniqueIDJDBCUserStoreManager uniqueIDJDBCUserStoreManager;

    public BulkUserProvisionServiceImpl() {

        super();
    }

    @Override
    public Boolean call() throws Exception {

        // Get csv Files.
        this.files = Utils.getCsvFiles();
        // Get user store manager.
        this.uniqueIDJDBCUserStoreManager = (UniqueIDJDBCUserStoreManager) Utils.getUserStoreManager();
        // Start time to provision bulk users.
        long startingTimeProvisioningBulkUsers = System.currentTimeMillis();

        if (this.uniqueIDJDBCUserStoreManager != null) {

            InputStream targetStream = null;
            BufferedReader bufferedReader = null;
            CSVReader csvReader = null;

            try {
                log.info(String.format("%s Starting reading from files and checking columns",
                        Constants.BULK_USER_PROVISION_LOG_PREFIX));

                // To store mandatory fields like username, password and role.
                Map<String, Integer> mandatoryFields = new HashMap<>();
                for (int file = 0; file < files.length; file++) {
                    log.info(
                            String.format("%s Started column check for the CSV file: %s, file order: %s",
                                    Constants.BULK_USER_PROVISION, files[file].getAbsolutePath(), file));
                    log.info(String.format("%s Started reading from file: %s, file order: %s",
                            Constants.BULK_USER_PROVISION, files[file].getAbsolutePath(), file));

                    targetStream = new FileInputStream(files[file]);
                    bufferedReader = new BufferedReader(new InputStreamReader(targetStream, StandardCharsets.UTF_8));
                    csvReader = new CSVReader(bufferedReader, Constants.COMMA, Constants.DOUBLE_QUOTE, 0);

                    String[] line = csvReader.readNext();
                    this.columnNames.add(line);

                    boolean usernameFieldFound = false;
                    boolean passwordFieldFound = false;
                    boolean roleFieldFound = false;

                    for (int j = 0; j < line.length; j++) {
                        if (StringUtils.equals(line[j], BulkUserProvisionDataHolder.getConfigs().getUsernameField())) {
                            if (usernameFieldFound) {
                                log.error(String.format("%s Field %s: %s duplicated in the CSV.Task Aborted",
                                        Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                        BulkUserProvisionDataHolder.getConfigs().getUsernameField(), line[j]));
                                return false;
                            }
                            log.info(String.format("%s Field %s: %s found in the CSV.",
                                    Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                    BulkUserProvisionDataHolder.getConfigs().getUsernameField(), line[j]));
                            mandatoryFields.put(BulkUserProvisionDataHolder.getConfigs().getUsernameField(), j);
                            usernameFieldFound = true;
                        } else if (StringUtils.equals(line[j],
                                BulkUserProvisionDataHolder.getConfigs().getPasswordField())) {
                            if (passwordFieldFound) {
                                log.error(String.format("%s Field %s: %s duplicated in the CSV.Task Aborted",
                                        Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                        BulkUserProvisionDataHolder.getConfigs().getPasswordField(), line[j]));
                                return false;
                            }
                            log.info(String.format("%s Field %s: %s found in the CSV.",
                                    Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                    BulkUserProvisionDataHolder.getConfigs().getUsernameField(), line[j]));
                            mandatoryFields.put(BulkUserProvisionDataHolder.getConfigs().getPasswordField(), j);
                            passwordFieldFound = true;
                        }
                        if (BulkUserProvisionDataHolder.getConfigs().isRoleFieldExist()) {
                            if (StringUtils.equals(line[j], BulkUserProvisionDataHolder.getConfigs().getRoleField())) {
                                log.info(String.format("%s Field %s: %s found in the CSV.",
                                        Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                        BulkUserProvisionDataHolder.getConfigs().getRoleField(), line[j]));
                                if (roleFieldFound) {
                                    log.error(String.format("%s Field %s: %s duplicated in the CSV.Task Aborted",
                                            Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                            BulkUserProvisionDataHolder.getConfigs().getRoleField(), line[j]));
                                    return false;
                                }
                                mandatoryFields.put(BulkUserProvisionDataHolder.getConfigs().getRoleField(), j);
                                roleFieldFound = true;
                            }
                        }
                    }
                    // Add file-index with the mandatory fields.
                    this.fileIndexToSpecialColumns.put(file, mandatoryFields);
                    this.userSet = new LinkedHashSet<>();
                    while (line != null && line.length > 0) {
                        line = csvReader.readNext();
                        userSet.add(line);
                    }
                    this.fileIndexToUserHashMap.put(file, userSet);
                }
            } catch (IOException e) {
                log.error(String.format("%s Error occurred while reading from CSV files:%s:%s",
                        Constants.BULK_USER_PROVISION_LOG_PREFIX,
                        Constants.ErrorMessage.SERVER_CSV_FILE_READ_ERROR.getCode(),
                        Constants.ErrorMessage.SERVER_CSV_FILE_READ_ERROR.getMessage()), e);
                return false;
            } finally {
                if (csvReader != null) {
                    try {
                        csvReader.close();
                    } catch (IOException e) {
                        log.error(String.format("%s Error occurred while closing csv-reader:%s:%s",
                                Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                Constants.ErrorMessage.SERVER_CSV_READER_CLOSE_ERROR.getCode(),
                                Constants.ErrorMessage.SERVER_CSV_READER_CLOSE_ERROR.getMessage()), e);
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        log.error(String.format("%s Error occurred while closing buffered-reader:%s:%s",
                                Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                Constants.ErrorMessage.SERVER_BUFFERED_READER_CLOSE_ERROR.getCode(),
                                Constants.ErrorMessage.SERVER_BUFFERED_READER_CLOSE_ERROR.getMessage()), e);
                    }
                }
                if (targetStream != null) {
                    try {
                        targetStream.close();
                    } catch (IOException e) {
                        log.error(String.format("%s Error occurred while closing input-stream:%s:%s",
                                Constants.BULK_USER_PROVISION_LOG_PREFIX,
                                Constants.ErrorMessage.SERVER_INPUT_STREAM_CLOSE_ERROR.getCode(),
                                Constants.ErrorMessage.SERVER_INPUT_STREAM_CLOSE_ERROR.getMessage()), e);
                    }
                }
            }

            long readingCsvFiles = System.currentTimeMillis();
            log.info(String.format("%s [TIME INDICATOR] Total time taken to read from CSV files and check columns" +
                            "(in milliseconds) :%s ", Constants.BULK_USER_PROVISION_LOG_PREFIX,
                    (readingCsvFiles - startingTimeProvisioningBulkUsers)));
            log.info(String.format("%s Starting user provisioning to the given user store.",
                    Constants.BULK_USER_PROVISION_LOG_PREFIX));

            // Start Privileged Carbon Context.
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(BulkUserProvisionDataHolder.getConfigs().getTenantDomain());
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantId(
                            Utils.getTenantIdFromDomain(BulkUserProvisionDataHolder.getConfigs().getTenantDomain()));

            LinkedHashSet<String[]> userSet;
            int fileIndex;
            int usernameColumnIndex;
            int passwordColumnIndex;
            int roleColumnIndex;
            String[] columnNames = null;
            for (Map.Entry<Integer, LinkedHashSet<String[]>> entry : this.fileIndexToUserHashMap.entrySet()) {
                roleColumnIndex = -1;
                fileIndex = entry.getKey();
                userSet = entry.getValue();

                usernameColumnIndex = this.fileIndexToSpecialColumns.get(fileIndex)
                        .get(BulkUserProvisionDataHolder.getConfigs().getUsernameField());
                passwordColumnIndex = this.fileIndexToSpecialColumns.get(fileIndex)
                        .get(BulkUserProvisionDataHolder.getConfigs().getPasswordField());
                if (BulkUserProvisionDataHolder.getConfigs().isRoleFieldExist()) {
                    if (this.fileIndexToSpecialColumns.get(fileIndex)
                            .get(BulkUserProvisionDataHolder.getConfigs().getRoleField()) != null) {
                        roleColumnIndex = this.fileIndexToSpecialColumns.get(fileIndex)
                                .get(BulkUserProvisionDataHolder.getConfigs().getRoleField());
                    }
                }

                columnNames = this.columnNames.get(fileIndex);
                for (String[] user : userSet) {
                    if (user != null && user[usernameColumnIndex] != null && !user[usernameColumnIndex].isEmpty()) {
                        // Add Claims.
                        Map<String, String> claims = new HashMap<>();
                        for (int i = 0; i < columnNames.length; i++) {
                            if (i != usernameColumnIndex && i != passwordColumnIndex && i != roleColumnIndex) {
                                // Check if the claims mappings are provided,if provided add it to claims map.
                                if (BulkUserProvisionDataHolder.getConfigs().getClaims().get(columnNames[i]) != null) {
                                    claims.put(BulkUserProvisionDataHolder.getConfigs().getClaims().get(columnNames[i]),
                                            user[i]);
                                } else {
                                    log.info(String.format(
                                            "%s Provided CSV column doesn't map with the claims provided. claim col:%s"
                                                    + ",claim value:%s",
                                            Constants.BULK_USER_PROVISION_LOG_PREFIX, columnNames[i], user[i]));
                                }
                            }
                        }
                        try {
                            if (roleColumnIndex != -1) {
                                String[] roles = {user[roleColumnIndex]};
                                uniqueIDJDBCUserStoreManager.doAddUserWithID(user[usernameColumnIndex],
                                        user[passwordColumnIndex], roles,
                                        claims, null, false);
                            } else {
                                uniqueIDJDBCUserStoreManager.doAddUserWithID(user[usernameColumnIndex],
                                        user[passwordColumnIndex], null,
                                        claims, null, false);
                            }
                        } catch (UserStoreException e) {
                            log.error(String.format("%s Error occurred while adding user with the username : %s",
                                    Constants.BULK_USER_PROVISION_LOG_PREFIX, user[0]), e);
                        }
                    }
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
            long bulkUserProvisionEnd = System.currentTimeMillis();
            log.info(String.format("%s [TIME INDICATOR] Total time taken to add users to the user store " +
                            "(in milliseconds) : %s", Constants.BULK_USER_PROVISION_LOG_PREFIX,
                    (bulkUserProvisionEnd - readingCsvFiles)));
        }
        return true;
    }
}
