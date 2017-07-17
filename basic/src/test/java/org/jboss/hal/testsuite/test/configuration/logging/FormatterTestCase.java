package org.jboss.hal.testsuite.test.configuration.logging;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.config.LoggingPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by pcyprian on 27.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class FormatterTestCase extends LoggingAbstractTestCase {

    private static final String FORMATTER_NAME = "Formatter_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String FORMATTER_TO_BE_REMOVED = "REMOVE_ME_PLS_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String FORMATTER_PATTERN = "%d{yyyy-MM-dd} %-5p [%c] (%t) %s%e%n";

    private static final Address FORMATTER_ADDRESS = LOGGING_SUBSYSTEM.and("pattern-formatter", FORMATTER_NAME);
    private static final Address FORMATTER_ADDRESS_TO_BE_REMOVED = LOGGING_SUBSYSTEM
            .and("pattern-formatter", FORMATTER_TO_BE_REMOVED);

    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException {
        operations.add(FORMATTER_ADDRESS, Values.empty().and("pattern", FORMATTER_PATTERN));
        operations.add(FORMATTER_ADDRESS_TO_BE_REMOVED, Values.empty().and("pattern", FORMATTER_PATTERN));
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, TimeoutException, IOException, OperationException {
        operations.removeIfExists(FORMATTER_ADDRESS);
        operations.removeIfExists(FORMATTER_ADDRESS_TO_BE_REMOVED);
    }

    @Drone
    private WebDriver browser;

    @Page
    private LoggingPage page;

    @Before
    public void before() {
        page.navigate();
        page.switchToFormatterTab();
        page.selectFormatter(FORMATTER_NAME);
    }

    @Test
    public void addFormatter() throws Exception {
        final Address formatterAddress = LOGGING_SUBSYSTEM.and("pattern-formatter",
                "Formatter_" + RandomStringUtils.randomAlphanumeric(5));
        try {
            page.addFormatter(formatterAddress.getLastPairValue());

            new ResourceVerifier(formatterAddress, client).verifyExists();
        } finally {
            operations.removeIfExists(formatterAddress);
        }
    }

    @Test
    public void updateFormatterPattern() throws Exception {
        String pattern = "%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n";
        editTextAndVerify(FORMATTER_ADDRESS, "pattern", pattern);
    }

    @Test
    public void updateFormatterColorMap() throws Exception {
        editTextAndVerify(FORMATTER_ADDRESS, "color-map", "fatal:black");
    }

    @Test
    public void removeFormatter() throws Exception {
        page.removeInTable(FORMATTER_TO_BE_REMOVED);

        administration.reloadIfRequired();

        new ResourceVerifier(FORMATTER_ADDRESS_TO_BE_REMOVED, client).verifyDoesNotExist();
    }
}
