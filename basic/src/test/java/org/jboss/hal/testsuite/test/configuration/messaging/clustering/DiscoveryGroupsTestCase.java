package org.jboss.hal.testsuite.test.configuration.messaging.clustering;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
public class DiscoveryGroupsTestCase extends AbstractMessagingTestCase {

    private static final String
            REFRESH_TIMEOUT = "refresh-timeout",
            INITIAL_WAIT_TIMEOUT = "initial-wait-timeout",
            JGROUPS_CHANNEL = "jgroups-channel",
            DISCOVERY_GROUP = "discovery-group",
            SOCKET_BINDING = "socket-binding";

    private static final Address DG_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP,
            "dg-group_" + RandomStringUtils.randomAlphabetic(7));
    private static final Address DG_TBR_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP,
            "dg-group_TBR_" + RandomStringUtils.randomAlphabetic(7));
    private static final Address DG_TBA_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP,
            "dg-group_TBA_" + RandomStringUtils.randomAlphabetic(7));
    private static final Address DG_TBA2_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP,
            "dg-group_TBA2_" + RandomStringUtils.randomAlphanumeric(5));
    private static final Address DG_INV_ADDRESS = DEFAULT_MESSAGING_SERVER.and(DISCOVERY_GROUP,
            "dg-group_INV_" + RandomStringUtils.randomAlphanumeric(5));

    private static final List<String> socketBindings = new LinkedList<>();

    private static final OnlineManagementClient discoveryGroupClient =  ConfigUtils.isDomain() ?
            ManagementClientProvider.withProfile("full-ha") :
            client;
    private static final Operations discoveryGroupOps = new Operations(discoveryGroupClient);

    @BeforeClass
    public static void setUp() throws Exception {
        socketBindings.add(createSocketBinding());
        socketBindings.add(createSocketBinding());
        discoveryGroupOps.add(DG_ADDRESS, Values.of(JGROUPS_CHANNEL, "ee")).assertSuccess();
        discoveryGroupOps.add(DG_TBR_ADDRESS, Values.of(JGROUPS_CHANNEL, "ee")).assertSuccess();
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException, CommandFailedException, TimeoutException, InterruptedException {
        discoveryGroupOps.removeIfExists(DG_ADDRESS);
        discoveryGroupOps.removeIfExists(DG_TBA_ADDRESS);
        discoveryGroupOps.removeIfExists(DG_TBA2_ADDRESS);
        discoveryGroupOps.removeIfExists(DG_TBR_ADDRESS);
        discoveryGroupOps.removeIfExists(DG_INV_ADDRESS);
    }

    @Page
    private MessagingPage page;

    @Before
    public void before() {
        if (ConfigUtils.isDomain()) {
            page.viewClusteringSettingsOnProfile("default", "full-ha");
        } else {
            page.viewClusteringSettings("default");
        }
        page.switchToDiscovery();
        page.selectInTable(DG_ADDRESS.getLastPairValue(), 0);
    }

    @Ignore("Ignored until https://issues.jboss.org/browse/WFLY-6607 is resolved")
    @Test
    public void addDiscoveryGroupWithSocketBindingDefined() throws Exception {
        page.addDiscoveryGroup()
                .name(DG_TBA_ADDRESS.getLastPairValue())
                .socketBinding(socketBindings.get(0))
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();

        Assert.assertTrue("Discovery group should be present!",
                page.getResourceManager().isResourcePresent(DG_TBA_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void addDiscoveryGroupWithJGroupsChannelDefined() throws Exception {
        page.addDiscoveryGroup()
                .name(DG_TBA2_ADDRESS.getLastPairValue())
                .jgroupsChannel("ee")
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowClosed();

        Assert.assertTrue("Discovery group should be present!",
                page.getResourceManager().isResourcePresent(DG_TBA2_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_TBA2_ADDRESS, discoveryGroupClient).verifyExists();
    }

    @Test
    public void addDiscoveryGroupWithOnlyNameDefined() throws Exception {
        page.addDiscoveryGroup()
                .name(DG_INV_ADDRESS.getLastPairValue())
                .saveAndDismissReloadRequiredWindowWithState()
                .assertWindowOpen();

        Assert.assertFalse("Discovery group should NOT be present!",
                page.getResourceManager().isResourcePresent(DG_INV_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_INV_ADDRESS, discoveryGroupClient).verifyDoesNotExist();
    }

    @Ignore("Ignored until https://issues.jboss.org/browse/WFLY-6607 is resolved")
    @Test
    public void updateDiscoveryGroupSocketBinding() throws Exception {
        editTextAndVerify(DG_ADDRESS, SOCKET_BINDING, socketBindings.get(1));
    }

    @Test
    public void updateDiscoveryGroupRefreshTimeout() throws Exception {
        final long value = 2000;
        new ConfigChecker.Builder(discoveryGroupClient, DG_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, REFRESH_TIMEOUT, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(REFRESH_TIMEOUT, value);
    }

    @Test
    public void updateBroadcastGroupRefreshTimeoutNegativeValue() throws Exception {
        new ConfigChecker.Builder(discoveryGroupClient, DG_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, REFRESH_TIMEOUT, "-1")
                .verifyFormNotSaved();
    }

    @Test
    public void updateDiscoveryGroupInitialTimeout() throws Exception {
        final long value = 200;
        new ConfigChecker.Builder(discoveryGroupClient, DG_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, INITIAL_WAIT_TIMEOUT, String.valueOf(value))
                .verifyFormSaved()
                .verifyAttribute(INITIAL_WAIT_TIMEOUT, value);
    }

    @Test
    public void updateBroadcastGroupInitialTimeoutNegativeValue() throws Exception {
        new ConfigChecker.Builder(discoveryGroupClient, DG_ADDRESS)
                .configFragment(page.getConfigFragment())
                .editAndSave(TEXT, INITIAL_WAIT_TIMEOUT, "-1")
                .verifyFormNotSaved();
    }

    @Test
    public void removeDiscoveryGroup() throws Exception {
        page.getResourceManager()
                .removeResourceAndConfirm(DG_TBR_ADDRESS.getLastPairValue());

        Assert.assertFalse(page.getResourceManager().isResourcePresent(DG_TBR_ADDRESS.getLastPairValue()));

        new ResourceVerifier(DG_TBR_ADDRESS, discoveryGroupClient).verifyDoesNotExist();
    }

}
