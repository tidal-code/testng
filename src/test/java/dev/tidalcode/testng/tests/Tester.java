
package dev.tidalcode.testng.tests;

import dev.tidalcode.testng.actions.HomeActions;
import com.tidal.flow.assertions.Assert;
import com.tidal.flow.assertions.Soft;
import dev.tidalcode.testng.actions.SearchActions;
import dev.tidalcode.testng.reports.Story;
import dev.tidalcode.testng.reports.TestInfo;
import dev.tidalcode.testng.reports.Feature;
import io.qameta.allure.Allure;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import dev.tidalcode.testng.testngcore.JiraId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static dev.tidalcode.testng.actions.ProductDetailsAction.getProductTitle;


@Feature(value = "Unit Tests")
public class Tester {

    @Story("Test JIRA ID - Search")
    @JiraId(value = "234343")
    @Test(groups = {"SmokeTest", "RegressionTest", "allure"}, description = "TestInfo to verify search navigation")
    public void testSearchNavigation() {
        TestInfo.given("Searching for product");
        TestInfo.step("Searching for product in the next page");
        HomeActions.searchForItem("Shampoo");
        TestInfo.when("Selecting an item");
        TestInfo.when("Selecting another item");
        SearchActions.selectFromSearchResults(4);
        TestInfo.then("Verifying the product title in PDP Page");
        Assert.verify("Verify product title in PDP page ", getProductTitle()).contains("Shampoo");
    }

    @Story("Test JIRA ID - Search")
    @JiraId(value = "234343")
    @Test(groups = {"SmokeTest", "RegressionTest", "allure"}, description = "TestInfo to verify search navigation 2")
    public void testSearchNavigation2() {
        TestInfo.given("Searching for product");
        TestInfo.step("Searching for product in the next page");
        HomeActions.searchForItem("Shampoo");
        TestInfo.when("Selecting an item");
        TestInfo.when("Selecting another item");
        SearchActions.selectFromSearchResults(4);
        TestInfo.then("Verifying the product title in PDP Page");
        Assert.verify("Verify product title in PDP page ", getProductTitle()).contains("Shampoo");
    }

    @Story("Test JIRA ID - Search")
    @JiraId(value = "234343")
    @Test(groups = {"SmokeTest", "RegressionTest", "allure"}, description = "TestInfo to verify search navigation 3")
    public void testSearchNavigation3() {
        TestInfo.given("Searching for product");
        TestInfo.step("Searching for product in the next page");
        HomeActions.searchForItem("Shampoo");
        TestInfo.when("Selecting an item");
        TestInfo.when("Selecting another item");
        SearchActions.selectFromSearchResults(4);
        TestInfo.then("Verifying the product title in PDP Page");
        Assert.verify("Verify product title in PDP page ", getProductTitle()).contains("Shampoo");
    }


    @Story("TestInfo JIRA ID - Search")
    @Test(groups = {"SmokeTest", "RegressionTest", "specialRun"}, description = "TestInfo to verify search navigation from Tester")
    public void testCartNavigation() {
        TestInfo.given("Searching for product X");
//        HomeActions.searchForItem("Shampoo");
        TestInfo.when("Selecting an item X");
        TestInfo.when("Selecting another item X");
        TestInfo.evidence("Work Order Number", "234343434 X");
        TestInfo.step("Details Test X");
        TestInfo.step("Details Test2 X");
//        SearchActions.selectFromSearchResults(4);
        TestInfo.then("Verifying the product title in PDP Page X");
        TestInfo.step("Verifying One X");
        TestInfo.step("Verifying Two X");
        TestInfo.evidence("Work Order Number", "234343434 X");
        TestInfo.evidence("Work Order Number 2", "234343434 XYZ");
//        HomeActions.searchForItem("water bottle");
//        SearchActions.selectFromSearchResults(4);
        TestInfo.step("Verify the product title in PDP page");
        Assert.verify("Test", true).isFalse();
        TestInfo.step("Verify the product title in PDP page Two");
//        Assert.verify("Verify product title in PDP page ", getProductTitle()).contains("Shruti");

    }


    @DataProvider(name = "testData", parallel = true)
    public static Iterator<Object[]> getData() {
        System.out.println("Executing data provider");
        List<Object[]> dataToBeReturned = new ArrayList<Object[]>();
        dataToBeReturned.add(new Object[]{1});
        dataToBeReturned.add(new Object[]{2});
        dataToBeReturned.add(new Object[]{3});
        return dataToBeReturned.iterator();
    }

    @JiraId("1,2,3")
    @Test(description = "To test data provider for a scenario without placeholders", dataProvider = "testData", groups = {"dataProviderTest", "RegressionTest", "demoTest","debug"})
    public void numberModTestWithoutPlaceHolders(int searchData) {
        System.out.println("This is for " + searchData + " " + searchData);
        int result = searchData % 2;
        Assert.verify("Verify that the number is even " + searchData, result).isEqualTo(0);
    }


    @JiraId("1234")
    @Test(description = "To test data soft assertion", groups = {"softTest", "RegressionTest"})
    public void softAssertionTest() {
        HomeActions.searchForItem("water bottle");
        IntStream.range(0, 5).forEach(number ->
                Soft.verify("Verify value for " + number, (number % 2)).isEqualTo(0));
    }

    @JiraId("24,25,26")
    @Test(description = "To test data provider for {0}", dataProvider = "testData", groups = {"dataProviderTest", "RegressionTest","demoTest","debug"})
    public void numberModTestWithPlaceHolders(int searchData) {
        System.out.println("This is for " + searchData + " " + searchData);
        int result = searchData % 2;
        Assert.verify("Verify that the number is even " + searchData, result).isEqualTo(0);
    }
}


