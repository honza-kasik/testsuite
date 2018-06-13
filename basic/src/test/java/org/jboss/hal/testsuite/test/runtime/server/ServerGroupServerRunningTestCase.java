package org.jboss.hal.testsuite.test.runtime.server;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.creaper.command.UndeployCommand;
import org.jboss.hal.testsuite.page.runtime.DeploymentsPage;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *@author <a href="padamec@redhat.com">Petr Adamec</a>
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerGroupServerRunningTestCase {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration administration = new Administration(client);

    private static final String DEPLOYMENT_FILE_NAME = "test.war";
    private static final File DEPLOYMENT_FILE = new File("src/test/resources/" + DEPLOYMENT_FILE_NAME);

    @Drone
    private WebDriver browser;

    @Page
    private DeploymentsPage deploymentsPage;

    @Before
    public void before() throws CommandFailedException {
        createDeployment().as(ZipExporter.class).exportTo((DEPLOYMENT_FILE), true);

        DeployCommand.Builder deployBuilder = new DeployCommand.Builder(DEPLOYMENT_FILE);
        if (client.options().isDomain) {
            deployBuilder.toAllGroups();
        }
        client.apply(deployBuilder.name(DEPLOYMENT_FILE_NAME).build());
    }

    @After
    public void after() throws Exception {
        UndeployCommand.Builder undeployBuilder = new UndeployCommand.Builder(DEPLOYMENT_FILE_NAME);
        if (client.options().isDomain) {
            undeployBuilder.fromAllGroups();
        }
        client.apply(undeployBuilder.build());

        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws IOException, TimeoutException, InterruptedException {
        try {
            DEPLOYMENT_FILE.delete();
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    /**
     * Test if deployment units under tab ServerGroup are showing as server not running even though they are.
     * @see <a href="https://issues.jboss.org/browse/HAL-1473">HAL-1473</a>
     */
    @Test
    public void navigateToDeploymentAndInvokeView() {
        try {
            deploymentsPage.navigateToDeploymentAndInvokeView(DEPLOYMENT_FILE_NAME);
        } catch (org.openqa.selenium.TimeoutException e) {
            Assert.fail("Test fails probably due to HAL-1473");
        }
    }

    private WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_FILE_NAME);
        war.addClass(ServerGroupServerRunningServlet.class);
        return war;
    }


}
