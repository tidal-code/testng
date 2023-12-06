package dev.tidalcode.testng.testngcore;

import com.tidal.wave.browser.Driver;
import dev.tidalcode.testng.reports.TestInfo;
import io.qameta.allure.Allure;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.TestResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import dev.tidalcode.testng.utils.TestScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class AllureListener implements TestLifecycleListener {
    private static Logger logger = LoggerFactory.getLogger(AllureListener.class);

    @Override
    public void beforeTestStop(TestResult result) {
        try {
            if (!result.getParameters().isEmpty()) {
                result.setName(TestScenario.getTestDescription());
                TestScenario.removeCurrentTestScenario();
            }
            TestInfo.remove(result.getStatus());
            if (!(result.getFullName().contains("API_") || result.getFullName().contains("DB_")) && (result.getStatus().equals(Status.FAILED) || result.getStatus().equals(Status.BROKEN))) {
                if (null != Driver.getDriver()) {
                    Allure.addAttachment(result.getName() + "_Failed_Screenshot", new ByteArrayInputStream(((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES)));
                }
            }
        } catch (Exception e){
            logger.info("Exception: {}" , Arrays.toString(e.getStackTrace()));
        }
    }
}

