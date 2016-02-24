package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.page.config.SecurityPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;

public abstract class SecurityTestCaseAbstract {

    @Page
    public SecurityPage page;

    @Drone
    public WebDriver browser;

    protected final String CODE = "code";
    protected final String FLAG = "flag";
    protected final String MODULE = "module";
    protected final String MODULE_OPTIONS = "module-options";

    protected final String CODE_ATTR = "code";
    protected final String FLAG_ATTR = "flag";
    protected final String MODULE_ATTR = "module";
    protected final String MODULE_OPTIONS_ATTR = "module-options";

    protected final String FLAG_VALUE = "optional";
    protected final String[] MODULE_OPTIONS_VALUE = new String[]{"example=value", "test=example"};

    protected static OnlineManagementClient client;
    protected static Administration administration;
    protected static Operations operations;

    protected static final String JBOSS_EJB_POLICY =  "jboss-ejb-policy";
    protected static final String JBOSS_WEB_POLICY =  "jboss-web-policy";
    protected static final String OTHER_POLICY = "other";

    protected static final Address JBOSS_EJB_ADDRESS = Address.subsystem("security").and("security-domain", JBOSS_EJB_POLICY);
    protected static final Address JBOSS_WEB_ADDRESS = Address.subsystem("security").and("security-domain", JBOSS_WEB_POLICY);
    protected static final Address OTHER_ADDRESS = Address.subsystem("security").and("security-domain", OTHER_POLICY);


    @BeforeClass
    public static void mainSetUp() {
        client = ManagementClientProvider.createOnlineManagementClient();
        administration = new Administration(client);
        operations = new Operations(client);
    }

    @Before
    public void mainBefore() {
        page.navigate();
    }

    @AfterClass
    public static void tearDown_() throws IOException {
        client.close();
    }

    protected void editModuleOptionsAndVerify(Address address, String identifier, String attributeName, String[] values) throws Exception {
        page.editTextAndSave(identifier, String.join("\n", values));
        ModelNode response = new ModelNode();
        for (int i = 0; i < values.length; i++) {
            String[] pair = values[i].split("=");
            response.get(pair[0]).set(pair[1]);
        }
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(attributeName, response.asObject());
    }

    protected void editTextAndVerify(Address address, String identifier, String attributeName, String value) throws Exception {
        page.editTextAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    protected void editTextAndVerify(Address address, String identifier, String attributeName) throws Exception {
        editTextAndVerify(address, identifier, attributeName, "sec_" + attributeName + RandomStringUtils.randomAlphabetic(4));
    }

    protected void editCheckboxAndVerify(Address address, String identifier, String attributeName, Boolean value) throws Exception {
        page.editCheckboxAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    public void selectOptionAndVerify(Address address, String identifier, String attributeName, String value) throws Exception {
        page.selectOptionAndSave(identifier, value);
        administration.reloadIfRequired();
        new ResourceVerifier(address, client).verifyAttribute(attributeName, value);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        ConfigFragment config = page.getConfigFragment();
        page.editTextAndSave(identifier, value);
        Assert.assertTrue(page.isErrorShownInForm());
        config.cancel();
    }

}
