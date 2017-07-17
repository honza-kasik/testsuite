package org.jboss.hal.testsuite.test.configuration.ee;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.config.EEPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class EETestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private final Operations operations = new Operations(client);
    private final Address eeAddress = Address.subsystem("ee");

    @Drone
    public WebDriver browser;

    @Page
    public EEPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        client.close();
    }

    @Test
    public void toggleIsolatedDeployments() throws Exception {
        final String attributeName = "ear-subdeployments-isolated";
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(eeAddress, attributeName);
        originalModelNodeResult.assertSuccess();
        final boolean originalBooleanValue = originalModelNodeResult.booleanValue();
        try {
            new ConfigChecker.Builder(client, eeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, !originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, !originalBooleanValue);

            new ConfigChecker.Builder(client, eeAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.CHECKBOX, attributeName, originalBooleanValue)
                    .verifyFormSaved()
                    .verifyAttribute(attributeName, originalBooleanValue);
        } finally {
            operations.writeAttribute(eeAddress, attributeName, originalModelNodeResult.value());
        }
    }
}
