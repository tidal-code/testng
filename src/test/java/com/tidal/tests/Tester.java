package com.tidal.tests;

import com.tidal.actions.HomeActions;
import com.tidal.actions.SearchActions;
import com.tidal.flow.assertions.Assert;
import com.tidal.flow.assertions.Soft;
import io.qameta.allure.Allure;
import io.qameta.allure.Story;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static com.tidal.actions.ProductDetailsAction.getProductTitle;

public class Tester {

    @Story("Test JIRA ID - Search")
    @Test(groups = {"SmokeTest", "RegressionTest"}, description = "Test to verify search navigation")
    public void testSearchNavigation() {
        HomeActions.searchForItem("Shampoo");
        SearchActions.selectFromSearchResults(4);
        Assert.verify("Verify product title in PDP page ", getProductTitle()).contains("Shampoo");
    }


    @Story("Test JIRA ID - Search")
    @Test(groups = {"SmokeTest", "RegressionTest", "specific"}, description = "Test to verify search navigation")
    public void testCartNavigation() {
        HomeActions.searchForItem("water bottle");
        SearchActions.selectFromSearchResults(4);
        Allure.step("Verify the product title in PDP page");
        Assert.verify("Verify product title in PDP page ", getProductTitle()).contains("Shruti");

    }


    @DataProvider(name = "testData", parallel = true)
    public static Iterator<Object[]> getData() {
        List<Object[]> dataToBeReturned = new ArrayList<Object[]>();
        dataToBeReturned.add(new Object[]{1});
        dataToBeReturned.add(new Object[]{2});
        dataToBeReturned.add(new Object[]{3});
        return dataToBeReturned.iterator();
    }

    @Test(description = "To test data provider", dataProvider = "testData", groups = {"dataProviderTest", "RegressionTest"})
    public void numberModTest(int searchData) {
        System.out.println("This is for " + searchData + " " + searchData);
        int result = searchData % 2;
        Assert.verify("Verify that the number is even " + searchData, result).isEqualTo(0);
    }

    @Test(description = "To test data soft assertion", groups = {"softTest", "RegressionTest"})
    public void softAssertionTest() {
        HomeActions.searchForItem("water bottle");
        IntStream.range(0,5).forEach(number->
                Soft.verify("Verify value for "+number,(number%2)).isEqualTo(0));
    }
}
