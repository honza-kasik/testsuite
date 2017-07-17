package org.jboss.hal.testsuite.test.configuration.web;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.AddSocketBinding;
import org.jboss.hal.testsuite.page.config.ModClusterPage;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.AvailablePortFinder;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;

import static org.jboss.hal.testsuite.cli.CliConstants.MOD_CLUSTER_CONFIG_ADDRESS;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ModClusterTestCase {

    private static final Logger log = LoggerFactory.getLogger(ModClusterTestCase.class);

    private static final String SESSIONS = "Sessions";
    private static final String WEB_CONTEXTS = "Web Contexts";
    private static final String PROXIES = "Proxies";
    private static final String NETWORKING = "Networking";
    private static final String LOAD_BALANCING_GROUP_ID = "loadBalancingGroup";
    private static final String BALANCER_ID = "balancer";
    private static final String ADVERTISE_SOCKET_ID = "advertiseSocket";
    private static final String ADVERTISE_KEY_ID = "advertiseKey";
    private static final String ADVERTISE_ID = "advertise";
    private static final String STICKY_SESSION_ID = "stickySession";
    private static final String STICKY_SESSION_FORCE_ID = "stickySessionForce";
    private static final String STICKY_SESSION_REMOVE_ID = "stickySessionRemove";
    private static final String AUTO_ENABLE_CONTEXTS_ID = "autoEnableContexts";
    private static final String EXCLUDED_CONTEXTS_ID = "excludedContexts";
    private static final String PROXY_URL_ID = "proxyUrl";
    private static final String PROXY_LIST_ID = "proxyList";
    private static final String NODE_TIMEOUT_ID = "nodeTimeout";
    private static final String SOCKET_TIMEOUT_ID = "socketTimeout";
    private static final String STOP_CONTEXT_TIMEOUT_ID = "stopContextTimeout";
    private static final String MAX_ATTEMPTS_ID = "maxAttemps";
    private static final String FLUSH_PACKETS_ID = "flushPackets";
    private static final String FLUSH_WAIT_ID = "flushWait";
    private static final String PING_ID = "ping";
    private static final String TTL_ID = "ttl";
    private static final String WORKER_TIMEOUT_ID = "workerTimeout";

    private static final String EXCLUDED_CONTEXTS_VALUE = "ROOT";
    private static final String LOAD_BALANCING_GROUP_VALUE = "lbg_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String BALANCER_VALUE = "b_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADVERTISE_SOCKET_VALUE = "lbg_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String ADVERTISE_KEY_VALUE = "lbg_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String PROXY_URL_VALUE = "/dd";
    private static final String PROXY_LIST_VALUE = "dd";
    private static final String NODE_TIMEOUT_VALUE = "50";
    private static final String SOCKET_TIMEOUT_VALUE = "52";
    private static final String STOP_CONTEXT_TIMEOUT_VALUE = "51";
    private static final String MAX_ATTEMPTS_VALUE = "58";
    private static final String FLUSH_WAIT_VALUE = "44";
    private static final String PING_VALUE = "46";
    private static final String TTL_VALUE = "42";
    private static final String WORKER_TIMEOUT_VALUE = "41";
    private static final String NUMERIC_VALUE_NEGATIVE = "-50";
    private static final String NUMERIC_VALUE_INVALID = "50" + RandomStringUtils.randomAlphabetic(3);

    private static final OnlineManagementClient managementClient = ManagementClientProvider.createOnlineManagementClient();
    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(MOD_CLUSTER_CONFIG_ADDRESS, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();

    @Drone
    public WebDriver browser;

    @Page
    public ModClusterPage page;

    @BeforeClass
    public static void beforeClass() throws IOException, CommandFailedException {
        int port = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
        int multicastPort = AvailablePortFinder.getNextAvailableNonPrivilegedPort();
        log.info("Obtained port for socket binding '" + ADVERTISE_SOCKET_VALUE + "' is " + port +
                ", multicast port is " + multicastPort);
        managementClient.apply(new AddSocketBinding.Builder(ADVERTISE_SOCKET_VALUE)
                .port(port)
                .multicastAddress("224.0.0.1")
                .multicastPort(multicastPort)
                .build());
        managementClient.apply(snapshotBackup.backup());
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException, IOException {
        try {
            managementClient.apply(snapshotBackup.restore());
        } finally {
            managementClient.close();
        }
    }

    @Before
    public void before() {
        page.navigate();
    }

    @After
    public void after() {
        client.reload(false);
    }

    /*
     * ADVERTISING
     */
    @Test
    public void loadBalancingGroup() {
        checker.editTextAndAssert(page, LOAD_BALANCING_GROUP_ID, LOAD_BALANCING_GROUP_VALUE).invoke();
    }

    @Test
    public void balancer() {
        checker.editTextAndAssert(page, BALANCER_ID, BALANCER_VALUE).invoke();
    }

    @Test
    public void advertiseSocket() {
        checker.editTextAndAssert(page, ADVERTISE_SOCKET_ID, ADVERTISE_SOCKET_VALUE).invoke();
    }

    @Test
    public void advertiseKey() {
        checker.editTextAndAssert(page, ADVERTISE_KEY_ID, ADVERTISE_KEY_VALUE).dmrAttribute("advertise-security-key").invoke();
    }

    @Test
    public void advertise() {
        checker.editCheckboxAndAssert(page, ADVERTISE_ID, false).invoke();
        checker.editCheckboxAndAssert(page, ADVERTISE_ID, true).invoke();
    }

    /*
     * SESSIONS
     */
    @Test
    public void stickySession() {
        checker.editCheckboxAndAssert(page, STICKY_SESSION_ID, false).tab(SESSIONS).invoke();
        checker.editCheckboxAndAssert(page, STICKY_SESSION_ID, true).tab(SESSIONS).invoke();
    }

    @Test
    public void stickySessionRemove() {
        checker.editCheckboxAndAssert(page, STICKY_SESSION_REMOVE_ID, false).tab(SESSIONS).invoke();
        checker.editCheckboxAndAssert(page, STICKY_SESSION_REMOVE_ID, true).tab(SESSIONS).invoke();
    }

    @Test
    public void stickySessionForce() {
        checker.editCheckboxAndAssert(page, STICKY_SESSION_FORCE_ID, false).tab(SESSIONS).invoke();
        checker.editCheckboxAndAssert(page, STICKY_SESSION_FORCE_ID, true).tab(SESSIONS).invoke();
    }

    /*
     * WEB CONTEXTS
     */
    @Test
    public void autoEnableContexts() {
        checker.editCheckboxAndAssert(page, AUTO_ENABLE_CONTEXTS_ID, false).tab(WEB_CONTEXTS).invoke();
        checker.editCheckboxAndAssert(page, AUTO_ENABLE_CONTEXTS_ID, true).tab(WEB_CONTEXTS).invoke();
    }

    @Test
    public void excludedContexts() {
        checker.editTextAndAssert(page, EXCLUDED_CONTEXTS_ID, EXCLUDED_CONTEXTS_VALUE).tab(WEB_CONTEXTS).invoke();
    }

    /*
     * PROXIES
     */
    @Test
    public void proxyUrl() {
        checker.editTextAndAssert(page, PROXY_URL_ID, PROXY_URL_VALUE).tab(PROXIES).invoke();
    }

    @Test
    @Ignore("Unknown error appears after setting proxy list")
    public void proxyList() {
        checker.editTextAndAssert(page, PROXY_LIST_ID, PROXY_LIST_VALUE).tab(PROXIES).invoke();
    }

    /*
     * NETWORKING
     */
    @Test
    public void nodeTimeout() {
        checker.editTextAndAssert(page, NODE_TIMEOUT_ID, NODE_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void nodeTimeoutNegative() {
        checker.editTextAndAssert(page, NODE_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void nodeTimeoutInvalid() {
        checker.editTextAndAssert(page, NODE_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void socketTimeout() {
        checker.editTextAndAssert(page, SOCKET_TIMEOUT_ID, SOCKET_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void socketTimeoutNegative() {
        checker.editTextAndAssert(page, SOCKET_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void socketTimeoutInvalid() {
        checker.editTextAndAssert(page, SOCKET_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void stopContextTimeout() {
        checker.editTextAndAssert(page, STOP_CONTEXT_TIMEOUT_ID, STOP_CONTEXT_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void stopContextTimeoutNegative() {
        checker.editTextAndAssert(page, STOP_CONTEXT_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void stopContextTimeoutInvalid() {
        checker.editTextAndAssert(page, STOP_CONTEXT_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void maxAttempts() {
        checker.editTextAndAssert(page, MAX_ATTEMPTS_ID, MAX_ATTEMPTS_VALUE).tab(NETWORKING).dmrAttribute("max-attempts").invoke();
    }

    @Test
    public void maxAttemptsNegative() {
        checker.editTextAndAssert(page, MAX_ATTEMPTS_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void maxAttemptsInvalid() {
        checker.editTextAndAssert(page, MAX_ATTEMPTS_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void flushPackets() {
        checker.editCheckboxAndAssert(page, FLUSH_PACKETS_ID, false).tab(NETWORKING).invoke();
        checker.editCheckboxAndAssert(page, FLUSH_PACKETS_ID, true).tab(NETWORKING).invoke();
    }

    @Test
    public void flushWait() {
        checker.editTextAndAssert(page, FLUSH_WAIT_ID, FLUSH_WAIT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void flushWaitNegative() {
        checker.editTextAndAssert(page, FLUSH_WAIT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void flushWaitInvalid() {
        checker.editTextAndAssert(page, FLUSH_WAIT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void ping() {
        checker.editTextAndAssert(page, PING_ID, PING_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void pingNegative() {
        checker.editTextAndAssert(page, PING_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void pingInvalid() {
        checker.editTextAndAssert(page, PING_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void ttl() {
        checker.editTextAndAssert(page, TTL_ID, TTL_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void ttlNegative() {
        checker.editTextAndAssert(page, TTL_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void ttlInvalid() {
        checker.editTextAndAssert(page, TTL_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void workerTimeout() {
        checker.editTextAndAssert(page, WORKER_TIMEOUT_ID, WORKER_TIMEOUT_VALUE).tab(NETWORKING).invoke();
    }

    @Test
    public void workerTimeoutNegative() {
        checker.editTextAndAssert(page, WORKER_TIMEOUT_ID, NUMERIC_VALUE_NEGATIVE).tab(NETWORKING).expectError().invoke();
    }

    @Test
    public void workerTimeoutInvalid() {
        checker.editTextAndAssert(page, WORKER_TIMEOUT_ID, NUMERIC_VALUE_INVALID).tab(NETWORKING).expectError().invoke();
    }
}
