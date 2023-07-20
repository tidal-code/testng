package testngcore;

import com.tidal.utils.propertieshandler.PropertiesFinder;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;


/**
 * Retry failed tests for a specified maximum count on test failure
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    int counter = 0;
    int retryLimit = Integer.parseInt(PropertiesFinder.getProperty("failedRetryCount"));

    @Override
    public boolean retry(ITestResult iTestResult) {
        System.out.println("Inside retry analyzer for "+iTestResult.getName()+" for count "+iTestResult.getMethod().getInvocationCount()+ " and current retry counter is set to "+counter);
        if (counter < retryLimit) {

            counter++;
            System.out.println("Retrying "+iTestResult.getName());
            return true;
        }
        return false;
    }
}
