package testngcore;

import com.tidal.flow.assertions.stackbuilder.ErrorStack;
import com.tidal.utils.filehandlers.FileOutWriter;
import com.tidal.utils.filehandlers.FilePaths;
import com.tidal.utils.filehandlers.FileReader;
import com.tidal.utils.propertieshandler.Config;
import com.tidal.utils.propertieshandler.PropertiesFinder;
import com.tidal.utils.scenario.ScenarioInfo;
import com.tidal.utils.utils.Helper;
import com.tidal.wave.browser.Browser;
import com.tidal.wave.browser.Driver;
import com.tidal.wave.options.BrowserWithOptions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.testng.*;
import utils.FileFinder;
import utils.TestDescriptionFinder;
import utils.TestScenario;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import static com.tidal.utils.filehandlers.FilePaths.TARGET_FOLDER_PATH;
import static com.tidal.utils.utils.CheckString.isNullOrEmpty;
import static com.tidal.wave.browser.Browser.close;


public class TestListener implements ITestListener {
    private static final Path TARGET_FOLDER_PATH = Paths.get(Helper.getAbsoluteFromRelativePath(FilePaths.TARGET_FOLDER_PATH.getPath()));
    private static final Path PATH_TO_WRITE_FILE = Paths.get(TARGET_FOLDER_PATH.toString(), "screenshots");


    @Override
    public void onTestStart(ITestResult result) {
        String currentDescription=result.getMethod().getDescription();
        if (result.getMethod().isDataDriven()) {
            String testCaseName = TestDescriptionFinder.findTestParameters(result);
            if (!isNullOrEmpty(testCaseName)) {
                currentDescription = currentDescription+ "  " + testCaseName;
                TestScenario.setTestDescription(currentDescription);
                result.setAttribute("customNameAttribute",currentDescription);
                result.getTestContext().setAttribute("customNameAttribute",currentDescription);
            }
        }
        //to be used in the custom junit reporter
        result.setAttribute("customNameAttribute",currentDescription);
        ScenarioInfo.setScenarioName(TestScenario.getTestDescription());
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
        closure(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        closure(result);
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

    private void closure(ITestResult result) {
        try {
            if (isUiTest(result) && Arrays.stream(result.getMethod().getGroups()).noneMatch(group -> group.contains("debug"))) {
                close();
            }
        } finally {
            new ErrorStack().execute();
        }
    }

}


