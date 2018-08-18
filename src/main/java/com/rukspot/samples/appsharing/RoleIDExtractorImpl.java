/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.rukspot.samples.appsharing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.NewPostLoginExecutor;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class RoleIDExtractorImpl implements NewPostLoginExecutor {

    private static final Log log = LogFactory.getLog(RoleIDExtractorImpl.class);

    public String getGroupingIdentifiers(String loginResponse) {
        return "";
    }

    @Override
    public String[] getGroupingIdentifierList(String loginResponse) {
        JSONObject obj;
        String username = null;
        Boolean isSuperTenant;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain;
        String[] groupIdArray = null;
        try {
            obj = new JSONObject(loginResponse);
            username = (String) obj.get("user");
            isSuperTenant = (Boolean) obj.get("isSuperTenant");

            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

            //if the user is not in the super tenant domain then find the domain name and tenant id.
            if (!isSuperTenant) {
                tenantDomain = MultitenantUtils.getTenantDomain(username);
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            }

            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();

            /**
             * retrieve role list and return
             */
            String[] roles = manager.getRoleListOfUser(username);
            if (roles != null) {
                return roles;
            } else {
                // If claim is null then returning a empty string
                groupIdArray = new String[] {};
            }
        } catch (JSONException e) {
            log.error("Exception occurred while trying to get roles from login response", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while checking user existence for " + username, e);
        }

        return groupIdArray;
    }

}