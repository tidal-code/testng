package com.tidal.tests;

import com.tidal.actions.HomeActions;
import com.tidal.actions.SearchActions;
import com.tidal.flow.assertions.Assert;
import io.qameta.allure.Allure;
import io.qameta.allure.Story;
import org.apache.commons.lang3.RandomUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.AllureUtils;

import static com.tidal.actions.ProductDetailsAction.getProductTitle;

public class Tester {

    @Story("Test JIRA ID - Search")
    @Test(groups ={ "SmokeTest","RegressionTest"},description = "Test to verify search navigation")
    public void testSearchNavigation(){
        HomeActions.searchForItem("Shampoo");
        SearchActions.selectFromSearchResults(4);
        Assert.verify("Verify product title in PDP page ",getProductTitle()).contains("Shampoo");
    }


    @Story("Test JIRA ID - Search")
    @Test(groups ={ "SmokeTest","RegressionTest","specific"},description = "Test to verify search navigation")
    public void testCartNavigation(){
        HomeActions.searchForItem("water bottle");
        SearchActions.selectFromSearchResults(4);
        Allure.step("Verify the product title in PDP page");
        Assert.verify("Verify product title in PDP page ",getProductTitle()).contains("Shruti");

    }


    @DataProvider(name = "testData",parallel = true)
    public Object[][] getData(){
        return new Object[][]{
                {new SearchData("Test1", 1 ,"Test One")},
                {new SearchData("Test2", 2, "Test Two")},
                {new SearchData("Test3", 3, "Test Three")}
        };
    }

    @Test(description = "To test data provider", dataProvider = "testData", groups = {"testDataTEST","RegressionTest"})
    public void dummyTest(SearchData searchData){
        AllureUtils.updateTestCaseNameWithDataProvider(searchData.testCaseName);
        System.out.println("This is for "+searchData.name+" "+searchData.age);
        int number= RandomUtils.nextInt(0,3);
        Assert.verify("Verifying "+number,number).isEqualTo(searchData.age);
    }
}
