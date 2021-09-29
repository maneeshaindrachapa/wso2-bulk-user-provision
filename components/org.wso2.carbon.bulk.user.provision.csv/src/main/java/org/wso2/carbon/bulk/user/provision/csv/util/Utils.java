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

package org.wso2.carbon.bulk.user.provision.csv.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bulk.user.provision.csv.constants.Constants;
import org.wso2.carbon.bulk.user.provision.csv.dto.ConfigurationsDTO;
import org.wso2.carbon.bulk.user.provision.csv.exception.BulkUserProvisionClientException;
import org.wso2.carbon.bulk.user.provision.csv.exception.BulkUserProvisionException;
import org.wso2.carbon.bulk.user.provision.csv.exception.BulkUserProvisionServerException;
import org.wso2.carbon.bulk.user.provision.csv.internal.BulkUserProvisionDataHolder;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This contains the utility functions need for Bulk user provision service.
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static void readConfigurations() throws BulkUserProvisionException {

        Path path = Constants.BULK_USER_PROVISION_CONFIG_DIR_PATH_FILE;
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw handleClientException(Constants.ErrorMessage.CLIENT_CONFIG_FILE_NOT_FOUND,
                    Constants.BULK_USER_PROVISION);
        }
        Properties properties = new Properties();
        Map<String, String> propertiesMap = new HashMap<>();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path.toString());
            properties.load(inputStream);

            // Service configs.
            propertiesMap.put(Constants.CONFIG_IS_ENABLED, properties.getProperty(Constants.CONFIG_IS_ENABLED));
            propertiesMap.put(Constants.CONFIG_IS_PRIMARY_USER_STORE,
                    properties.getProperty(Constants.CONFIG_IS_PRIMARY_USER_STORE));
            propertiesMap.put(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN,
                    properties.getProperty(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN));
            propertiesMap.put(Constants.CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN,
                    properties.getProperty(Constants.CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN));
            propertiesMap.put(Constants.CONFIG_ROWS_TO_FETCH, properties.getProperty(Constants.CONFIG_ROWS_TO_FETCH));
            propertiesMap.put(Constants.CONFIG_THREAD_POOL_SIZE,
                    properties.getProperty(Constants.CONFIG_THREAD_POOL_SIZE));

            // Specific configs.
            propertiesMap.put(Constants.CONFIG_TENANT_DOMAIN, properties.getProperty(Constants.CONFIG_TENANT_DOMAIN));
            propertiesMap.put(Constants.CONFIG_USERNAME_FIELD, properties.getProperty(Constants.CONFIG_USERNAME_FIELD));
            propertiesMap.put(Constants.CONFIG_PASSWORD_FIELD, properties.getProperty(Constants.CONFIG_PASSWORD_FIELD));
            propertiesMap.put(Constants.CONFIG_ROLE_FIELD_EXIST,
                    properties.getProperty(Constants.CONFIG_ROLE_FIELD_EXIST));
            propertiesMap.put(Constants.CONFIG_ROLE_FIELD, properties.getProperty(Constants.CONFIG_ROLE_FIELD));
            propertiesMap.put(Constants.CONFIG_CLAIMS, properties.getProperty(Constants.CONFIG_CLAIMS));

            sanitizeAndPopulateConfigs(propertiesMap);
        } catch (IOException e) {
            throw handleServerException(Constants.ErrorMessage.SERVER_CONFIG_READING_ERROR,
                    Constants.BULK_USER_PROVISION, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw handleServerException(Constants.ErrorMessage.SERVER_CONFIG_READING_ERROR,
                            Constants.BULK_USER_PROVISION, e);
                }
            }
        }
    }

    private static void sanitizeAndPopulateConfigs(Map<String, String> configs)
            throws BulkUserProvisionClientException {

        ConfigurationsDTO configurationsDTO = BulkUserProvisionDataHolder.getConfigs();

        // Check Bulk user provision is enabled.
        boolean isEnabled = Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_IS_ENABLED)));
        configurationsDTO.setEnabled(isEnabled);

        // Check using primary user store.
        boolean isPrimaryUserStore =
                Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_IS_PRIMARY_USER_STORE)));
        configurationsDTO.setPrimaryUserStore(isPrimaryUserStore);

        // Check if role field exists.
        boolean isRoleFieldExists =
                Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_ROLE_FIELD_EXIST)));
        configurationsDTO.setRoleFieldExist(isRoleFieldExists);

        // If role field exists is True, take the roleField from configs.
        if (isRoleFieldExists) {
            if (StringUtils.isBlank(configs.get(Constants.CONFIG_ROLE_FIELD))) {
                configurationsDTO.setRoleField(Constants.DEFAULT_ROLE_FIELD);
            } else {
                String roleField = StringUtils.trim(configs.get(Constants.CONFIG_ROLE_FIELD));
                configurationsDTO.setRoleField(roleField);
            }
        }

        // If not using primary user store and secondary user store is not configured throw an error.
        if (!isPrimaryUserStore && StringUtils.isBlank(configs.get(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN))) {
            throw handleClientException(Constants.ErrorMessage.CLIENT_USER_STORE_CONFIGURATIONS_ERROR,
                    Constants.BULK_USER_PROVISION);
        }
        String secondaryUserStoreDomain = StringUtils.trim(configs.get(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN));
        configurationsDTO.setSecondaryUserStoreDomain(secondaryUserStoreDomain);

        // Check waiting time for secondary user store.
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN))) {
            configurationsDTO.setWaitingTimeForSecondaryUserStore(
                    Constants.DEFAULT_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN);
        } else {
            int timeWaitForSecondaryUserStore = Integer.parseInt(
                    StringUtils.trim(configs.get(Constants.CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN)));
            configurationsDTO.setNoOfRowsFetch(timeWaitForSecondaryUserStore);
        }

        // Check tenant domain is set if not use default carbon.super tenant domain
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_TENANT_DOMAIN))) {
            configurationsDTO.setTenantDomain(Constants.DEFAULT_TENANT_DOMAIN);
        } else {
            String tenantDomain = StringUtils.trim(configs.get(Constants.CONFIG_TENANT_DOMAIN));
            configurationsDTO.setSecondaryUserStoreDomain(tenantDomain);
        }

        // Check rows fetch is configured if not use default row values to fetch.
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_ROWS_TO_FETCH))) {
            configurationsDTO.setNoOfRowsFetch(Constants.DEFAULT_NO_OF_ROWS_FETCH);
        } else {
            int noOfRowsFetch = Integer.parseInt(StringUtils.trim(configs.get(Constants.CONFIG_ROWS_TO_FETCH)));
            configurationsDTO.setNoOfRowsFetch(noOfRowsFetch);
        }

        // Check the size of thread pool is configured if not use default.
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_THREAD_POOL_SIZE))) {
            configurationsDTO.setThreadPoolSize(Constants.DEFAULT_BULK_USER_PROVISION_POOL_SIZE);
        } else {
            int threadPoolSize = Integer.parseInt(StringUtils.trim(configs.get(Constants.CONFIG_THREAD_POOL_SIZE)));
            configurationsDTO.setThreadPoolSize(threadPoolSize);
        }

        // Check username field is configured if not use default username field.
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_USERNAME_FIELD))) {
            configurationsDTO.setUsernameField(Constants.DEFAULT_USERNAME_FIELD);
        } else {
            String usernameField = StringUtils.trim(configs.get(Constants.CONFIG_USERNAME_FIELD));
            configurationsDTO.setUsernameField(usernameField);
        }

        // Check password field is configured if not use default password field.
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_PASSWORD_FIELD))) {
            configurationsDTO.setUsernameField(Constants.DEFAULT_PASSWORD_FIELD);
        } else {
            String passwordField = StringUtils.trim(configs.get(Constants.CONFIG_PASSWORD_FIELD));
            configurationsDTO.setPasswordField(passwordField);
        }

        // Get claims.
        if (StringUtils.isBlank(configs.get(Constants.CONFIG_CLAIMS))) {
            configurationsDTO.setClaims(null);
        } else {
            String[] claims = StringUtils.trim(configs.get(Constants.CONFIG_CLAIMS)).split(
                    String.valueOf(Constants.SEMI_COLON));
            Map<String, String> claimsMap = new HashMap<>();
            for (String claim : claims) {
                String[] claimSplit = claim.split(String.valueOf(Constants.COMMA));
                // 0 - claim mapping column ,1 - claimURI.
                claimsMap.put(claimSplit[0], claimSplit[1]);
            }
            BulkUserProvisionDataHolder.getConfigs().setClaims(claimsMap);
        }
    }

    // Get tenantID from tenantDomain.
    public static int getTenantIdFromDomain(String tenantDomain) throws BulkUserProvisionServerException {

        try {
            return IdentityTenantUtil.getTenantId(tenantDomain);
        } catch (IdentityRuntimeException e) {
            log.error(String.format(
                    "%s Prerequisites were not satisfied. Error occurred while resolving tenant Id from tenant domain" +
                            ":%s", Constants.BULK_USER_PROVISION_LOG_PREFIX, tenantDomain), e);
            throw handleServerException(Constants.ErrorMessage.SERVER_TENANT_ERROR, Constants.BULK_USER_PROVISION, e);
        }
    }

    // Get user-store manager.
    public static UserStoreManager getUserStoreManager() throws BulkUserProvisionServerException {

        long time = System.currentTimeMillis();
        UserStoreManager userStoreManager = null;
        boolean timeOut = false; // This is to check time limit reached while checking secondary user store.
        try {
            if (BulkUserProvisionDataHolder.getConfigs().isPrimaryUserStore()) {
                log.info(String.format("%s Trying to find primary user store.", Constants.BULK_USER_PROVISION));
                userStoreManager = BulkUserProvisionDataHolder.getInstance().getRealmService().getBootstrapRealm()
                        .getUserStoreManager();
                if (userStoreManager != null) {
                    return userStoreManager;
                }
                log.error(String.format(
                        "%s Prerequisites were not satisfied. Primary user store was not found. Task aborted.",
                        Constants.BULK_USER_PROVISION_LOG_PREFIX));
                throw handleServerException(Constants.ErrorMessage.SERVER_PRIMARY_USER_STORE_FIND_ERROR,
                        Constants.BULK_USER_PROVISION);
            } else {
                log.info(String.format("%s Waiting until secondary user store is found.",
                        Constants.BULK_USER_PROVISION));
                // Try to fetch secondary user store manager.
                while (userStoreManager == null) {
                    userStoreManager = BulkUserProvisionDataHolder.getInstance().getRealmService().getBootstrapRealm()
                            .getUserStoreManager().getSecondaryUserStoreManager(
                                    BulkUserProvisionDataHolder.getConfigs()
                                            .getSecondaryUserStoreDomain());

                    if (System.currentTimeMillis() > time + BulkUserProvisionDataHolder.getConfigs()
                            .getWaitingTimeForSecondaryUserStore()) {
                        log.error(String.format(
                                "%s Prerequisites were not satisfied.Secondary user store was not found." +
                                        "[Reason could be that the given user store domain is wrong." +
                                        "Check whether it is matching]. Allocated time exceeded. Task aborted.",
                                Constants.BULK_USER_PROVISION_LOG_PREFIX));
                        timeOut = true;
                        break;
                    }
                }
            }
            if (!timeOut) {
                log.info(String.format("%s Prerequisites were satisfied.user store found.",
                        Constants.BULK_USER_PROVISION_LOG_PREFIX));
            }
        } catch (UserStoreException e) {
            log.error(String.format("%s Error while obtaining user store manager",
                    Constants.BULK_USER_PROVISION_LOG_PREFIX), e);
            throw handleServerException(Constants.ErrorMessage.SERVER_USER_STORE_FIND_ERROR,
                    Constants.BULK_USER_PROVISION, e);
        }
        log.info(String.format("%s Time taken to fetch user store:%s", Constants.BULK_USER_PROVISION_LOG_PREFIX,
                (System.currentTimeMillis() - time)));
        return userStoreManager;
    }

    // Get CSV files in directory
    public static File[] getCsvFiles() throws BulkUserProvisionClientException {

        long time = System.currentTimeMillis();
        File fileDir = new File(String.valueOf(Constants.BULK_USER_PROVISION_CSV_DIR_PATH));
        File[] files = fileDir.listFiles((fileTemp, name) -> name.toLowerCase().endsWith(Constants.FILE_TYPE_CSV));
        if (files == null) {
            log.error(String.format("%s Invalid folder path %s",
                    Constants.BULK_USER_PROVISION_LOG_PREFIX, fileDir.getAbsolutePath()));
            throw handleClientException(Constants.ErrorMessage.CLIENT_CSV_FILE_FOLDER_NOT_FOUND,
                    Constants.BULK_USER_PROVISION);
        } else if (files.length == 0) {
            log.error(String.format("%s No CSV file is found at %s",
                    Constants.BULK_USER_PROVISION_LOG_PREFIX, fileDir.getAbsolutePath()));
            throw handleClientException(Constants.ErrorMessage.CLIENT_CSV_FILES_NOT_FOUND,
                    Constants.BULK_USER_PROVISION);
        }
        log.info(String.format("%s At least one CSV file is found in %s", Constants.BULK_USER_PROVISION_LOG_PREFIX,
                fileDir.getAbsolutePath()));
        log.info(String.format("%s Time taken to fetch files from directory:%s",
                Constants.BULK_USER_PROVISION_LOG_PREFIX, (System.currentTimeMillis() - time)));
        return files;
    }

    public static BulkUserProvisionClientException handleClientException(Constants.ErrorMessage error, String data) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(error.getDescription(), data);
        } else {
            description = error.getDescription();
        }
        return new BulkUserProvisionClientException(error.getMessage(), description, error.getCode());
    }

    public static BulkUserProvisionClientException handleClientException(Constants.ErrorMessage error, String data,
                                                                         Throwable e) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(error.getDescription(), data);
        } else {
            description = error.getDescription();
        }
        return new BulkUserProvisionClientException(error.getMessage(), description, error.getCode(), e);
    }

    public static BulkUserProvisionServerException handleServerException(Constants.ErrorMessage error, String data,
                                                                         Throwable e) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(error.getDescription(), data);
        } else {
            description = error.getDescription();
        }
        return new BulkUserProvisionServerException(error.getMessage(), description, error.getCode(), e);
    }

    public static BulkUserProvisionServerException handleServerException(Constants.ErrorMessage error, String data) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(error.getDescription(), data);
        } else {
            description = error.getDescription();
        }
        return new BulkUserProvisionServerException(error.getMessage(), description, error.getCode());
    }
}
