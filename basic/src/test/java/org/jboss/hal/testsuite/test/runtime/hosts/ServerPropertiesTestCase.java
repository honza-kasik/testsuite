package org.jboss.hal.testsuite.test.runtime.hosts;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.fragment.ConfigAreaFragment;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
@Category(Domain.class)
public class ServerPropertiesTestCase extends PropertiesTestCaseAbstract {


    private static final Address SERVER_ADDRESS = Address.host(ConfigUtils.getDefaultHost()).and(SERVER_CONFIG, "server-one");

    @BeforeClass
    public static void beforeClass() {
        serverAddress = SERVER_ADDRESS;
    }

    @Override
    protected void navigate() {
        page.navigate();
        page.viewServerConfiguration(SERVER_ADDRESS.getLastPairValue());
        page.getConfig(ConfigAreaFragment.class).switchTo("System Properties");
    }
}
