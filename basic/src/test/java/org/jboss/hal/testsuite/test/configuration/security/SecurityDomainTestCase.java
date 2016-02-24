package org.jboss.hal.testsuite.test.configuration.security;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.security.SecurityDomainAddWizard;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class SecurityDomainTestCase extends SecurityTestCaseAbstract {

    private static final String SECURITY_DOMAIN_TBR = "sec-domain-TBR_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String SECURITY_DOMAIN_TBA = "sec-domain-TBA_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address SECURITY_SUB = Address.subsystem("security");
    private static final Address SECURITY_DOMAIN_TBA_ADDRESS = SECURITY_SUB.and("security-domain", SECURITY_DOMAIN_TBA);
    private static final Address SECURITY_DOMAIN_TBR_ADDRESS = SECURITY_SUB.and("security-domain", SECURITY_DOMAIN_TBR);

    @BeforeClass
    public static void beforeClass() throws IOException, TimeoutException, InterruptedException {
        operations.add(SECURITY_DOMAIN_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, OperationException, TimeoutException, InterruptedException {
        operations.removeIfExists(SECURITY_DOMAIN_TBA_ADDRESS);
        operations.removeIfExists(SECURITY_DOMAIN_TBR_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSecurityDomainInGUI() throws Exception {
        SecurityDomainAddWizard wizard = page.addSecurityDomain();
        wizard.name(SECURITY_DOMAIN_TBA)
            .cacheType("default")
            .finish();
        Assert.assertTrue("Security domain should be visible", page.isDomainPresent(SECURITY_DOMAIN_TBA));
        new ResourceVerifier(SECURITY_DOMAIN_TBA_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeSecurityDomainInGUI() throws Exception {
        page.removeSecurityDomain(SECURITY_DOMAIN_TBR);
        new ResourceVerifier(SECURITY_DOMAIN_TBR_ADDRESS, client).verifyDoesNotExist();
    }
}
