package org.jboss.hal.testsuite.test.configuration.elytron.authentication;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator.ModelNodeListBuilder;
import org.jboss.hal.testsuite.fragment.config.elytron.authentication.AddAuthenticationConfigurationWizard;
import org.jboss.hal.testsuite.page.config.elytron.ElytronAuthenticationPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowElytronOperations;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ElytronIntegrationChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class ElytronAuthenticationConfigurationTestCase extends AbstractElytronTestCase {

    @Page
    private ElytronAuthenticationPage page;

    private final ElytronAuthenticationOperations elytronAuthenticationOperations = new ElytronAuthenticationOperations(client);

    private static final String
            AUTHENTICATION_CONFIGURATION = "authentication-configuration",
            ANONYMOUS = "anonymous",
            AUTHENTICATION_NAME = "authentication-name",
            AUTHORIZATION_NAME = "authorization-name",
            CLEAR_TEXT = "clear-text",
            EXTENDS = "extends",
            HOST = "host",
            KERBEROS_SECURITY_FACTORY = "kerberos-security-factory",
            MECHANISM_PROPERTIES = "mechanism-properties",
            PORT = "port",
            PROTOCOL = "protocol",
            REALM = "realm",
            SASL_MECHANISM_SELECTOR = "sasl-mechanism-selector",
            SECURITY_DOMAIN = "security-domain",
            CREDENTIAL_REFERENCE = "credential-reference",
            CREDENTIAL_REFERENCE_LABEL = "Credential Reference";

    /**
     * @tpTestDetails Try to create Elytron Authentication configuration instance in Web Console's Elytron subsystem
     * configuration.
     * Validate created resource is visible in Authentication configuration table.
     * Validate created resource is present in model.
     */
    @Test
    public void testAddAuthenticationConfigurationTestCase() throws Exception {
        final String credentialStoreClearTextValue = RandomStringUtils.randomAlphabetic(7);

        final Address authenticationConfigurationAddress = elyOps.getElytronAddress(AUTHENTICATION_CONFIGURATION, RandomStringUtils.randomAlphanumeric(7));

        page.navigate();

        try {
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .addResource(AddAuthenticationConfigurationWizard.class)
                    .name(authenticationConfigurationAddress.getLastPairValue())
                    .clearTextCredentialStoreReference(credentialStoreClearTextValue)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();

            Assert.assertTrue(page.getResourceManager().isResourcePresent(authenticationConfigurationAddress.getLastPairValue()));

            new ResourceVerifier(authenticationConfigurationAddress, client).verifyExists()
                    .verifyAttribute(CREDENTIAL_REFERENCE, new ModelNodeGenerator.ModelNodePropertiesBuilder()
                            .addProperty(CLEAR_TEXT, credentialStoreClearTextValue)
                            .build());
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to remove it in Web Console's
     * Elytron subsystem configuration.
     * Validate the resource is not any more visible in Authentication configuration table.
     * Validate created resource is not any more present in the model.
     */
    @Test
    public void testRemoveAuthenticationConfigurationTestCase() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        page.navigate();

        try {
            page.switchToAuthenticationConfiguration()
                    .getResourceManager()
                    .removeResource(authenticationConfigurationAddress.getLastPairValue())
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();

            Assert.assertFalse(page.getResourceManager().isResourcePresent(authenticationConfigurationAddress.getLastPairValue()));

            new ResourceVerifier(authenticationConfigurationAddress, client).verifyDoesNotExist();
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            adminOps.reloadIfRequired();
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its anonymous
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void toggleAnonymous() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();
        final boolean initialValue = false;

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    ANONYMOUS,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.CHECKBOX, ANONYMOUS, !initialValue)
                                .verifyFormSaved()
                                .verifyAttribute(ANONYMOUS, !initialValue);

                        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();

                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.CHECKBOX, ANONYMOUS, initialValue)
                                .verifyFormSaved()
                                .verifyAttribute(ANONYMOUS, initialValue);
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * authentication-name attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAuthenticationName() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    AUTHENTICATION_NAME,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, AUTHENTICATION_NAME, value)
                                .verifyFormSaved()
                                .verifyAttribute(AUTHENTICATION_NAME, value);                   }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * authorization-name attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editAuthorizationName() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    AUTHORIZATION_NAME,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, AUTHORIZATION_NAME, value)
                                .verifyFormSaved()
                                .verifyAttribute(AUTHORIZATION_NAME, value);                  }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its extends
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editExtends() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();
        List<Address> removeLater = new ArrayList<>();

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    EXTENDS,
                    () -> {
                        Address extendsConfigurationAddress = null;

                        try {
                            extendsConfigurationAddress = createAuthenticationConfiguration();

                            page.navigate();
                            page.switchToAuthenticationConfiguration()
                                    .getResourceManager()
                                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
                            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                    .configFragment(page.getConfigFragment())
                                    .editAndSave(ConfigChecker.InputType.TEXT, EXTENDS, extendsConfigurationAddress.getLastPairValue())
                                    .verifyFormSaved()
                                    .verifyAttribute(EXTENDS, extendsConfigurationAddress.getLastPairValue());
                        } finally {
                            removeLater.add(extendsConfigurationAddress);
                        }
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            for (Address address : removeLater) {
                if (address != null) {
                    ops.removeIfExists(address);
                }
            }
        }

    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its host attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editHost() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    HOST,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, HOST, value)
                                .verifyFormSaved()
                                .verifyAttribute(HOST, value);
                    }
            );

        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * kerberos-security-factory attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editKerberosSecurityFactory() throws Exception {
        final Address
            authenticationConfigurationAddress = createAuthenticationConfiguration(),
            kerberosFactory = createKerberosFactory();

        final String kerberosFactoryName = kerberosFactory.getLastPairValue();

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    KERBEROS_SECURITY_FACTORY,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, KERBEROS_SECURITY_FACTORY, kerberosFactoryName)
                                .verifyFormSaved()
                                .verifyAttribute(KERBEROS_SECURITY_FACTORY, kerberosFactoryName);
                    }
            );

        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            ops.removeIfExists(kerberosFactory);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * mechanism-properties attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editMechanismProperties() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final Map<String, ModelNode> value = new HashMap<String, ModelNode>() {
            {
                put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphanumeric(7)));
                put(RandomStringUtils.randomAlphabetic(7), new ModelNode(RandomStringUtils.randomAlphanumeric(7)));
            }
        };

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    MECHANISM_PROPERTIES,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, MECHANISM_PROPERTIES, value.entrySet()
                                        .stream()
                                        .map(entry -> entry.getKey() + '=' + entry.getValue().asString())
                                        .collect(Collectors.joining("\n")))
                                .verifyFormSaved()
                                .verifyAttribute(MECHANISM_PROPERTIES,
                                        new ModelNodeGenerator().createObjectNodeWithPropertyChildren(value));
                    }
            );

        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its port attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editPort() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final int value = 9993;

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    PORT,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, PORT, String.valueOf(value))
                                .verifyFormSaved()
                                .verifyAttribute(PORT, value);
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its protocol
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editProtocol() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    PROTOCOL,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, PROTOCOL, value)
                                .verifyFormSaved()
                                .verifyAttribute(PROTOCOL, value);
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its realm attribute
     * value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editRealm() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    REALM,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, REALM, value)
                                .verifyFormSaved()
                                .verifyAttribute(REALM, value);
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * sasl-mechanism-selector attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editSaslMechanismSelector() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        final String value = RandomStringUtils.randomAlphabetic(7);

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    SASL_MECHANISM_SELECTOR,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                .configFragment(page.getConfigFragment())
                                .editAndSave(ConfigChecker.InputType.TEXT, SASL_MECHANISM_SELECTOR, value)
                                .verifyFormSaved()
                                .verifyAttribute(SASL_MECHANISM_SELECTOR, value);
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its security-domain
     * attribute value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editSecurityDomain() throws Exception {
        Address authenticationConfigurationAddress = createAuthenticationConfiguration();
        List<Address> removeLater = new ArrayList<>();

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    SECURITY_DOMAIN,
                    () -> {
                        final UndertowElytronOperations undertowElytronOperations = new UndertowElytronOperations(client);
                        final Address securityRealmAddress = undertowElytronOperations.createSecurityRealm(),
                                securityDomainAddress = undertowElytronOperations
                                        .createSecurityDomain(securityRealmAddress.getLastPairValue());
                        try {
                            page.navigate();
                            page.switchToAuthenticationConfiguration()
                                    .getResourceManager()
                                    .selectByName(authenticationConfigurationAddress.getLastPairValue());
                            new ConfigChecker.Builder(client, authenticationConfigurationAddress)
                                    .configFragment(page.getConfigFragment())
                                    .editAndSave(ConfigChecker.InputType.TEXT, SECURITY_DOMAIN, securityDomainAddress.getLastPairValue())
                                    .verifyFormSaved()
                                    .verifyAttribute(SECURITY_DOMAIN, securityDomainAddress.getLastPairValue());
                        } finally {
                            removeLater.add(securityDomainAddress);
                            removeLater.add(securityRealmAddress);
                        }
                    }
            );

        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
            for (Address address : removeLater) {
                if (address != null) {
                    ops.removeIfExists(address);
                }
            }
        }
    }

    /**
     * @tpTestDetails Create Elytron Authentication configuration instance in model and try to edit its
     * credential-reference value in Web Console's Elytron subsystem configuration.
     * Validate edited attribute value in the model.
     */
    @Test
    public void editCredentialReference() throws Exception {
        final Address authenticationConfigurationAddress = createAuthenticationConfiguration();

        try {
            elytronAuthenticationOperations.performActionOnAttributeAndRevertItsValueToOriginal(
                    authenticationConfigurationAddress,
                    CREDENTIAL_REFERENCE,
                    () -> {
                        page.navigate();
                        page.switchToAuthenticationConfiguration()
                                .getResourceManager()
                                .selectByName(authenticationConfigurationAddress.getLastPairValue());
                        page.getConfig().switchTo(CREDENTIAL_REFERENCE_LABEL);
                        ElytronIntegrationChecker credentialReferenceChecker = new ElytronIntegrationChecker.Builder(client)
                                .address(authenticationConfigurationAddress).configFragment(page.getConfigFragment()).build();
                        credentialReferenceChecker.setCredentialStoreCredentialReferenceAndVerify();
                        credentialReferenceChecker.testIllegalCombinationCredentialReferenceAttributes();
                        credentialReferenceChecker.setClearTextCredentialReferenceAndVerify();
                    }
            );
        } finally {
            ops.removeIfExists(authenticationConfigurationAddress);
        }
    }

    private Address createAuthenticationConfiguration() throws IOException {
        final Address address = elyOps.getElytronAddress(AUTHENTICATION_CONFIGURATION, RandomStringUtils.randomAlphanumeric(7));
        ops.add(address).assertSuccess();
        return address;
    }

    private Address createKerberosFactory() throws IOException {
        final String
                factoryName = "factoryName_" + RandomStringUtils.randomAlphanumeric(5),
                oid1 = "1.2.840.113554.1.2.2",
                oid2 = "1.3.6.1.5.5.2",
                pathValue = "path_" + RandomStringUtils.randomAlphanumeric(5),
                principalValue = "principal_" + RandomStringUtils.randomAlphanumeric(5);
        final Address factoryAddress = elyOps.getElytronAddress(KERBEROS_SECURITY_FACTORY, factoryName);
        final ModelNode oidsNode = new ModelNodeListBuilder().addNode(new ModelNode(oid1))
                .addNode(new ModelNode(oid2)).build();
        ops.add(factoryAddress,
                Values.of("mechanism-oids", oidsNode).and("path", pathValue).and("principal", principalValue))
                .assertSuccess();
        return factoryAddress;
    }

}
