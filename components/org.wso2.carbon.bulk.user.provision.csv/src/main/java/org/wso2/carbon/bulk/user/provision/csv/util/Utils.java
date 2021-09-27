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

        Path path = Constants.BULK_USER_PROVISION_CONFIG_DIR_PATH;
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

            // Specific configs.
            propertiesMap.put(Constants.CONFIG_TENANT_DOMAIN, properties.getProperty(Constants.CONFIG_TENANT_DOMAIN));
            propertiesMap.put(Constants.CONFIG_USERNAME_FIELD, properties.getProperty(Constants.CONFIG_USERNAME_FIELD));
            propertiesMap.put(Constants.CONFIG_PASSWORD_FIELD, properties.getProperty(Constants.CONFIG_PASSWORD_FIELD));

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

    private static void sanitizeAndPopulateConfigs(Map<String, String> configs) {

        ConfigurationsDTO configurationsDTO = BulkUserProvisionDataHolder.getConfigs();

        boolean isEnabled = Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_IS_ENABLED)));
        configurationsDTO.setEnabled(isEnabled);

        boolean isPrimaryUserStore =
                Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_IS_PRIMARY_USER_STORE)));
        configurationsDTO.setPrimaryUserStore(isPrimaryUserStore);

        if (!isPrimaryUserStore && StringUtils.isBlank(configs.get(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN))) {
            // if primary user store is set false and secondary user store domain is not set primary user store as true.
            configurationsDTO.setPrimaryUserStore(true);
        }
        String secondaryUserStoreDomain = StringUtils.trim(configs.get(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN));
        configurationsDTO.setSecondaryUserStoreDomain(secondaryUserStoreDomain);

        if (StringUtils.isBlank(configs.get(Constants.CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN))) {
            configurationsDTO.setWaitingTimeForSecondaryUserStore(
                    Constants.DEFAULT_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN);
        } else {
            int timeWaitForSecondaryUserStore = Integer.parseInt(
                    StringUtils.trim(configs.get(Constants.CONFIG_WAITING_TIME_FOR_SECONDARY_USER_STORE_DOMAIN)));
            configurationsDTO.setNoOfRowsFetch(timeWaitForSecondaryUserStore);
        }

        if (StringUtils.isBlank(configs.get(Constants.CONFIG_TENANT_DOMAIN))) {
            configurationsDTO.setTenantDomain(Constants.DEFAULT_TENANT_DOMAIN);
        } else {
            String tenantDomain = StringUtils.trim(configs.get(Constants.CONFIG_TENANT_DOMAIN));
            configurationsDTO.setSecondaryUserStoreDomain(tenantDomain);
        }

        if (StringUtils.isBlank(configs.get(Constants.CONFIG_ROWS_TO_FETCH))) {
            configurationsDTO.setNoOfRowsFetch(Constants.DEFAULT_NO_OF_ROWS_FETCH);
        } else {
            int noOfRowsFetch = Integer.parseInt(StringUtils.trim(configs.get(Constants.CONFIG_ROWS_TO_FETCH)));
            configurationsDTO.setNoOfRowsFetch(noOfRowsFetch);
        }

        if (StringUtils.isBlank(configs.get(Constants.CONFIG_USERNAME_FIELD))) {
            configurationsDTO.setUsernameField(Constants.DEFAULT_USERNAME_FIELD);
        } else {
            String usernameField = StringUtils.trim(configs.get(Constants.CONFIG_USERNAME_FIELD));
            configurationsDTO.setUsernameField(usernameField);
        }

        if (StringUtils.isBlank(configs.get(Constants.CONFIG_PASSWORD_FIELD))) {
            configurationsDTO.setUsernameField(Constants.DEFAULT_PASSWORD_FIELD);
        } else {
            String passwordField = StringUtils.trim(configs.get(Constants.CONFIG_PASSWORD_FIELD));
            configurationsDTO.setPasswordField(passwordField);
        }
    }

    // Get tenantID from tenantDomain.
    public static int getTenantIdFromDomain(String tenantDomain) throws BulkUserProvisionServerException {

        try {
            return IdentityTenantUtil.getTenantId(tenantDomain);
        } catch (IdentityRuntimeException e) {
            log.error(String.format(
                    "%s Prerequisites were not satisfied. Error occurred while resolving tenant Id from tenant domain :%s",
                    Constants.BULK_USER_PROVISION_LOG_PREFIX, tenantDomain), e);
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
            throw handleServerException(Constants.ErrorMessage.SERVER_PRIMARY_USER_STORE_FIND_ERROR,
                    Constants.BULK_USER_PROVISION, e);
        }
        return userStoreManager;
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
