package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.DefaultContext;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.dmr.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.StatementContext;
import org.jboss.hal.testsuite.fragment.config.undertow.UndertowFragment;
import org.jboss.hal.testsuite.page.config.UndertowPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
public abstract class UndertowTestCaseAbstract {

    protected static Dispatcher dispatcher;
    protected static ResourceVerifier verifier;
    protected static UndertowOperations operations;
    protected static StatementContext context = new DefaultContext();
    protected static AddressTemplate undertowAddressTemplate = AddressTemplate.of("{default.profile}/subsystem=undertow");
    protected static AddressTemplate httpServerTemplate = undertowAddressTemplate.append("/server=*");
    protected static AddressTemplate servletContainerTemplate = undertowAddressTemplate.append("/servlet-container=*");

    //shared valid values
    protected static String WORKER_VALUE_VALID;
    protected static String BUFFER_POOL_VALUE_VALID;
    protected static String SOCKET_BINDING_VALUE_VALID;
    protected String NUMERIC_VALID = "25";
    protected String NUMERIC_INVALID = "25dfs";
    protected String BUFFER_CACHE_VALUE_VALID = "";

    @Drone
    protected WebDriver browser;

    @Page
    protected UndertowPage page;

    @BeforeClass
    public static void mainSetUp() {
        dispatcher = new Dispatcher();
        verifier = new ResourceVerifier(dispatcher);
        operations = new UndertowOperations(dispatcher);
        WORKER_VALUE_VALID = operations.createWorker();
        BUFFER_POOL_VALUE_VALID = operations.createBufferPool();
        SOCKET_BINDING_VALUE_VALID = operations.createSocketBinding();
    }

    @AfterClass
    public static void mainTearDown() {
        operations.removeWorker(WORKER_VALUE_VALID);
        operations.removeBufferPool(BUFFER_POOL_VALUE_VALID);
        dispatcher.close();
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().editTextAndSave(identifier, value);
        UndertowOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    protected void editTextAndVerify(ResourceAddress address, String identifier, String attributeName) throws IOException, InterruptedException {
        editTextAndVerify(address, identifier, attributeName, RandomStringUtils.randomAlphabetic(6));
    }

    protected void editCheckboxAndVerify(ResourceAddress address, String identifier, String attributeName, Boolean value) throws IOException, InterruptedException {
        page.getConfigFragment().editCheckboxAndSave(identifier, value);
        UndertowOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value.toString());
    }

    public void selectOptionAndVerify(ResourceAddress address, String identifier, String attributeName, String value) throws IOException, InterruptedException {
        page.getConfigFragment().selectOptionAndSave(identifier, value);
        UndertowOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, value);
    }

    public void editTextAreaAndVerify(ResourceAddress address, String identifier, String attributeName, String[] values) throws IOException, InterruptedException {
        page.getConfigFragment().editTextAndSave(identifier, String.join("\n", values));
        UndertowOperations.reloadIfRequiredAndWaitForRunning();
        verifier.verifyAttribute(address, attributeName, values);
    }

    protected void verifyIfErrorAppears(String identifier, String value) {
        UndertowFragment config = page.getConfigFragment();
        config.editTextAndSave(identifier, value);
        Assert.assertTrue(config.isErrorShowedInForm());
        config.cancel();
    }



}
