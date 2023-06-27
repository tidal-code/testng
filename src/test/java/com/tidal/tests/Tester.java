package com.tidal.tests;

import com.tidal.wave.exceptions.RuntimeTestException;
import org.testng.annotations.Test;

import static com.tidal.wave.webelement.ElementFinder.find;


public class Tester {



    @Test(groups = {"SmokeTest", "RegressionTest"})
    public void testMethod() {
        System.out.println("This is a test");
    }

    @Test(groups = {"SmokeTest", "RegressionTest", "uitest"})
    public void testUiMethod() {
        find("name:q").click();
        System.out.println("This is a UI test");
    }

    @Test(groups = {"SmokeTest", "RegressionTest", "uitest"})
    public void testAnotherUiMethod() {

        System.out.println("This is a another UI test");
    }

    @Test(groups = {"SmokeTest", "RegressionTest", "uitest"})
    public void testFailingMethod() {
        System.out.println("This is a failing UI test");
        throw new RuntimeTestException("Test Failure");
    }
}
