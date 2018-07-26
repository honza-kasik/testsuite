package org.jboss.hal.testsuite.test.configuration.general;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.fragment.WindowFragment;
import org.jboss.hal.testsuite.page.home.HomePage;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:padamec@redhat.com">Petr Adamec</a>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class TakeTourTestCase {

    @Drone
    public WebDriver browser;

    @Page
    public HomePage page;

    @Before
    public void setupWindow() {
        Console.withBrowser(browser).refreshAndNavigate(HomePage.class);
    }

    /**
     * Test if Take a tour window contain English text <i>Enable, disable, and view the<i/>
     * https://issues.jboss.org/browse/HAL-1490
     */
    @Test
    public void testEnglishLanguage() {
        WebElement webElement = browser.findElement(By.linkText("Take a Tour!"));
        webElement.click();
        WindowFragment windowFragment = Console.withBrowser(browser).openedWindow();
        if (windowFragment != null) {
            WebElement iFrame = browser.findElement(ByJQuery.selector("iframe.gwt-Frame"));
            WebElement content = browser.switchTo().frame(iFrame).findElement(By.tagName("body"));
            String text = content.findElement(By.className("text-1")).getText();
            Assert.assertTrue("Take a tour window doesn't contain English text (Enable, disable, and view the ...). Probably due to https://issues.jboss.org/browse/HAL-1490", text.contains("Enable, disable, and view the"));
        }
    }
}
