package dev.tidalcode.testng.actions;

import dev.tidalcode.testng.pages.ProductDetailsPage;
import dev.tidalcode.testng.reports.TestInfo;

import static com.tidal.wave.webelement.ElementFinder.find;

public class ProductDetailsAction {


    public static void clickAddToBagButton(){
        TestInfo.step("Click on add to bag button");
        find(ProductDetailsPage.ADD_TO_CART_BUTTON).click();
    }

    public static String getProductTitle(){
        TestInfo.step("Fetch the product title");
        return find(ProductDetailsPage.PRODUCT_NAME_TEXT).getText();
    }
}
