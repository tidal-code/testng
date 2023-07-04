package com.tidal.tests;

import com.tidal.actions.HomeActions;
import com.tidal.actions.SearchActions;
import com.tidal.flow.assertions.Assert;
import io.qameta.allure.Allure;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

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
    @Test(groups ={ "SmokeTest","RegressionTest"},description = "Test to verify search navigation")
    public void testCartNavigation(){
        HomeActions.searchForItem("water bottle");
        SearchActions.selectFromSearchResults(4);
        Allure.step("Verify the product title in PDP page");
        Assert.verify("Verify product title in PDP page ",getProductTitle()).contains("Bottle");

    }

}
