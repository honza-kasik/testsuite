package org.jboss.hal.testsuite.test.configuration.transactions;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.page.config.TransactionsPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class TransactionsTestCase extends TransactionsTestCaseAbstract {

    private final String DEFAULT_TIMEOUT = "default-timeout";
    private final String ENABLE_TSM_STATUS = "enable-tsm-status";
    private final String JTS = "jts";
    private final String NODE_IDENTIFIER = "node-identifier";
    private final String STATISTICS_ENABLED = "statistics-enabled";
    private final String SOCKET_BINDING = "socket-binding";
    private final String STATUS_SOCKET_BINDING = "status-socket-binding";
    private final String RECOVERY_LISTENER = "recovery-listener";
    private final String OBJECT_STORE_PATH = "object-store-path";
    private final String OBJECT_STORE_RELATIVE_TO = "object-store-relative-to";

    private final String DEFAULT_TIMEOUT_ATTR = "default-timeout";
    private final String ENABLE_TSM_STATUS_ATTR = "enable-tsm-status";
    private final String JTS_ATTR = "jts";
    private final String NODE_IDENTIFIER_ATTR = "node-identifier";
    private final String STATISTICS_ENABLED_ATTR = "statistics-enabled";
    private final String SOCKET_BINDING_ATTR = "socket-binding";
    private final String STATUS_SOCKET_BINDING_ATTR = "status-socket-binding";
    private final String RECOVERY_LISTENER_ATTR = "recovery-listener";
    private final String OBJECT_STORE_PATH_ATTR = "object-store-path";
    private final String OBJECT_STORE_RELATIVE_TO_ATTR = "object-store-relative-to";

    @Drone
    public WebDriver browser;

    @Page
    public TransactionsPage page;

    @Before
    public void before() {
        page.navigate();
    }

    @Test
    public void setStatisticsToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, STATISTICS_ENABLED, STATISTICS_ENABLED_ATTR, true);
    }

    @Test
    public void setStatisticsToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, STATISTICS_ENABLED, STATISTICS_ENABLED_ATTR, true);
    }

    @Test
    public void setJTSToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, JTS, JTS_ATTR, true);
    }

    @Test
    public void setJTSToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, JTS, JTS_ATTR, false);
    }

    @Test
    public void setTSMStatusToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, ENABLE_TSM_STATUS, ENABLE_TSM_STATUS_ATTR, true);
    }

    @Test
    public void setTSMStatusToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, ENABLE_TSM_STATUS, ENABLE_TSM_STATUS_ATTR, false);
    }

    @Test
    public void editNodeIdentifier() throws IOException, InterruptedException {
        editTextAndVerify(address, NODE_IDENTIFIER, NODE_IDENTIFIER_ATTR);
    }

    @Test
    public void editDefaultTimeout() throws IOException, InterruptedException {
        editTextAndVerify(address, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_ATTR, "500");
    }

    @Test
    public void editDefaultTimeoutNegative() {
        verifyIfErrorAppears(DEFAULT_TIMEOUT, "-500");
    }
    @Test
    public void editDefaultTimeoutInvalid() {
        verifyIfErrorAppears(DEFAULT_TIMEOUT, "asdf");
    }



    @Test
    public void editSocketBinding() throws IOException, InterruptedException {
        String socketBinding = operations.createSocketBinding();
        page.getConfig().switchTo("Recovery");
        editTextAndVerify(address, SOCKET_BINDING, SOCKET_BINDING_ATTR, socketBinding);
    }

    @Test
    public void editStatusSocketBinding() throws IOException, InterruptedException {
        String socketBinding = operations.createSocketBinding();
        page.getConfig().switchTo("Recovery");
        editTextAndVerify(address, STATUS_SOCKET_BINDING, STATUS_SOCKET_BINDING_ATTR, socketBinding);
    }

    @Test
    public void setRecoveryListenerToTrue() throws IOException, InterruptedException {
        page.getConfig().switchTo("Recovery");
        editCheckboxAndVerify(address, RECOVERY_LISTENER, RECOVERY_LISTENER_ATTR, true);
    }

    @Test
    public void setRecoveryListenerToFalse() throws IOException, InterruptedException {
        page.getConfig().switchTo("Recovery");
        editCheckboxAndVerify(address, RECOVERY_LISTENER, RECOVERY_LISTENER_ATTR, false);
    }

    @Test
    public void editObjectStorePath() throws IOException, InterruptedException {
        page.getConfig().switchTo("Path");
        editTextAndVerify(address, OBJECT_STORE_PATH, OBJECT_STORE_PATH_ATTR);
    }

    @Test
    public void editObjectStoreRelativeTo() throws IOException, InterruptedException {
        page.getConfig().switchTo("Path");
        editTextAndVerify(address, OBJECT_STORE_RELATIVE_TO, OBJECT_STORE_RELATIVE_TO_ATTR);
    }





}
