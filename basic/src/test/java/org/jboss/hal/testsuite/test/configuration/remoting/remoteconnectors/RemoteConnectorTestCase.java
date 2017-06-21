package org.jboss.hal.testsuite.test.configuration.remoting.remoteconnectors;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.page.config.RemotingSubsystemPage;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.socketbindings.RemoveSocketBinding;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Testing settings of Remote Connectors
 * TODO: Expand tests
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RemoteConnectorTestCase {

    @Drone
    private WebDriver browser;

    @Page
    private RemotingSubsystemPage page;

    private static final String
            SOCKET_BINDING = "socket-binding",
            SOCKET_BINDING_1 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_2 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_3 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7),
            SOCKET_BINDING_4 = "socket_binding_" + RandomStringUtils.randomAlphanumeric(7);

    private static final Address
            REMOTING_SUBSYSTEM_ADDRESS = Address.subsystem("remoting"),
            CONNECTOR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("connector", "connector" + RandomStringUtils.randomAlphanumeric(7)),
            CONNECTOR_TBA_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("connector", "connector_tba" + RandomStringUtils.randomAlphanumeric(7)),
            CONNECTOR_TBR_ADDRESS = REMOTING_SUBSYSTEM_ADDRESS.and("connector", "connector_tbr" + RandomStringUtils.randomAlphanumeric(7));

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeClass() throws CommandFailedException, IOException, TimeoutException, InterruptedException {
        createSocketBinding(SOCKET_BINDING_1);
        createSocketBinding(SOCKET_BINDING_2);
        createSocketBinding(SOCKET_BINDING_3);
        createSocketBinding(SOCKET_BINDING_4);
        operations.add(CONNECTOR_ADDRESS, Values.of(SOCKET_BINDING, SOCKET_BINDING_1)).assertSuccess();
        operations.add(CONNECTOR_TBR_ADDRESS, Values.of(SOCKET_BINDING, SOCKET_BINDING_2)).assertSuccess();
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.switchToRemoteConnectorsTab();
        page.getResourceManager().selectByName(CONNECTOR_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException, CommandFailedException {
        try {
            operations.removeIfExists(CONNECTOR_ADDRESS);
            operations.removeIfExists(CONNECTOR_TBR_ADDRESS);
            operations.removeIfExists(CONNECTOR_TBA_ADDRESS);
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_1));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_2));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_3));
            client.apply(new RemoveSocketBinding(SOCKET_BINDING_4));
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addConnector() throws Exception {
        page.addNativeConnector()
                .name(CONNECTOR_TBA_ADDRESS.getLastPairValue())
                .socketBinding(SOCKET_BINDING_3)
                .saveAndDismissReloadRequiredWindow();

        Assert.assertTrue(page.getResourceManager().isResourcePresent(CONNECTOR_TBA_ADDRESS.getLastPairValue()));
        new ResourceVerifier(CONNECTOR_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeConnector() throws Exception {
        page.getResourceManager().removeResource(CONNECTOR_TBR_ADDRESS.getLastPairValue())
                .confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse(page.getResourceManager().isResourcePresent(CONNECTOR_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(CONNECTOR_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editSocketBinding() throws Exception {
        final String value = SOCKET_BINDING_4;
        final ModelNodeResult originalModelNodeResult = operations.readAttribute(CONNECTOR_ADDRESS, SOCKET_BINDING);
        originalModelNodeResult.assertSuccess();
        try {
            ResourceVerifier resourceVerifier = new ConfigChecker.Builder(client, CONNECTOR_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, SOCKET_BINDING, value)
                    .verifyFormSaved();

            administration.reloadIfRequired();

            resourceVerifier.verifyAttribute(SOCKET_BINDING, value);
        } finally {
            operations.writeAttribute(CONNECTOR_ADDRESS, SOCKET_BINDING, originalModelNodeResult.value());
        }
    }

    private static void createSocketBinding(String name) throws CommandFailedException {
        client.apply(new AddSocketBinding.Builder(name)
                .port(AvailablePortFinder.getNextAvailableNonPrivilegedPort())
                .build());
    }

}
