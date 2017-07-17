package org.jboss.hal.testsuite.test.runtime.server.configurationchanges;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.page.runtime.ConfigurationChangesPage;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.configurationchanges.ConfigurationChange;
import org.jboss.hal.testsuite.util.configurationchanges.ConfigurationChangesResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
public class ConfigurationChangesTestCase {

    private static final String LISTS_NOT_EQUAL_MESSAGE = "Some unexpected differences found! Either some changes are " +
            "not displayed or some extra appeared. Some could fail because of https://issues.jboss.org/browse/HAL-1355" +
            "because remote address is not set.";

    @Drone
    private WebDriver browser;

    @Page
    private ConfigurationChangesPage page;

    private static final int INITIAL_MAX_HISTORY_VALUE = 100;

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static final Administration administration = new Administration(client);
    private static final Address SERVICE_ADDRESS = client.options().isDomain ?
                                                    Address.host(client.options().defaultHost)
                                                            .and("subsystem", "core-management")
                                                            .and("service", "configuration-changes") :
                                                    Address.subsystem("core-management")
                                                            .and("service", "configuration-changes");
    private static ConfigurationChangesResource changesResource = new ConfigurationChangesResource(client);

    @Before
    public void before() throws Exception {
        //make sure the service is up and running
        operations.add(SERVICE_ADDRESS, Values.of("max-history", INITIAL_MAX_HISTORY_VALUE)).assertSuccess();
        page.navigate();
    }

    @After
    public void after() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(SERVICE_ADDRESS);
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        try {
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void disableAndEnableTracking() throws Exception {
        final int MAX_HISTORY = 42;

        page.disable();
        new ResourceVerifier(SERVICE_ADDRESS, client)
                .verifyDoesNotExist();

        page.openEnableConfigurationChangesDialog()
                .maxHistory(MAX_HISTORY)
                .clickSave();
        new ResourceVerifier(SERVICE_ADDRESS, client)
                .verifyAttribute("max-history", MAX_HISTORY);
    }

    @Test
    public void allRelevantRecordsAreShown() throws IOException, ParseException {
        assertListEquals("Probably fails because of https://issues.jboss.org/browse/HAL-1355.",
                changesResource.getAllConfigurationChanges(),
                page.getConfigurationChangesTable().getAllConfigurationChanges());
    }

    @Test
    public void filterConfigurationChangesCapabilities() throws Exception {
        List<ConfigurationChange> filtered_S_configurationChanges = page.filter().setFilter("s").getAllConfigurationChanges();

        assertListEquals(LISTS_NOT_EQUAL_MESSAGE,
                changesResource.getFilteredConfigurationChanges(page.filter().getCurrentFilter()),
                filtered_S_configurationChanges);

        List<ConfigurationChange> filtered_SU_configurationChanges = page.filter().appendSymbol('u').getAllConfigurationChanges();

        assertListEquals(LISTS_NOT_EQUAL_MESSAGE,
                changesResource.getFilteredConfigurationChanges(page.filter().getCurrentFilter()),
                filtered_SU_configurationChanges);

        List<ConfigurationChange> filtered_STAR_configurationChanges = page.filter().appendSymbol('*').getAllConfigurationChanges();
        Assert.assertTrue("Some unexpected configuration changes appeared.", filtered_STAR_configurationChanges.isEmpty());

        assertListEquals(LISTS_NOT_EQUAL_MESSAGE,
                filtered_SU_configurationChanges,
                page.filter().removeSymbol().getAllConfigurationChanges());
    }

    @Test
    public void generateSomeRecordsAndVerifyAllRelevantRecordsAreShownAfterRefresh()
            throws IOException, ParseException, TimeoutException, InterruptedException, CommandFailedException {
        //generate records
        final SnapshotBackup snapshotBackup = new SnapshotBackup();
        try {
            client.apply(snapshotBackup.backup());

            BackupAndRestoreAttributes backup = new BackupAndRestoreAttributes.Builder(Address.subsystem("datasources")
                    .and("data-source", "ExampleDS"))
                    .build();
            client.apply(backup.backup());
            client.apply(backup.restore());

            Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
            administration.reloadIfRequired();

            //new records were generated but they should not be present in GUI yet

            Assert.assertNotEquals(page.getConfigurationChangesTable().getAllConfigurationChanges(),
                    changesResource.getAllConfigurationChanges());

            page.refresh();

            assertListEquals(LISTS_NOT_EQUAL_MESSAGE, changesResource.getAllConfigurationChanges(),
                    page.getConfigurationChangesTable().getAllConfigurationChanges());
        } finally {
            client.apply(snapshotBackup.restore());
        }
    }

    private static void assertListEquals(List expected, List actual) {
        assertListEquals(null, expected, actual);
    }

    private static void assertListEquals(String message, List expected, List actual) {

        StringBuilder stringBuilder = new StringBuilder()
                .append(message == null ? "" : message)
                .append("\n")
                .append("Expected:<")
                .append(expected)
                .append(">\nActual:<")
                .append(actual)
                .append(">");

        if (!CollectionUtils.isEqualCollection(expected, actual)) {
            throw new AssertionError(stringBuilder.toString());
        }
    }
}
