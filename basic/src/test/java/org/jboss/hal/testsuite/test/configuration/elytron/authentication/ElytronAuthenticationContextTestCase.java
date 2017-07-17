package org.jboss.hal.testsuite.test.configuration.elytron.authentication;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.config.elytron.authentication.AddAuthenticationContextWizard;
import org.jboss.hal.testsuite.fragment.config.elytron.authentication.AddMatchRuleWizard;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.page.config.elytron.ElytronAuthenticationPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;

@RunWith(Arquillian.class)
public class ElytronAuthenticationContextTestCase extends AbstractElytronTestCase {

    @Page
    private ElytronAuthenticationPage page;

    private static final String
            MATCH_RULES = "match-rules",
            MATCH_RULES_LABEL = "Match Rules",
            EXTENDS = "extends",
            AUTHENTICATION_CONTEXT = "authentication-context",
            AUTHENTICATION_CONFIGURATION = "authentication-configuration",
            SSL_CONTEXT = "ssl-context",
            MATCH_ABSTRACT_TYPE = "match-abstract-type",
            MATCH_ABSTRACT_TYPE_AUTHORITY = "match-abstract-type-authority",
            MATCH_HOST = "match-host",
            MATCH_LOCAL_SECURITY_DOMAIN = "match-local-security-domain",
            MATCH_NO_USER = "match-no-user",
            MATCH_PATH = "match-path",
            MATCH_PORT = "match-port",
            MATCH_PROTOCOL = "match-protocol",
            MATCH_URN = "match-urn",
            MATCH_USER = "match-user";

    /**
     * @tpTestDetails Try to create Elytron Authentication context instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Authentication context table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddAuthenticationContext() throws Exception {
        final Address authenticationContextAddress = elyOps.getElytronAddress(AUTHENTICATION_CONTEXT, RandomStringUtils.randomAlphanumeric(7));

        page.navigate();

        try {
            page.switchToAuthenticationContext()
                    .getResourceManager()
                    .addResource(AddAuthenticationContextWizard.class)
                    .name(authenticationContextAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(authenticationContextAddress.getLastPairValue()));

            new ResourceVerifier(authenticationContextAddress, client).verifyExists();
        } finally {
            ops.removeIfExists(authenticationContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Try to create Elytron Authentication context instance which extends another instance in Web
     * Console's Elytron subsystem configuration.
     * Validate created resource is visible in Authentication context table.
     * Validate created resource is present in model.
     * Validate attributes of created resource in model.
     */
    @Test
    public void testAddAuthenticationContextWithExtends() throws Exception {
        final Address authenticationContextAddress = elyOps.getElytronAddress(AUTHENTICATION_CONTEXT, RandomStringUtils.randomAlphanumeric(7)),
                extendedContextAddress = createAuthenticationContext();

        try {
            page.navigate();
            page.switchToAuthenticationContext()
                    .getResourceManager()
                    .addResource(AddAuthenticationContextWizard.class)
                    .name(authenticationContextAddress.getLastPairValue())
                    .extendsContext(extendedContextAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(authenticationContextAddress.getLastPairValue()));

            new ResourceVerifier(authenticationContextAddress, client).verifyExists()
                    .verifyAttribute(EXTENDS, extendedContextAddress.getLastPairValue());
        } finally {
            ops.removeIfExists(authenticationContextAddress);
            ops.removeIfExists(extendedContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication context instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Authentication context table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void removeAuthenticationContext() throws Exception {
        final Address authenticationContextAddress = createAuthenticationContext();

        try {
            page.navigate();
            page.switchToAuthenticationContext()
                    .getResourceManager()
                    .removeResource(authenticationContextAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(authenticationContextAddress.getLastPairValue()));

            new ResourceVerifier(authenticationContextAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(authenticationContextAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication context instance in model and try to edit its extends attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editExtends() throws Exception {
        Address authenticationContextAddress = null,
                extendsContextAddress = null;

        try {
            authenticationContextAddress = createAuthenticationContext();
            extendsContextAddress = createAuthenticationContext();

            page.navigate();
            page.switchToAuthenticationContext()
                    .getResourceManager()
                    .selectByName(authenticationContextAddress.getLastPairValue());

            new ConfigChecker.Builder(client, authenticationContextAddress)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(ConfigChecker.InputType.TEXT, EXTENDS, extendsContextAddress.getLastPairValue())
                    .verifyFormSaved()
                    .verifyAttribute(EXTENDS, extendsContextAddress.getLastPairValue());

        } finally {
            if (authenticationContextAddress != null) {
                ops.removeIfExists(authenticationContextAddress);
            }
            if (extendsContextAddress != null) {
                ops.removeIfExists(extendsContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication context instance in model and try to edit its match-rules attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testAddMatchRule() throws Exception {
        Address authenticationContextAddress = null,
                authenticationConfigurationAddress = null,
                keyStoreAddress = null,
                keyManagerAddress = null,
                sslContextAddress = null;

        final String
                matchAbstractTypeValue = RandomStringUtils.randomAlphanumeric(7),
                matchAbstractTypeAuthorityValue = RandomStringUtils.randomAlphanumeric(7),
                matchHostValue = RandomStringUtils.randomAlphanumeric(7),
                matchLocalSecurityDomainValue = RandomStringUtils.randomAlphanumeric(7),
                matchPathValue = RandomStringUtils.randomAlphanumeric(7),
                matchProtocolValue = RandomStringUtils.randomAlphanumeric(7),
                matchUrnValue = RandomStringUtils.randomAlphanumeric(7),
                matchUserValue = RandomStringUtils.randomAlphanumeric(7);

        final long matchPortValue = 9993;

        final boolean matchNoUserValue = false;

        try {
            final UndertowElytronOperations undertowOperations = new UndertowElytronOperations(client);
            authenticationContextAddress = createAuthenticationContext();
            authenticationConfigurationAddress = createAuthenticationConfiguration();
            keyStoreAddress = undertowOperations.createKeyStore();
            keyManagerAddress = undertowOperations.createKeyManager(keyStoreAddress);
            sslContextAddress = undertowOperations.createClientSSLContext(keyManagerAddress);

            page.navigate();
            page.switchToAuthenticationContext()
                    .getResourceManager()
                    .selectByName(authenticationContextAddress.getLastPairValue());
            page.getConfig().switchTo(MATCH_RULES_LABEL).getResourceManager().addResource(AddMatchRuleWizard.class)
                    .matchAbstractType(matchAbstractTypeValue)
                    .matchAbstractTypeAuthority(matchAbstractTypeAuthorityValue)
                    .matchHost(matchHostValue)
                    .matchLocalSecurityDomain(matchLocalSecurityDomainValue)
                    .matchPath(matchPathValue)
                    .matchPort(matchPortValue)
                    .matchProtocol(matchProtocolValue)
                    .matchNoUser(matchNoUserValue)
                    .matchUrn(matchUrnValue)
                    .matchUser(matchUserValue)
                    .authenticationConfiguration(authenticationConfigurationAddress.getLastPairValue())
                    .sslContext(sslContextAddress.getLastPairValue())
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue("Match rule should be present in table! Probably failed because of" +
                            "https://issues.jboss.org/browse/HAL-1352",
                    page.getConfig().getResourceManager().getResourceTable().getAllRows().stream()
                    .anyMatch(resourceTableRowFragment -> resourceTableRowFragment.getCellValue(0)
                            .contains(MATCH_ABSTRACT_TYPE + ": " + matchAbstractTypeValue)));

            new ResourceVerifier(authenticationContextAddress, client).verifyListAttributeContainsValue(MATCH_RULES,
                    new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(MATCH_ABSTRACT_TYPE, matchAbstractTypeValue)
                            .addProperty(MATCH_ABSTRACT_TYPE_AUTHORITY, matchAbstractTypeAuthorityValue)
                            .addProperty(MATCH_HOST, matchHostValue)
                            .addProperty(MATCH_LOCAL_SECURITY_DOMAIN, matchLocalSecurityDomainValue)
                            .addProperty(MATCH_PATH, matchPathValue)
                            .addProperty(MATCH_PORT, new ModelNode(matchPortValue))
                            .addProperty(MATCH_PROTOCOL, matchProtocolValue)
                            .addProperty(MATCH_URN, matchUrnValue)
                            .addProperty(MATCH_USER, matchUserValue)
                            .addProperty(AUTHENTICATION_CONFIGURATION, authenticationConfigurationAddress.getLastPairValue())
                            .addProperty(SSL_CONTEXT, sslContextAddress.getLastPairValue())
                            .build()
            );
        } finally {
            if (authenticationContextAddress != null) {
                ops.removeIfExists(authenticationContextAddress);
            }
            if (authenticationConfigurationAddress != null) {
                ops.removeIfExists(authenticationConfigurationAddress);
            }
            if (sslContextAddress != null) {
                ops.removeIfExists(sslContextAddress);
            }
            if (keyManagerAddress != null) {
                ops.removeIfExists(keyManagerAddress);
            }
            if (keyStoreAddress != null) {
                ops.removeIfExists(keyStoreAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication context instance in model and try to edit its match-rules attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void testRemoveMatchRule() throws Exception {
        Address authenticationContextAddress = null;

        try {
            authenticationContextAddress = createAuthenticationContext();
            final String matchUserValue = addMatchRule(authenticationContextAddress);
            final String identifyingString = MATCH_USER + ": " + matchUserValue;

            page.navigate();
            page.switchToAuthenticationContext()
                    .getResourceManager()
                    .selectByName(authenticationContextAddress.getLastPairValue());

            //Nasty hack with fixed indexes since usual way of removing doesn't work
            page.getConfig()
                    .switchTo(MATCH_RULES_LABEL)
                    .getResourceManager()
                    .getResourceTable()
                    .getAllRows()
                    .get(0)
                    .getCell(0).click();

            page.getConfig().clickButton("Remove");
            Console.withBrowser(browser).openedWindow(ConfirmationWindow.class)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getConfig().getResourceManager().getResourceTable().getAllRows().stream()
                    .anyMatch(resourceTableRowFragment -> resourceTableRowFragment.getCellValue(0)
                            .contains(identifyingString)));

            new ResourceVerifier(authenticationContextAddress, client)
                    .verifyListAttributeDoesNotContainValue(MATCH_RULES,
                            new ModelNodeGenerator.ModelNodePropertiesBuilder().addProperty(MATCH_USER, matchUserValue)
                                    .build());
        } finally {
            if (authenticationContextAddress != null) {
                ops.removeIfExists(authenticationContextAddress);
            }
            adminOps.reloadIfRequired();
        }
    }

    private String addMatchRule(Address authenticationContextAddres) throws IOException {
        final String matchUserValue = RandomStringUtils.randomAlphanumeric(7);
        ops.writeAttribute(authenticationContextAddres, MATCH_RULES,
                new ModelNodeGenerator.ModelNodeListBuilder()
                        .addNode(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                                .addProperty(MATCH_USER, matchUserValue).build())
                        .build())
                .assertSuccess();
        return matchUserValue;
    }

    private Address createAuthenticationContext() throws IOException {
        final Address address = elyOps.getElytronAddress(AUTHENTICATION_CONTEXT, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address).assertSuccess();
        return address;
    }

    private Address createAuthenticationConfiguration() throws IOException {
        final Address address = elyOps.getElytronAddress(AUTHENTICATION_CONFIGURATION, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address).assertSuccess();
        return address;
    }

}
