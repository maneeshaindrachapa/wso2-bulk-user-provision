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

package org.wso2.carbon.bulk.user.provision.csv.dto;

import java.util.Map;

/**
 * This class holds the Bulk User Provision feature configurations.
 */
public class ConfigurationsDTO {

    private boolean isEnabled;
    private boolean isPrimaryUserStore;
    private String secondaryUserStoreDomain;
    private int waitingTimeForSecondaryUserStore;
    private int noOfRowsFetch;
    private String tenantDomain;
    private boolean roleFieldExist;
    private String roleField;
    private String usernameField;
    private String passwordField;
    private Map<String, String> claims;
    private int threadPoolSize;

    public boolean isEnabled() {

        return isEnabled;
    }

    public void setEnabled(boolean enabled) {

        isEnabled = enabled;
    }

    public boolean isPrimaryUserStore() {

        return isPrimaryUserStore;
    }

    public void setPrimaryUserStore(boolean primaryUserStore) {

        isPrimaryUserStore = primaryUserStore;
    }

    public String getSecondaryUserStoreDomain() {

        return secondaryUserStoreDomain;
    }

    public void setSecondaryUserStoreDomain(String secondaryUserStoreDomain) {

        this.secondaryUserStoreDomain = secondaryUserStoreDomain;
    }

    public int getWaitingTimeForSecondaryUserStore() {

        return waitingTimeForSecondaryUserStore;
    }

    public void setWaitingTimeForSecondaryUserStore(int waitingTimeForSecondaryUserStore) {

        this.waitingTimeForSecondaryUserStore = waitingTimeForSecondaryUserStore;
    }

    public int getNoOfRowsFetch() {

        return noOfRowsFetch;
    }

    public void setNoOfRowsFetch(int noOfRowsFetch) {

        this.noOfRowsFetch = noOfRowsFetch;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public boolean isRoleFieldExist() {

        return roleFieldExist;
    }

    public void setRoleFieldExist(boolean roleFieldExist) {

        this.roleFieldExist = roleFieldExist;
    }

    public String getRoleField() {

        return roleField;
    }

    public void setRoleField(String roleField) {

        this.roleField = roleField;
    }

    public String getUsernameField() {

        return usernameField;
    }

    public void setUsernameField(String usernameField) {

        this.usernameField = usernameField;
    }

    public String getPasswordField() {

        return passwordField;
    }

    public void setPasswordField(String passwordField) {

        this.passwordField = passwordField;
    }

    public Map<String, String> getClaims() {

        return claims;
    }

    public void setClaims(Map<String, String> claims) {

        this.claims = claims;
    }

    public int getThreadPoolSize() {

        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {

        this.threadPoolSize = threadPoolSize;
    }
}
