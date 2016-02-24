package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class AuthorizationTestCase extends SecurityTestCaseAbstract {

    private static String POLICY_MODULE_NAME = "pm_" + RandomStringUtils.randomAlphanumeric(6);
    private static String POLICY_MODULE_TBR_NAME = "pm-TBR_" + RandomStringUtils.randomAlphanumeric(6);

    private static final Address AUTHORIZATION_ADDRESS = JBOSS_EJB_ADDRESS.and("authorization", "classic");
    private static final Address MODULE_ADDRESS = AUTHORIZATION_ADDRESS.and("policy-module", POLICY_MODULE_NAME);
    private static final Address MODULE_TBR_ADDRESS = AUTHORIZATION_ADDRESS.and("policy-module", POLICY_MODULE_TBR_NAME);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(MODULE_ADDRESS, Values.of("code", RandomStringUtils.randomAlphanumeric(8)).and("flag", "optional"));
        operations.add(MODULE_TBR_ADDRESS, Values.of("code", RandomStringUtils.randomAlphanumeric(8)).and("flag", "optional"));
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(MODULE_ADDRESS);
        operations.removeIfExists(MODULE_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToAuthorization();
        page.selectModule(POLICY_MODULE_NAME);
    }

    @Test
    public void selectFlag() throws Exception {
        selectOptionAndVerify(MODULE_ADDRESS, FLAG, FLAG_ATTR, FLAG_VALUE);
    }

    @Test
    public void removeModuleInGUI() throws Exception {
        page.getResourceManager().removeResource(POLICY_MODULE_TBR_NAME).confirmAndDismissReloadRequiredMessage();
        new ResourceVerifier(MODULE_TBR_ADDRESS, client).verifyDoesNotExist();
    }

    @Test
    public void editModuleOptions() throws Exception {
        editModuleOptionsAndVerify(MODULE_ADDRESS, MODULE_OPTIONS, MODULE_OPTIONS_ATTR, MODULE_OPTIONS_VALUE);
    }

}
