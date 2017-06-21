package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.UndertowHTTPPage;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.undertow.AddUndertowListener;
import org.wildfly.extras.creaper.commands.undertow.SslVerifyClient;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.SELECT;
import static org.jboss.hal.testsuite.util.ConfigChecker.InputType.TEXT;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class HTTPSListenerTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowHTTPPage page;

    //identifiers
    private static final String
            ALLOW_ENCODED_SLASH = "allow-encoded-slash",
            ALLOW_EQUALS_IN_COOKIE_VALUE = "allow-equals-in-cookie-value",
            ALWAYS_SET_KEEP_ALIVE = "always-set-keep-alive",
            BUFFER_PIPELINED_DATA = "buffer-pipelined-data",
            BUFFER_POOL = "buffer-pool",
            DECODE_URL = "decode-url",
            ENABLE_HTTP2 = "enable-http2",
            ENABLED_CIPHER_SUITS = "enabled-cipher-suites",
            ENABLED_PROTOCOLS = "enabled-protocols",
            MAX_BUFFERED_REQUEST_SIZE = "max-buffered-request-size",
            MAX_CONNECTIONS = "max-connections",
            MAX_COOKIES = "max-cookies",
            MAX_HEADER_SIZE = "max-header-size",
            MAX_HEADERS = "max-headers",
            MAX_PARAMETERS = "max-parameters",
            MAX_POST_SIZE = "max-post-size",
            NO_REQUEST_TIMEOUT = "no-request-timeout",
            READ_TIMEOUT = "read-timeout",
            RECEIVE_BUFFER = "receive-buffer",
            RECORD_REQUEST_START_TIME = "record-request-start-time",
            REQUEST_PARSE_TIMEOUT = "request-parse-timeout",
            RESOLVE_PEER_ADDRESS = "resolve-peer-address",
            SECURITY_REALM = "security-realm",
            SSL_CONTEXT = "ssl-context",
            SEND_BUFFER = "send-buffer",
            SOCKET_BINDING = "socket-binding",
            SSL_SESSION_CACHE_SIZE = "ssl-session-cache-size",
            SSL_SESSION_TIMEOUT = "ssl-session-timeout",
            TCP_BACKLOG = "tcp-backlog",
            TCP_KEEP_ALIVE = "tcp-keep-alive",
            URL_CHARSET = "url-charset",
            VERIFY_CLIENT = "verify-client",
            WORKER = "worker",
            WRITE_TIMEOUT = "write-timeout";

    //values
    private static final String HTTP_SERVER = "undertow-http-server-https-listener_" + RandomStringUtils.randomAlphanumeric(5);

    private static final String HTTPS_LISTENER = "https-listener_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String HTTPS_LISTENER_TBR = "https-listener-btr_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String HTTPS_LISTENER_TBA = "https-listener-tba_" + RandomStringUtils.randomAlphanumeric(5);

    private static final Address HTTP_SERVER_ADDRESS = UNDERTOW_ADDRESS.and("server", HTTP_SERVER);

    private static final Address HTTPS_LISTENER_ADDRESS = HTTP_SERVER_ADDRESS.and("https-listener", HTTPS_LISTENER);
    private static final Address HTTPS_LISTENER_TBR_ADDRESS = HTTP_SERVER_ADDRESS.and("https-listener", HTTPS_LISTENER_TBR);
    private static final Address HTTPS_LISTENER_TBA_ADDRESS = HTTP_SERVER_ADDRESS.and("https-listener", HTTPS_LISTENER_TBA);

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, TimeoutException, InterruptedException, OperationException {
        operations.add(HTTP_SERVER_ADDRESS);
        administration.reloadIfRequired();
        client.apply(new AddUndertowListener.HttpsBuilder(HTTPS_LISTENER, HTTP_SERVER,
                        undertowOps.createSocketBinding())
                .securityRealm(undertowOps.createSecurityRealm())
                .verifyClient(SslVerifyClient.NOT_REQUESTED)
                .enabled(true)
                .build());
        client.apply(new AddUndertowListener.HttpsBuilder(HTTPS_LISTENER_TBR, HTTP_SERVER,
                        undertowOps.createSocketBinding())
                .securityRealm(undertowOps.createSecurityRealm())
                .verifyClient(SslVerifyClient.NOT_REQUESTED)
                .enabled(true)
                .build());
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.viewHTTPServer(HTTP_SERVER)
                .switchToHTTPSListeners()
                .selectItemInTableByText(HTTPS_LISTENER);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, CommandFailedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(HTTPS_LISTENER_ADDRESS);
        operations.removeIfExists(HTTPS_LISTENER_TBA_ADDRESS);
        operations.removeIfExists(HTTPS_LISTENER_TBR_ADDRESS);
        operations.removeIfExists(HTTP_SERVER_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void setAllowEncodedSlashToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ALLOW_ENCODED_SLASH, true);
    }

    @Test
    public void setAllowEncodedSlashToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ALLOW_ENCODED_SLASH, false);
    }

    @Test
    public void setAllowEqualsInCookieValueToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ALLOW_EQUALS_IN_COOKIE_VALUE, true);
    }

    @Test
    public void setAllowEqualsInCookieValueToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ALLOW_EQUALS_IN_COOKIE_VALUE, false);
    }

    @Test
    public void setAlwaysSetKeepAliveToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ALWAYS_SET_KEEP_ALIVE, true);
    }

    @Test
    public void setAlwaysSetKeepAliveToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ALWAYS_SET_KEEP_ALIVE, false);
    }

    @Test
    public void setBufferPipelinedDataToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, BUFFER_PIPELINED_DATA, true);
    }

    @Test
    public void setBufferPipelinedDataToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, BUFFER_PIPELINED_DATA, false);
    }

    @Test
    public void editBufferPool() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, BUFFER_POOL, undertowOps.createBufferPool());
    }

    @Test
    public void setDecodeURLToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, DECODE_URL, true);
    }

    @Test
    public void setDecodeURLToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, DECODE_URL, false);
    }

    @Test
    public void setEnableHTTP2ToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ENABLE_HTTP2, true);
    }

    @Test
    public void setEnableHTTP2ToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, ENABLE_HTTP2, false);
    }

    @Test
    public void editMaxBufferedRequestSize() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_BUFFERED_REQUEST_SIZE, NUMERIC_VALID);
    }

    @Test
    public void editMaxBufferedRequestSizeInvalid() {
        verifyIfErrorAppears(MAX_BUFFERED_REQUEST_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editMaxConnections() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_CONNECTIONS, NUMERIC_VALID);
    }

    @Test
    public void editMaxConnectionsInvalid() {
        verifyIfErrorAppears(MAX_CONNECTIONS, NUMERIC_INVALID);
    }

    @Test
    public void editMaxCookies() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_COOKIES, NUMERIC_VALID);
    }

    @Test
    public void editMaxCookiesInvalid() {
        verifyIfErrorAppears(MAX_COOKIES, NUMERIC_INVALID);
    }

    @Test
    public void editMaxHeaderSize() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_HEADER_SIZE, NUMERIC_VALID);
    }

    @Test
    public void editMaxHeaderSizeInvalid() {
        verifyIfErrorAppears(MAX_HEADER_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editMaxHeaders() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_HEADERS, NUMERIC_VALID);
    }

    @Test
    public void editMaxHeadersInvalid() {
        verifyIfErrorAppears(MAX_HEADERS, NUMERIC_INVALID);
    }

    @Test
    public void editMaxParameters() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_PARAMETERS, NUMERIC_VALID);
    }

    @Test
    public void editMaxParametersInvalid() {
        verifyIfErrorAppears(MAX_PARAMETERS, NUMERIC_INVALID);
    }

    @Test
    public void editMaxPostSize() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, MAX_POST_SIZE, NUMERIC_VALID_LONG);
    }

    @Test
    public void editMaxPostSizeInvalid() {
        verifyIfErrorAppears(MAX_POST_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editNoRequestTimeout() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, NO_REQUEST_TIMEOUT, NUMERIC_VALID);
    }

    @Test
    public void editNoRequestInvalid() {
        verifyIfErrorAppears(NO_REQUEST_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void editReadTimeout() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, READ_TIMEOUT, NUMERIC_VALID);
    }

    @Test
    public void editReadTimeoutInvalid() throws Exception {
        verifyIfErrorAppears(READ_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void editReceiveBuffer() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, RECEIVE_BUFFER, NUMERIC_VALID);
    }

    @Test
    public void editReceiveBufferInvalid() {
        verifyIfErrorAppears(RECEIVE_BUFFER, NUMERIC_INVALID);
    }

    @Test
    public void setRecordRequestStartTimeToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, RECORD_REQUEST_START_TIME, true);
    }

    @Test
    public void setRecordRequestStartTimeToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, RECORD_REQUEST_START_TIME, false);
    }

    @Test
    public void editRequestParseTimeout() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, REQUEST_PARSE_TIMEOUT, NUMERIC_VALID);
    }

    @Test
    public void editRequestParseTimeoutInvalid() throws Exception {
        verifyIfErrorAppears(REQUEST_PARSE_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void setResolvePeerAddressToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, RESOLVE_PEER_ADDRESS, true);
    }

    @Test
    public void setResolvePeerAddressToFalse() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, RESOLVE_PEER_ADDRESS, false);
    }

    @Test
    public void editSendBuffer() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, SEND_BUFFER, NUMERIC_VALID);
    }

    @Test
    public void editSendBufferInvalid() throws Exception {
        verifyIfErrorAppears(SEND_BUFFER, NUMERIC_INVALID);
    }

    @Test
    public void editSocketBinding() throws Exception {
        String socketBinding = undertowOps.createSocketBinding();
        ModelNode originalSocketBindingValue = operations.readAttribute(HTTPS_LISTENER_ADDRESS, SOCKET_BINDING,
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS).value();
        try {
            new ConfigChecker.Builder(client, HTTPS_LISTENER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .editAndSave(TEXT, SOCKET_BINDING, socketBinding)
                    .verifyFormSaved()
                    .verifyAttribute(SOCKET_BINDING, socketBinding);
        } finally {
            operations.writeAttribute(HTTPS_LISTENER_ADDRESS, SOCKET_BINDING, originalSocketBindingValue);
        }
    }

    @Test
    public void editSSLContextSecurityRealmInvalidCombination() throws Exception {
        final UndertowElytronOperations undertowElytronOperations = new UndertowElytronOperations(client);
        final Address keyStoreAddress = undertowElytronOperations.createKeyStore(),
                keyManagerAddress = undertowElytronOperations.createKeyManager(keyStoreAddress),
                serverSSLContextAddress = undertowElytronOperations.createServerSSLContext(keyManagerAddress);

        final ModelNodeResult sslContextModelNodeResult = operations.readAttribute(HTTPS_LISTENER_ADDRESS, SSL_CONTEXT),
                securityRealmModelNodeResult = operations.readAttribute(HTTPS_LISTENER_ADDRESS, SECURITY_REALM);
        sslContextModelNodeResult.assertSuccess();
        securityRealmModelNodeResult.assertSuccess();

        try {
            new ConfigChecker.Builder(client, HTTPS_LISTENER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, SSL_CONTEXT, serverSSLContextAddress.getLastPairValue())
                    .edit(TEXT, SECURITY_REALM, RandomStringUtils.randomAlphanumeric(7))
                    .andSave()
                    .verifyFormNotSaved("Probably fails because of https://issues.jboss.org/browse/HAL-1340");
        } finally {
            operations.batch(new Batch()
                    .writeAttribute(HTTPS_LISTENER_ADDRESS, SSL_CONTEXT, sslContextModelNodeResult.value())
                    .writeAttribute(HTTPS_LISTENER_ADDRESS, SECURITY_REALM, securityRealmModelNodeResult.value()))
                    .assertSuccess();
            operations.removeIfExists(serverSSLContextAddress);
            operations.removeIfExists(keyManagerAddress);
            operations.removeIfExists(keyStoreAddress);
        }
    }

    @Test
    public void editSSLContext() throws Exception {
        final UndertowElytronOperations undertowElytronOperations = new UndertowElytronOperations(client);
        final Address keyStoreAddress = undertowElytronOperations.createKeyStore(),
                keyManagerAddress = undertowElytronOperations.createKeyManager(keyStoreAddress),
                serverSSLContextAddress = undertowElytronOperations.createServerSSLContext(keyManagerAddress);

        final Map<String, ConfigChecker.InputType> alternatives = new HashMap<>();
        alternatives.put(SECURITY_REALM, TEXT);
        alternatives.put(VERIFY_CLIENT, SELECT);
        alternatives.put(ENABLED_CIPHER_SUITS, TEXT);
        alternatives.put(ENABLED_PROTOCOLS, TEXT);
        alternatives.put(SSL_SESSION_CACHE_SIZE, TEXT);
        alternatives.put(SSL_SESSION_TIMEOUT, TEXT);

        final Map<String, ModelNodeResult> backedUpValues = new HashMap<>();

        for (String alternative : alternatives.keySet()) {
            ModelNodeResult modelNodeResult = operations.readAttribute(HTTPS_LISTENER_ADDRESS, alternative);
            modelNodeResult.assertSuccess();
            backedUpValues.put(alternative, modelNodeResult);
        }

        final ModelNodeResult sslContextModelNodeResult = operations.readAttribute(HTTPS_LISTENER_ADDRESS, SSL_CONTEXT);
        sslContextModelNodeResult.assertSuccess();

        try {
            final ConfigChecker.Builder builder = new ConfigChecker.Builder(client, HTTPS_LISTENER_ADDRESS)
                    .configFragment(page.getConfigFragment())
                    .edit(TEXT, SSL_CONTEXT, serverSSLContextAddress.getLastPairValue());

            alternatives.forEach((alternativeName, type) -> builder.edit(type, alternativeName, ""));

            builder.andSave()
                    .verifyFormSaved()
                    .verifyAttribute(SSL_CONTEXT, serverSSLContextAddress.getLastPairValue());
        } finally {
            final Batch batch = new Batch()
                    .writeAttribute(HTTPS_LISTENER_ADDRESS, SSL_CONTEXT, sslContextModelNodeResult.value());
            backedUpValues.forEach((key, value) -> batch.writeAttribute(HTTPS_LISTENER_ADDRESS, key, value.value()));
            operations.batch(batch).assertSuccess();
            operations.removeIfExists(serverSSLContextAddress);
            operations.removeIfExists(keyManagerAddress);
            operations.removeIfExists(keyStoreAddress);
        }
    }

    @Test
    public void editSSLSessionCacheSize() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, SSL_SESSION_CACHE_SIZE, NUMERIC_VALID);
    }

    @Test
    public void editSSLSessionCacheSizeInvalid() throws Exception {
        verifyIfErrorAppears(SSL_SESSION_CACHE_SIZE, NUMERIC_INVALID);
    }

    @Test
    public void editSSLSessionTimeout() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, SSL_SESSION_TIMEOUT, NUMERIC_VALID);
    }

    @Test
    public void editSSLSessionTimeoutInvalid() throws Exception {
        verifyIfErrorAppears(SSL_SESSION_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void editTCPBacklog() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, TCP_BACKLOG, NUMERIC_VALID);
    }

    @Test
    public void editTCPBacklogInvalid() {
        verifyIfErrorAppears(TCP_BACKLOG, NUMERIC_INVALID);
    }

    @Test
    public void setTCPKeepAliveToTrue() throws Exception {
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, TCP_KEEP_ALIVE, true);
    }

    @Test
    public void setTCPKeepAliveToFalse() throws Exception {
        operations.writeAttribute(HTTPS_LISTENER_ADDRESS, TCP_KEEP_ALIVE, true);
        administration.reloadIfRequired();
        page.navigate();
        page.viewHTTPServer(HTTP_SERVER)
                .switchToHTTPSListeners()
                .selectItemInTableByText(HTTPS_LISTENER);
        editCheckboxAndVerify(HTTPS_LISTENER_ADDRESS, TCP_KEEP_ALIVE, false);
    }

    @Test
    public void editURLCharset() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, URL_CHARSET);
    }

    @Test
    public void editWorker() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, WORKER, undertowOps.createWorker());
    }

    @Test
    public void editWriteTimeout() throws Exception {
        editTextAndVerify(HTTPS_LISTENER_ADDRESS, WRITE_TIMEOUT, NUMERIC_VALID);
    }

    @Test
    public void editWriteTimeoutInvalid() throws Exception {
        verifyIfErrorAppears(WRITE_TIMEOUT, NUMERIC_INVALID);
    }

    @Test
    public void addHTTPSListenerInGUI() throws Exception {
        String socketBinding = undertowOps.createSocketBinding();
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", HTTPS_LISTENER_TBA);
        editor.text(SOCKET_BINDING, socketBinding);
        boolean result = wizard.finish();

        Assert.assertTrue("Window should be closed", result);
        Assert.assertTrue("HTTPS listener should be present in table", page.getResourceManager().isResourcePresent(HTTPS_LISTENER_TBA));
        ResourceVerifier verifier = new ResourceVerifier(HTTPS_LISTENER_TBA_ADDRESS, client);
        verifier.verifyAttribute(SOCKET_BINDING, socketBinding);
}

    @Test
    public void removeHTTPSListenerInGUI() throws Exception {
        page.getResourceManager()
                .removeResource(HTTPS_LISTENER_TBR)
                .confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse("HTTPS listener host should not be present in table", page.getResourceManager().isResourcePresent(HTTPS_LISTENER_TBR));
        new ResourceVerifier(HTTPS_LISTENER_TBR_ADDRESS, client).verifyDoesNotExist(); //HTTP server host should not be present on the server
    }

}
