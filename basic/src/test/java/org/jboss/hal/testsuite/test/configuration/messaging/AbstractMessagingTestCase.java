package org.jboss.hal.testsuite.test.configuration.messaging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.creaper.command.RemoveSocketBinding;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

public abstract class AbstractMessagingTestCase {

    protected static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static Administration administration;
    protected static Operations operations;

    protected static final Address MESSAGING_SUBSYSTEM = Address.subsystem("messaging-activemq");

    protected static final Address DEFAULT_MESSAGING_SERVER = MESSAGING_SUBSYSTEM.and("server", "default");

    private static List<String> socketBindings;

    @Page
    protected MessagingPage page;

    @Drone
    protected WebDriver browser;

    @BeforeClass
    public static void beforeClass_() {
        administration = new Administration(client);
        operations = new Operations(client);
    }

    @AfterClass
    public static void afterClass_() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try {
            removeAccumulatedSocketBindings();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    protected void editTextAndVerify(Address address, String name, String value) throws Exception {
        editTextAndVerify(address, name, name, value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, String value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String name, Integer value) throws Exception {
        editTextAndVerify(address, name, name, value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, Integer value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, value.toString());
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String name, Long value) throws Exception {
       editTextAndVerify(address, name, name, value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, Long value) throws Exception {
        editTextAndVerify(address, name, attributeName, value.toString(), value);
    }

    protected void editTextAndVerify(Address address, String name, String attributeName, String formValue, Long value) throws Exception {
        boolean finished = page.getConfigFragment().editTextAndSave(name, formValue);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attributeName, value);
    }

    protected void editCheckboxAndVerify(Address address, String name, boolean value) throws Exception {
        editCheckboxAndVerify(address, name, name, value);
    }

    protected void editCheckboxAndVerify(Address address, String identifier, String attribute, boolean value) throws Exception {
        boolean finished = page.getConfigFragment().editCheckboxAndSave(identifier, value);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(attribute, value);
    }

    protected  void selectOptionAndVerify(Address address, String name, String value) throws Exception {
        boolean finished = page.getConfigFragment().selectOptionAndSave(name, value);
        assertTrue("Config should be saved and closed.", finished);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client, 500).verifyAttribute(name, value);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShownInForm());
        config.cancel();
    }
    protected static String createSocketBinding(String name) throws CommandFailedException {
        client.apply(new AddSocketBinding.Builder(name)
                .port(AvailablePortFinder.getNextAvailableNonPrivilegedPort())
                .build());
        addSocketBindingToList(name);
        return name;
    }

    protected static String createSocketBinding() throws CommandFailedException {
        String name = "messaging_" + RandomStringUtils.randomAlphanumeric(6);
        return createSocketBinding(name);
    }


    private static void addSocketBindingToList(String name) {
        if (socketBindings == null) {
            socketBindings = new LinkedList<>();
        }
        socketBindings.add(name);
    }

    private static void removeAccumulatedSocketBindings() throws CommandFailedException {
        if (socketBindings == null) {
            return;
        }
        for (String socketBinding : socketBindings) {
            client.apply(new RemoveSocketBinding(socketBinding));
        }
        socketBindings.clear();
    }



}
