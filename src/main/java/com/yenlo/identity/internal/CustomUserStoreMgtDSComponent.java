package com.yenlo.identity.internal;

import com.yenlo.identity.CustomJDBCUserStoreManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="com.yenlo.identity.component" immediate=true
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class CustomUserStoreMgtDSComponent {
    private static Log log = LogFactory.getLog(CustomUserStoreMgtDSComponent.class);
    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {

        CustomJDBCUserStoreManager customUserStoreManager = new CustomJDBCUserStoreManager();
        ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), customUserStoreManager, null);
        log.info("CustomUserStoreManager bundle activated successfully..");
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Custom User Store Manager is deactivated ");
        }
    }

    protected void setRealmService(RealmService rlmService) {
        CustomUserStoreMgtDSComponent.realmService  = rlmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        CustomUserStoreMgtDSComponent.realmService = null;
    }
    public static RealmService getRealmService() {
        return realmService;
    }
}