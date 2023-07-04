package testngcore;

import com.tidal.flow.assertions.stackbuilder.ErrorStack;
import com.tidal.wave.browser.Browser;
import com.tidal.wave.browser.Driver;
import com.tidal.wave.options.BrowserWithOptions;
import com.tidal.wave.propertieshandler.Config;
import com.tidal.wave.propertieshandler.PropertiesFinder;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Arrays;

import static com.tidal.wave.browser.Browser.close;
import static com.tidal.wave.utils.CheckString.isNullOrEmpty;


public class TestListener implements ITestListener {


    @Override
    public void onTestStart(ITestResult result) {
        if (isUiTest(result)) {
            String browser = Config.BROWSER_NAME;

            //Wait Duration
            Duration duration = Duration.ofSeconds(4);

            AbstractDriverOptions<?> options = null;

            //Set the options corresponding to remote and local runs
            String executionType = Config.EXECUTION_TYPE;
            if (executionType.equalsIgnoreCase("local")) {
                options = setLocalOptions(browser);
                duration = Duration.ofSeconds(Config.LOCAL_TIMEOUT);
            } else if (executionType.equalsIgnoreCase("docker") || executionType.equalsIgnoreCase("remote")) {
                options = setRemoteOptions(browser);
                duration = Duration.ofSeconds(Config.REMOTE_TIMEOUT);
            }

            //Option to complete the initial setting without setting up a browser session
            if (!isNullOrEmpty(Config.BASE_URL)) {
                Browser
                        .withOptions(options)
                        .type(browser)
                        .withWaitTime(duration)
                        .open(PropertiesFinder.getProperty("base.url"));
            }

        }

    }


    @Override
    public void onTestFailure(ITestResult result) {
        try {
            if (isUiTest(result)) {
                Allure.addAttachment(result.getName(), "image/png", new ByteArrayInputStream(getScreenshot()), ".png");
                close();
            }

        } finally {
            new ErrorStack().execute();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        try {
            if (isUiTest(result))
                close();

        } finally {
            new ErrorStack().execute();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            if (isUiTest(result))
                close();
        } finally {
            new ErrorStack().execute();
        }
    }



    @Override
    public void onFinish(ITestContext context) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    private AbstractDriverOptions<?> setLocalOptions(String browserType) {
        return new BrowserWithOptions().getLocalOptions(browserType);
    }

    private AbstractDriverOptions<?> setRemoteOptions(String browserType) {
        return new BrowserWithOptions().getRemoteOptions(browserType);
    }


    public boolean isUiTest(ITestResult result) {
        return Arrays.stream(result.getMethod().getGroups())
                .noneMatch(group -> group.contains("apiTest")||group.contains("dbTest"));
    }

    private byte[] getScreenshot() {

        return ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
    }



}
