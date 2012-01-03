package org.springframework.roo.addon.cloud.foundry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.preferences.PreferencesService;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.uaa.client.TransmissionAwareUaaService;
import org.springframework.uaa.client.TransmissionEventListener;
import org.springframework.uaa.client.UaaService;
import org.springframework.util.Assert;

import com.vmware.appcloud.client.AppCloudClient;
import com.vmware.appcloud.client.CloudApplication;
import com.vmware.appcloud.client.CloudService;
import com.vmware.appcloud.client.ServiceConfiguration;

@Component
@Service
public class CloudFoundrySessionImpl implements CloudFoundrySession,
        TransmissionEventListener {

    // Constants
    private static final Logger LOGGER = HandlerUtils
            .getLogger(CloudFoundryOperationsImpl.class);

    // Fields
    @Reference AppCloudClientFactory appCloudClientFactory;
    @Reference private PreferencesService preferencesService;
    @Reference private UaaService uaaService;

    private final List<Integer> memoryOptions = new ArrayList<Integer>();
    private final List<String> appNames = new ArrayList<String>();
    private final List<String> provisionedServices = new ArrayList<String>();
    private final List<String> serviceTypes = new ArrayList<String>();
    private final Map<String, List<String>> boundUrlMap = new HashMap<String, List<String>>();

    CloudPreferences preferences;
    private UaaAwareAppCloudClient client;

    protected void activate(final ComponentContext context) {
        preferences = new CloudPreferences(preferencesService);
        if (uaaService instanceof TransmissionAwareUaaService) {
            ((TransmissionAwareUaaService) uaaService)
                    .addTransmissionEventListener(this);
        }
    }

    protected void deactivate(final ComponentContext cc) {
        if (uaaService instanceof TransmissionAwareUaaService) {
            ((TransmissionAwareUaaService) uaaService)
                    .removeTransmissionEventListener(this);
        }
        if (client != null) {
            client.deactivate();
        }
        preferences.flush();
    }

    public void beforeTransmission(final TransmissionType type) {
        if (client != null) {
            client.beforeTransmission(type);
        }
    }

    public void afterTransmission(final TransmissionType type,
            final boolean successful) {
        if (client != null) {
            client.afterTransmission(type, successful);
        }
    }

    public void login(final String email, final String password,
            final String cloudControllerUrl) {
        final CloudCredentials credentials = getLoginCredentials(
                cloudControllerUrl, email, password);
        if (credentials == null) {
            LOGGER.info("Login failed");
            return;
        }

        login(credentials);

        if (StringUtils.hasText(email) && StringUtils.hasText(password)) {
            // The user provided fresh credentials
            preferences.storeCredentials(credentials);
            LOGGER.info("Credentials saved.");
        }

        LOGGER.info("Logged in successfully with email address '"
                + credentials.getEmail() + "'");
    }

    /**
     * Returns the credentials for logging into the cloud.
     * 
     * @param cloudControllerUrl the URL provided by the user (can be blank)
     * @param email the email address provided by the user (can be blank)
     * @param password the password provided by the user (can be blank)
     * @return <code>null</code> if a complete set of credentials could not be
     *         obtained from either the provided or stored values
     */
    private CloudCredentials getLoginCredentials(String cloudControllerUrl,
            String email, String password) {
        cloudControllerUrl = StringUtils.defaultIfEmpty(cloudControllerUrl,
                UaaAwareAppCloudClient.CLOUD_FOUNDRY_URL);

        if (StringUtils.isBlank(email)) {
            email = getStoredEmailAddress(cloudControllerUrl);
            if (StringUtils.isBlank(email)) {
                return null;
            }
        }

        if (StringUtils.isBlank(password)) {
            password = preferences.getStoredPassword(cloudControllerUrl, email);
        }

        final CloudCredentials loginCredentials = new CloudCredentials(email,
                password, cloudControllerUrl);
        if (loginCredentials.isValid()) {
            return loginCredentials;
        }
        return null;
    }

    /**
     * Returns the email address for the stored credentials with the given URL
     * 
     * @param cloudControllerUrl
     * @return <code>null</code> if there are not exactly one such set of
     *         credentials
     */
    private String getStoredEmailAddress(final String cloudControllerUrl) {
        final Collection<String> storedEmails = preferences
                .getStoredEmails(cloudControllerUrl);
        switch (storedEmails.size()) {
        case 1:
            return storedEmails.iterator().next();
        case 0:
            LOGGER.warning("An email address is required.");
            return null;
        default:
            LOGGER.warning("Multiple email addresses are stored for the cloud controller URL '"
                    + cloudControllerUrl
                    + "'. Please specify an email address.");
            return null;
        }
    }

    /**
     * Logs the user in with the given credentials
     * 
     * @param credentials the credentials to use (must be valid)
     */
    private void login(final CloudCredentials credentials) {
        Assert.isTrue(credentials.isValid(), "Invalid credentials "
                + credentials);
        if (client != null) {
            client.deactivate();
        }
        client = appCloudClientFactory.getUaaAwareInstance(credentials);
        client.loginIfNeeded();
    }

    public AppCloudClient getClient() {
        return client;
    }

    public List<String> getApplicationNames() {
        updateApplicationNames();
        return appNames;
    }

    public List<String> getProvisionedServices() {
        updateProvisionedServices();
        return provisionedServices;
    }

    public Map<String, List<String>> getBoundUrlMap() {
        updateUrlMap();
        return boundUrlMap;
    }

    public List<Integer> getApplicationMemoryOptions() {
        updateMemoryOptions();
        return memoryOptions;
    }

    public CloudService getProvisionedService(
            final String provisionedServiceName) {
        return client.getService(provisionedServiceName);
    }

    public ServiceConfiguration getService(final String serviceVendor) {
        for (final ServiceConfiguration serviceConfiguration : client
                .getServiceConfigurations()) {
            if (serviceConfiguration.getVendor().equals(serviceVendor)) {
                return serviceConfiguration;
            }
        }
        return null;
    }

    public List<String> getServiceTypes() {
        updateServiceTypes();
        return serviceTypes;
    }

    public List<String> getStoredEmails() {
        return preferences.getStoredEmails();
    }

    public List<String> getStoredUrls() {
        return preferences.getStoredUrls();
    }

    public void clearStoredLoginDetails() {
        preferences.clearStoredLoginDetails();
    }

    private void updateApplicationNames() {
        appNames.clear();
        for (final CloudApplication app : client.getApplications()) {
            appNames.add(app.getName());
        }
    }

    private void updateProvisionedServices() {
        provisionedServices.clear();
        for (final CloudService provisionedService : client.getServices()) {
            provisionedServices.add(provisionedService.getName());
        }
    }

    private void updateServiceTypes() {
        serviceTypes.clear();
        for (final ServiceConfiguration serviceType : client
                .getServiceConfigurations()) {
            serviceTypes.add(serviceType.getVendor());
        }
    }

    private void updateUrlMap() {
        this.boundUrlMap.clear();
        for (final CloudApplication app : client.getApplications()) {
            this.boundUrlMap.put(app.getName(), app.getUris());
        }
    }

    private void updateMemoryOptions() {
        memoryOptions.clear();
        for (final int memoryOption : client.getApplicationMemoryChoices()) {
            memoryOptions.add(memoryOption);
        }
        Collections.sort(memoryOptions);
    }
}
