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
public class AuditTestCase extends SecurityTestCaseAbstract {

    private static final Address AUDIT_ADDRESS = JBOSS_EJB_ADDRESS.and("audit", "classic");
    private static final String PROVIDER_MODULE_NAME = "provider-module_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROVIDER_MODULE_TBA_NAME = "prm-TBA_" + RandomStringUtils.randomAlphanumeric(6);
    private static final Address PROVIDER_MODULE_ADDRESS = AUDIT_ADDRESS.and("provider-module", PROVIDER_MODULE_NAME);
    private static final Address PROVIDER_MODULE_TBA_ADDRESS = AUDIT_ADDRESS
            .and("provider-module", PROVIDER_MODULE_TBA_NAME);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(AUDIT_ADDRESS);
        operations.add(PROVIDER_MODULE_ADDRESS, Values.of("code", RandomStringUtils.randomAlphanumeric(8)));
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(PROVIDER_MODULE_ADDRESS);
        operations.removeIfExists(PROVIDER_MODULE_TBA_ADDRESS);
        operations.removeIfExists(AUDIT_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewJBossEJBPolicy();
        page.switchToAudit();
        page.selectModule(PROVIDER_MODULE_NAME);
    }

    @Test
    public void editCode() throws Exception {
        editTextAndVerify(PROVIDER_MODULE_ADDRESS, CODE, CODE_ATTR);
    }

    @Test
    public void addProviderModuleInGUI() throws Exception {
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + PROVIDER_MODULE_TBA_NAME;
        page.addAuditModule(PROVIDER_MODULE_TBA_NAME, code);
        Assert.assertTrue("Provider module should be present in table", page.getConfigFragment()
                .resourceIsPresent(PROVIDER_MODULE_TBA_NAME));
        new ResourceVerifier(PROVIDER_MODULE_TBA_ADDRESS, client).verifyExists();
    }


}
