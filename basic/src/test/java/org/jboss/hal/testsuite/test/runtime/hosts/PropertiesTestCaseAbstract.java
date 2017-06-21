package org.jboss.hal.testsuite.test.runtime.hosts;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.runtime.HostPropertiesWizard;
import org.jboss.hal.testsuite.page.runtime.HostsPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class PropertiesTestCaseAbstract {

    protected static final String
            SERVER_CONFIG = "server-config",
            SYSTEM_PROPERTY = "system-property",
            VALUE = "value";

    @Drone
    protected WebDriver browser;

    @Page
    protected HostsPage page;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);

    protected static Address serverAddress;

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void createServerProperty() throws Exception {
        final String propertyName = RandomStringUtils.randomAlphabetic(7);
        try {
            navigate();
            HostPropertiesWizard wizard = page.addProperty();
            wizard.name(propertyName)
                    .value("example")
                    .bootTime(true)
                    .finishWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Property should be present in table", page.isRowPresent(propertyName));
            verifyOnServer(propertyName, true);
        } finally {
            operations.removeIfExists(serverAddress.and(SYSTEM_PROPERTY, propertyName));
        }
    }

    @Test
    public void removeServerProperty() throws Exception {
        final String propertyName = RandomStringUtils.randomAlphabetic(7),
                propertyValue = RandomStringUtils.randomAlphanumeric(7);
        try {
            addPropertyToServer(propertyName, propertyValue);
            navigate();
            page.getResourceManager()
                    .removeResourceAndConfirm(propertyName);
            Assert.assertFalse("Property should not be present in table", page.isRowPresent(propertyName));
            verifyOnServer(propertyName, false);
        } finally {
            operations.removeIfExists(serverAddress.and(SYSTEM_PROPERTY, propertyName));
        }
    }

    @Test
    public void invalidPropertyName() throws IOException, OperationException {
        final String propertyName = RandomStringUtils.randomAlphabetic(7) + " *+<";
        try {
            navigate();
            HostPropertiesWizard wizard = page.addProperty();
            wizard.name(propertyName)
                    .value(RandomStringUtils.randomAlphabetic(7))
                    .bootTime(true)
                    .finishWithState()
                    .assertWindowOpen();
            Assert.assertTrue("Error should be shown", page.isErrorShown());
        } finally {
            operations.removeIfExists(serverAddress.and(SYSTEM_PROPERTY, propertyName));
        }

    }

    @Test
    public void invalidPropertyValue() throws IOException, OperationException {
        final String propertyName = RandomStringUtils.randomAlphabetic(7);
        try {
            navigate();
            HostPropertiesWizard wizard = page.addProperty();
            wizard.name(propertyName)
                    .value("examplečřž")
                    .bootTime(true)
                    .finishWithState()
                    .assertWindowOpen();
            Assert.assertTrue("Error should be shown", page.isErrorShown());
        } finally {
            operations.removeIfExists(serverAddress.and(SYSTEM_PROPERTY, propertyName));
        }
    }

    @Test
    public void createServerPropertyWithFalseBootTime() throws Exception {
        final String propertyName = RandomStringUtils.randomAlphabetic(7);
        try {
            navigate();
            HostPropertiesWizard wizard = page.addProperty();
            wizard.name(propertyName)
                    .value(RandomStringUtils.randomAlphabetic(7))
                    .bootTime(false)
                    .finishWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Property should be present in table", page.isRowPresent(propertyName));
            verifyOnServer(propertyName, true);
        } finally {
            operations.removeIfExists(serverAddress.and(SYSTEM_PROPERTY, propertyName));
        }
    }

    protected abstract void navigate();

    private void verifyOnServer(String propertyName, boolean shouldExist) throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(serverAddress.and(SYSTEM_PROPERTY, propertyName), client);
        if (shouldExist) {
            verifier.verifyExists();
        } else {
            verifier.verifyDoesNotExist();
        }
    }

    private void addPropertyToServer(String name, String value) throws IOException {
        operations.add(serverAddress.and(SYSTEM_PROPERTY, name), Values.of(VALUE, value)).assertSuccess();
    }
}
