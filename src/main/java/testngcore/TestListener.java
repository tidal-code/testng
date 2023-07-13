package testngcore;

import com.tidal.flow.assertions.stackbuilder.ErrorStack;
import com.tidal.utils.exceptions.AzureOperationsException;
import com.tidal.utils.filehandlers.FileReader;
import com.tidal.utils.filehandlers.Finder;
import com.tidal.utils.json.JsonReader;
import com.tidal.utils.loggers.Logger;
import com.tidal.utils.propertieshandler.Config;
import com.tidal.utils.propertieshandler.PropertiesFinder;
import com.tidal.utils.report.ReportBuilder;
import com.tidal.utils.report.Reporter;
import com.tidal.wave.browser.Browser;
import com.tidal.wave.browser.Driver;
import com.tidal.wave.options.BrowserWithOptions;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.testng.*;
import utils.AllureUtils;
import utils.FileFinder;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tidal.utils.utils.CheckString.isNullOrEmpty;
import static com.tidal.wave.browser.Browser.close;


public class TestListener implements ITestListener, IInvokedMethodListener {


    @Override
    public void onTestStart(ITestResult result) {
        String testCaseName = null;
        if (result.getMethod().isDataDriven()) {
            Object dataProviderObject = result.getParameters()[0];

            if (dataProviderObject instanceof String) {
                testCaseName = dataProviderObject.toString();
            } else {
                try {
                    Field field = dataProviderObject.getClass().getDeclaredField("testCaseName");
                    field.setAccessible(true);
                    testCaseName = (String) field.get(dataProviderObject);
                    System.out.println("Test case name is " + testCaseName);
                } catch (IllegalAccessException | NoSuchFieldException ex) {
                    ex.printStackTrace();
                }

            }
            if (!isNullOrEmpty(testCaseName)) {
                System.out.println("Inside test listner=====================================================================================================================");
                System.out.println("Setting description to "+testCaseName);
                String currentDescription = result.getMethod().getDescription();
                //result.getMethod().setDescription(currentDescription+"-"+testCaseName);
            }
        }
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
        System.out.println("=================================================INSIDE TESTNG LISTENTER"+result.getName() +" ============================================");
        try {
            if (isUiTest(result)) {
                Logger.info(TestListener.class,"Attaching screenshot");
                Allure.addAttachment(result.getName(), "image/png", new ByteArrayInputStream(getScreenshot()), ".png");
                close();
            }

        } finally {
            new ErrorStack().execute();
        }
    }

    private byte[] getScreenshot(){
        return ((TakesScreenshot)Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("=================================================INSIDE TESTNG LISTENTER"+result.getName() +" ============================================");

        try {
            if (isUiTest(result))
                close();

        } finally {
            new ErrorStack().execute();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("=================================================INSIDE TESTNG LISTENTER"+result.getName() +" ============================================");

        try {
            if (isUiTest(result))
                close();
        } finally {
            new ErrorStack().execute();
        }
    }


    @Override
    public void onFinish(ITestContext context) {
        Iterator<ITestResult> skippedTestCases = context.getSkippedTests().getAllResults().iterator();
        while (skippedTestCases.hasNext()) {
            ITestResult skippedTestCase = skippedTestCases.next();
            ITestNGMethod method = skippedTestCase.getMethod();
            if (context.getSkippedTests().getResults(method).size() > 0) {
                skippedTestCases.remove();
            }
        }
        List<String> fileNames = FileFinder.findFile("-result.json", Paths.get("target"));
        fileNames.parallelStream().forEach(fileName -> {
            String jsonValue = FileReader.readFileToString(fileName, Paths.get("target"));
            if (jsonValue.contains("parameters\":[{\"name\"") && jsonValue.contains("\"status\":\"skipped\"")) {
                FileFinder.deleteFile(fileName, Paths.get("target"));
            }
        });
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
        boolean flag= Arrays.stream(result.getMethod().getGroups())
                .noneMatch(group -> group.contains("apiTest") || group.contains("dbTest"));
        return flag;
    }

}


