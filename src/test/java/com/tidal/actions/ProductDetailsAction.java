package com.tidal.actions;

import com.tidal.pages.ProductDetailsPage;
import io.qameta.allure.Allure;

import static com.tidal.wave.webelement.ElementFinder.find;

public class ProductDetailsAction {


    public static void clickAddToBagButton(){
        Allure.step("Click on add to bag button");
        find(ProductDetailsPage.ADD_TO_CART_BUTTON).click();
    }

    public static String getProductTitle(){
        Allure.step("Fetch the product title");
        return find(ProductDetailsPage.PRODUCT_NAME_TEXT).getText();
    }
}
