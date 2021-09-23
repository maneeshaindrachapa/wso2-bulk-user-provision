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
        Map<String, String> propertiesMap = new HashMap<String, String>();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path.toString());
            properties.load(inputStream);

            propertiesMap.put(Constants.CONFIG_IS_ENABLED, properties.getProperty(Constants.CONFIG_IS_ENABLED));
            propertiesMap.put(Constants.CONFIG_IS_PRIMARY_USER_STORE,
                    properties.getProperty(Constants.CONFIG_IS_PRIMARY_USER_STORE));
            propertiesMap.put(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN,
                    properties.getProperty(Constants.CONFIG_SECONDARY_USER_STORE_DOMAIN));
            propertiesMap.put(Constants.CONFIG_ROWS_TO_FETCH, properties.getProperty(Constants.CONFIG_ROWS_TO_FETCH));
            propertiesMap.put(Constants.CONFIG_TENANT_DOMAIN, properties.getProperty(Constants.CONFIG_TENANT_DOMAIN));
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

        ConfigurationsDTO configurationsDTO = BulkUserProvisionDataHolder.getInstance().getConfigs();

        boolean isEnabled = Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_IS_ENABLED)));
        configurationsDTO.setEnabled(isEnabled);

        boolean isPrimaryUserStore =
                Boolean.parseBoolean(StringUtils.trim(configs.get(Constants.CONFIG_IS_PRIMARY_USER_STORE)));
        configurationsDTO.setPrimaryUserStore(isPrimaryUserStore);

        if (!isPrimaryUserStore && StringUtils.isBlank(configs.get(Constants.CONFIG_IS_PRIMARY_USER_STORE))) {
            // if primary user store is set false and secondary user store domain is not set primary user store as true.
            configurationsDTO.setPrimaryUserStore(true);
        }
        String secondaryUserStoreDomain = StringUtils.trim(configs.get(Constants.CONFIG_IS_PRIMARY_USER_STORE));
        configurationsDTO.setSecondaryUserStoreDomain(secondaryUserStoreDomain);

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
