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
public class MappingTestCase extends SecurityTestCaseAbstract {

    private static final Address MAPPING_ADDRESS = OTHER_ADDRESS.and("mapping", "classic");
    private static final String MAPPING_MODULE_NAME = "map_" + RandomStringUtils.randomAlphanumeric(6);
    private static final String MAPPING_MODULE_TBA_NAME = "map-TBA_" + RandomStringUtils.randomAlphanumeric(6);
    private static final Address MAPPING_MODULE_ADDRESS = MAPPING_ADDRESS.and("mapping-module", MAPPING_MODULE_NAME);
    private static final Address MAPPING_MODULE_TBA_ADDRESS = MAPPING_ADDRESS
            .and("mapping-module", MAPPING_MODULE_TBA_NAME);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        operations.add(MAPPING_ADDRESS);
        operations.add(MAPPING_MODULE_ADDRESS, Values.of("code", RandomStringUtils.randomAlphanumeric(8))
                .and("type", RandomStringUtils.randomAlphanumeric(8)));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(MAPPING_MODULE_ADDRESS);
        operations.removeIfExists(MAPPING_MODULE_TBA_ADDRESS);
        operations.removeIfExists(MAPPING_ADDRESS);
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewOther();
        page.switchToMapping();
        page.selectModule(MAPPING_MODULE_NAME);
    }

    @Test
    public void editCode() throws Exception {
        editTextAndVerify(MAPPING_MODULE_ADDRESS, CODE, CODE_ATTR);
    }

    @Test
    public void addMappingModuleInGUI() throws Exception {
        String code = "code_" + RandomStringUtils.randomAlphanumeric(6) + "-" + MAPPING_MODULE_TBA_NAME;
        String type = "type_" + RandomStringUtils.randomAlphanumeric(6) + "-" + MAPPING_MODULE_TBA_NAME;
        page.addMappingModule(MAPPING_MODULE_TBA_NAME, code, type);
        Assert.assertTrue("Mapping module should be present in table", page.getConfigFragment()
                .resourceIsPresent(MAPPING_MODULE_TBA_NAME));
        new ResourceVerifier(MAPPING_MODULE_TBA_ADDRESS, client)
                .verifyExists();
    }
}
