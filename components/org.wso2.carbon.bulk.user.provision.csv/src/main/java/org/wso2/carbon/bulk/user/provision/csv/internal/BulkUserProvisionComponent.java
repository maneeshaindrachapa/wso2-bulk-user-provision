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

package org.wso2.carbon.bulk.user.provision.csv.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.bulk.user.provision.csv.constants.Constants;
import org.wso2.carbon.bulk.user.provision.csv.util.Utils;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi service component for BulkUserProvision.
 */
@Component(
        name = "org.wso2.carbon.bulk.user.provision.csv.component",
        immediate = true
)
public class BulkUserProvisionComponent {

    private static Log log = LogFactory.getLog(BulkUserProvisionComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        log.debug("Activating BulkUserProvisionComponent");
        try {
            Utils.readConfigurations();
            if (BulkUserProvisionDataHolder.getInstance().getConfigs().isEnabled()) {
                log.info(Constants.BULK_USER_PROVISION_LOG_PREFIX);
            }
        } catch (Throwable e) {
            log.error("Error while activating BulkUserProvision bundle ", e);
        }
    }

    @Reference(name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service for org.wso2.carbon.bulk.user.provision.csv.component");
        BulkUserProvisionDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("Unset the Realm Service for org.wso2.carbon.bulk.user.provision.csv.component");
        BulkUserProvisionDataHolder.getInstance().setRealmService(null);
    }
}
