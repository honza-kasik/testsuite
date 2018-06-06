package org.jboss.hal.testsuite.test.configuration.messaging.connections;


import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.page.config.MessagingPage;
import org.jboss.hal.testsuite.test.configuration.messaging.AbstractMessagingTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="padamec@redhat.com">Petr Adamec</a>
 */

@RunWith(Arquillian.class)
@Category(Standalone.class)
public class EditConnectionTestCase extends AbstractMessagingTestCase {
    private static final Address IN_VM_CONNECTOR_ADDRESS = DEFAULT_MESSAGING_SERVER.and("connection-factory", "InVmConnectionFactory");
    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();


    @Page
    private MessagingPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        client.apply(snapshotBackup.backup());
        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.viewConnectionSettings("default");
        page.switchToConnectionFactories();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        client.apply(snapshotBackup.restore());
        administration.reloadIfRequired();
    }

    /**
     * Test if value -1 can be set as connection TTL.</br>
     * @see <a href="https://issues.jboss.org/browse/HAL-1382">HAL-1382</a>
     *
     */
    @Test
    public void setConnectionTtlToMinusOne() throws Exception {
        page.selectInTable("InVmConnectionFactory");
        page.switchToConnectionManagementTab();
        editTextAndVerify(IN_VM_CONNECTOR_ADDRESS, "connectionTTL", "connection-ttl", -1L) ;
    }

    @After
    public void after() throws InterruptedException, TimeoutException, IOException {
        administration.reloadIfRequired();
    }

}
