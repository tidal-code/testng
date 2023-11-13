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

import java.io.ByteArrayInputStream;

public class AllureListener implements TestLifecycleListener {


    @Override
    public void beforeTestWrite(TestResult result) {
        if (!result.getParameters().isEmpty()) {
            result.setName(TestScenario.getTestDescription());
            TestScenario.removeCurrentTestScenario();
        }
    }

    @Override
    public void beforeTestStop(TestResult result) {
        TestInfo.remove(result.getStatus());
        if (!(result.getFullName().contains("API_") || result.getFullName().contains("DB_")) && (result.getStatus().equals(Status.FAILED) || result.getStatus().equals(Status.BROKEN))) {
            Allure.addAttachment(result.getName() + "_Failed_Screenshot", new ByteArrayInputStream(((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.BYTES)));
        }
    }
}

