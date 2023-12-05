package dev.tidalcode.testng.testngcore;

import com.tidal.flow.assertions.stackbuilder.ErrorStack;
import com.tidal.stream.zephyrscale.ZephyrScale;
import com.tidal.utils.csv.CsvData;
import com.tidal.utils.filehandlers.FileOutWriter;
import com.tidal.utils.filehandlers.FilePaths;
import com.tidal.utils.filehandlers.FileReader;
import com.tidal.utils.propertieshandler.PropertiesFinder;
import com.tidal.utils.scenario.ScenarioInfo;
import com.tidal.utils.utils.Helper;
import com.tidal.wave.browser.Browser;
import com.tidal.wave.browser.Driver;
import com.tidal.wave.config.Config;
import com.tidal.wave.options.BrowserWithOptions;
import dev.tidalcode.testng.reports.Feature;
import dev.tidalcode.testng.reports.Story;
import dev.tidalcode.testng.utils.DataFormatter;
import dev.tidalcode.testng.utils.FileFinder;
import dev.tidalcode.testng.utils.TestScenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.slf4j.Logger;
import org.testng.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import static com.tidal.utils.utils.CheckString.isNotNullOrEmpty;
import static com.tidal.utils.utils.CheckString.isNullOrEmpty;
import static com.tidal.wave.browser.Browser.close;


public class TestListener implements ITestListener, IHookable {
    public Logger logger = LoggerFactory.getLogger(TestListener.class);
    private static final Path TARGET_FOLDER_PATH = Paths.get(Helper.getAbsoluteFromRelativePath(FilePaths.TARGET_FOLDER_PATH.getPath()));
    private static final Path PATH_TO_WRITE_FILE = Paths.get(TARGET_FOLDER_PATH.toString(), "screenshots");

    @Override
    public void onTestStart(ITestResult result) {
        setReportAttributes(result);
        if ("true".equalsIgnoreCase(PropertiesFinder.getProperty("testng.mode.dryrun"))) {
            logger.info("Dry Run mode chosen. test will not be run");
            return;
        }
        if (result.getMethod().isDataDriven()) {
            String currentDescription = DataFormatter.formatTestDescription(result.getMethod().getDescription(), result.getParameters());
            TestScenario.setTestDescription(currentDescription);
            result.setAttribute("customNameAttribute", currentDescription);
            ScenarioInfo.setScenarioName(currentDescription);
        } else {
            result.setAttribute("customNameAttribute", result.getMethod().getDescription());
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
                logger.info("Running in local mode");
                options = setLocalOptions(browser);
                duration = Duration.ofSeconds(Config.LOCAL_TIMEOUT);
            } else if (executionType.equalsIgnoreCase("docker") || executionType.equalsIgnoreCase("remote")) {
                logger.info("Running in remote mode");
                options = setRemoteOptions(browser);
                duration = Duration.ofSeconds(Config.REMOTE_TIMEOUT);
            }

            //Option to complete the initial setting without setting up a browser session
            if (!isNullOrEmpty(Config.BASE_URL)) {
                logger.info("Test starting with options {}", options);
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
        publishResultToZephyrScale(result);
        saveScreenShotForUpload(result);
        closure(result);
    }

    //to be used for ado screenshot upload
    private byte[] getScreenshot() {
        return ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES);
    }

    private void saveScreenShotForUpload(ITestResult result) {
        if (isUiTest(result)) {
            byte[] screenShot = getScreenshot();
            String formattedFileName = result.getAttribute("customNameAttribute").toString().replaceAll("[^a-zA-Z0-9]", "");
            Path screenshotStringPath = Paths.get(PATH_TO_WRITE_FILE.toString(), formattedFileName + ".txt");
            String encodedScreenshotData = Base64.getEncoder().encodeToString(screenShot);
            FileOutWriter.createDirectory(PATH_TO_WRITE_FILE.toString());
            FileOutWriter.writeFileTo(encodedScreenshotData, screenshotStringPath.toString());
        }
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
        publishResultToZephyrScale(result);
        closure(result);
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
        if (!(isUiTest(result) && Arrays.stream(result.getMethod().getGroups()).anyMatch(group -> group.equalsIgnoreCase("debug")) && "local".equalsIgnoreCase(Config.EXECUTION_TYPE)))
            close();
    }

    private String getJiraId(ITestResult result) {

        if (result.getMethod().isTest()) {
            if (result.getMethod().getConstructorOrMethod().getMethod().isAnnotationPresent(JiraId.class)) {
                return result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(JiraId.class).value();
            }

            CsvData csvData = new CsvData();
            csvData.setCSVFolderAsDataFilePath();
            return csvData.readDataFrom("TestLinkData", "Key");
        }
        return null;
    }


    //to fail the test case in case of a soft assertion failure
    @Override
    public void run(IHookCallBack iHookCallBack, ITestResult iTestResult) {
        iHookCallBack.runTestMethod(iTestResult);
        new ErrorStack().execute();
    }

    private void publishResultToZephyrScale(ITestResult result) {
        String zephyrResultUpdate = PropertiesFinder.getProperty("zephyr.results.update");

//        logger.info("Publishing results to Zephyr");
//        logger.info("Zephyr result publish (true/false) {}", zephyrResultUpdate);

        if (isNotNullOrEmpty(zephyrResultUpdate)  && zephyrResultUpdate.equals("true") ) {

            new ZephyrScale.TestResults().updateTestNGResults()
                    .testTagProcessor(getJiraId(result))
                    .testStatus(result.isSuccess()) //Negation added to negate the negative result.
                    .publish();
        }
    }
}


