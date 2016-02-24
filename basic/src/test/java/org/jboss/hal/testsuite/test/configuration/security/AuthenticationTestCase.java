package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.junit.AfterClass;
import org.junit.Assert;
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
public class AuthenticationTestCase extends SecurityTestCaseAbstract {

    private static final Address AUTHENTICATION_ADDRESS = JBOSS_EJB_ADDRESS.and("authentication", "classic");
    private static final String LOGIN_MODULE_NAME = "lm_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String LOGIN_MODULE_TBA_NAME = "lm-TBA_" + RandomStringUtils.randomAlphanumeric(6);
    private static Address LOGIN_MODULE_ADDRESS = AUTHENTICATION_ADDRESS.and("login-module", LOGIN_MODULE_NAME);
    private static Address LOGIN_MODULE_TBA_ADDRESS = AUTHENTICATION_ADDRESS.and("login-module", LOGIN_MODULE_TBA_NAME);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        operations.add(AUTHENTICATION_ADDRESS);
        operations.add(LOGIN_MODULE_ADDRESS,
                Values.of("code", RandomStringUtils.randomAlphanumeric(8)).and("flag", "optional"));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(LOGIN_MODULE_ADDRESS);
        operations.removeIfExists(LOGIN_MODULE_TBA_ADDRESS);
        operations.removeIfExists(AUTHENTICATION_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToAuthentication();
        page.selectModule(LOGIN_MODULE_NAME);
    }

    @Test
    public void addLoginModuleInGUI() throws Exception {
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + LOGIN_MODULE_TBA_NAME;
        page.addAuthenticationModule(LOGIN_MODULE_TBA_NAME, code);
        Assert.assertTrue("Login module should be present in table", page.getConfigFragment()
                .resourceIsPresent(LOGIN_MODULE_TBA_NAME));
        new ResourceVerifier(LOGIN_MODULE_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void editCode() throws Exception {
        editTextAndVerify(LOGIN_MODULE_ADDRESS, CODE, CODE_ATTR);
    }


    @Test
    public void editModule() throws Exception {
        editTextAndVerify(LOGIN_MODULE_ADDRESS, MODULE, MODULE_ATTR);
    }

    @Test
    public void editModuleOptions() throws Exception {
        editModuleOptionsAndVerify(LOGIN_MODULE_ADDRESS, MODULE_OPTIONS, MODULE_OPTIONS_ATTR, MODULE_OPTIONS_VALUE);
    }

    @Test
    public void selectFlag() throws Exception {
        selectOptionAndVerify(LOGIN_MODULE_ADDRESS, FLAG, FLAG_ATTR, FLAG_VALUE);
    }
}
