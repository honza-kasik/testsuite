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
public class IdentityTrustTestCase extends SecurityTestCaseAbstract {

    private static final String TRUST_MODULE_NAME = "tm_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String TRUST_MODULE_TBA_NAME = "tm-TBA_" + RandomStringUtils.randomAlphanumeric(6);
    private static final Address TRUST_ADDRESS = JBOSS_EJB_ADDRESS.and("identity-trust", "classic");
    private static final Address TRUST_MODULE_ADDRESS = TRUST_ADDRESS.and("trust-module", TRUST_MODULE_NAME);
    private static final Address TRUST_MODULE_TBA_ADDRESS = TRUST_ADDRESS.and("trust-module", TRUST_MODULE_TBA_NAME);

    @BeforeClass
    public static void setUp() throws InterruptedException, TimeoutException, IOException {
        operations.add(TRUST_ADDRESS);
        operations.add(TRUST_MODULE_ADDRESS, Values.of("code", RandomStringUtils.randomAlphanumeric(8))
                .and("flag", "optional"));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(TRUST_MODULE_ADDRESS);
        operations.removeIfExists(TRUST_MODULE_TBA_ADDRESS);
        operations.removeIfExists(TRUST_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToIdentityTrust();
        page.selectModule(TRUST_MODULE_NAME);
    }

    @Test
    public void editCode() throws Exception {
        editTextAndVerify(TRUST_MODULE_ADDRESS, CODE, CODE_ATTR);
    }

    @Test
    public void addTrustModuleInGUI() throws Exception {
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + TRUST_MODULE_TBA_NAME;
        page.addTrustModule(TRUST_MODULE_TBA_NAME, code);
        Assert.assertTrue("Trust module should be present in table", page.getConfigFragment()
                .resourceIsPresent(TRUST_MODULE_TBA_NAME));
        new ResourceVerifier(TRUST_MODULE_TBA_ADDRESS, client).verifyExists();
    }
}
