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
public class ACLTestCase extends SecurityTestCaseAbstract {

    private static final Address ACL_ADDRESS = JBOSS_WEB_ADDRESS.and("acl", "classic");
    private static final String ACL_MODULE_NAME = "acl-module_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ACL_MODULE_TBA_NAME = "acl-module-TBA_" + RandomStringUtils.randomAlphanumeric(5);
    private static final Address ACL_MODULE_ADDRESS = ACL_ADDRESS.and("acl-module", ACL_MODULE_NAME);
    private static final Address ACL_MODULE_TBA_ADDRESS = ACL_ADDRESS.and("acl-module", ACL_MODULE_TBA_NAME);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        operations.add(ACL_ADDRESS);
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + ACL_MODULE_NAME;
        operations.add(ACL_MODULE_ADDRESS, Values.of("code", code).and("flag", "optional"));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(ACL_MODULE_ADDRESS);
        operations.removeIfExists(ACL_MODULE_TBA_ADDRESS);
        operations.removeIfExists(ACL_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewJBossWebPolicy();
        page.switchToACL();
        page.selectModule(ACL_MODULE_NAME);
    }

    @Test
    public void editModuleOptions() throws Exception {
        editModuleOptionsAndVerify(ACL_MODULE_ADDRESS, MODULE_OPTIONS, MODULE_OPTIONS_ATTR, MODULE_OPTIONS_VALUE);
    }

    @Test
    public void addACLModule() throws Exception {
        String code = "code_" + ACL_MODULE_TBA_NAME;
        page.addACLModule(ACL_MODULE_TBA_NAME, code);
        new ResourceVerifier(ACL_MODULE_TBA_ADDRESS, client).verifyExists();
    }
}
