package dev.tidalcode.testng.testngcore;

import com.tidal.flow.assertions.stackbuilder.ErrorStack;
import com.tidal.utils.filehandlers.FileReader;
import com.tidal.utils.propertieshandler.Config;
import com.tidal.utils.propertieshandler.PropertiesFinder;
import com.tidal.utils.scenario.ScenarioInfo;
import com.tidal.wave.browser.Browser;
import com.tidal.wave.browser.Driver;
import com.tidal.wave.options.BrowserWithOptions;
import dev.tidalcode.testng.reports.Feature;
import dev.tidalcode.testng.reports.Story;
import dev.tidalcode.testng.utils.DataFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.testng.*;
import dev.tidalcode.testng.utils.FileFinder;
import dev.tidalcode.testng.utils.TestScenario;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.tidal.utils.utils.CheckString.isNullOrEmpty;
import static com.tidal.wave.browser.Browser.close;


public class TestListener implements ITestListener, IHookable {


    @Override
    public void onTestStart(ITestResult result) {
        setReportAttributes(result);
        if ("true".equalsIgnoreCase(PropertiesFinder.getProperty("testng.mode.dryrun"))) {
            return;
        }
        if (result.getMethod().isDataDriven()) {
            String currentDescription = DataFormatter.formatTestDescription(result.getMethod().getDescription(), result.getParameters());
            TestScenario.setTestDescription(currentDescription);
            ScenarioInfo.setScenarioName(currentDescription);

        } else {
            ScenarioInfo.setScenarioName(result.getMethod().getDescription());
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

    private static void setReportAttributes(ITestResult result) {
        Feature annotatedFeature = result.getMethod().getTestClass().getRealClass().getAnnotation(Feature.class);
        if (null != annotatedFeature) {
            TestScenario.setFeature(annotatedFeature.value());
        }
        Story annotatedStory = result.getMethod().getTestClass().getRealClass().getAnnotation(Story.class);
        if (null != annotatedStory) {
            TestScenario.setStory(annotatedStory.value());
        }
    }


    @Override
    public void onTestFailure(ITestResult result) {
        if ("true".equalsIgnoreCase(PropertiesFinder.getProperty("testng.mode.dryrun"))) {
            return;
        }
        closure(result);
        getJiraId(result);
    }

    //to be used for ado screenshot upload
    private byte[] getScreenshot() {
        return ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if ("true".equalsIgnoreCase(PropertiesFinder.getProperty("testng.mode.dryrun"))) {
            return;
        }
        closure(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if ("true".equalsIgnoreCase(PropertiesFinder.getProperty("testng.mode.dryrun"))) {
            return;
        }
        closure(result);
        getJiraId(result);
    }


    @Override
    public void onFinish(ITestContext context) {
        Iterator<ITestResult> skippedTestCases = context.getSkippedTests().getAllResults().iterator();
        while (skippedTestCases.hasNext()) {
            ITestResult skippedTestCase = skippedTestCases.next();
            ITestNGMethod method = skippedTestCase.getMethod();
            if (!context.getSkippedTests().getResults(method).isEmpty()) {
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


    private AbstractDriverOptions<?> setLocalOptions(String browserType) {
        return new BrowserWithOptions().getLocalOptions(browserType);
    }

    private AbstractDriverOptions<?> setRemoteOptions(String browserType) {
        return new BrowserWithOptions().getRemoteOptions(browserType);
    }


    public boolean isUiTest(ITestResult result) {
        return Arrays.stream(result.getMethod().getGroups())
                .noneMatch(group -> group.contains("apiTest") || group.contains("dbTest"));
    }


    //do not close browser if debug group is added in local mode
    private void closure(ITestResult result) {
        if ("local".equalsIgnoreCase(Config.EXECUTION_TYPE) && isUiTest(result) && Arrays.stream(result.getMethod().getGroups()).noneMatch(group -> group.equalsIgnoreCase("debug")))
            close();
    }

    private String getJiraId(ITestResult result) {
        String jiraId = "";
        if (result.getMethod().isTest()) {
            if (result.getMethod().getConstructorOrMethod().getMethod().isAnnotationPresent(JiraId.class) && !result.getMethod().isDataDriven()) {
                jiraId = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(JiraId.class).value();
            }
        }
        return jiraId;
    }


    //to fail the test case in case of a soft assertion failure
    @Override
    public void run(IHookCallBack iHookCallBack, ITestResult iTestResult) {
        iHookCallBack.runTestMethod(iTestResult);
        new ErrorStack().execute();
    }
}


