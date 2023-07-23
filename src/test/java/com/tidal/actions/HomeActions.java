package com.tidal.actions;

import com.tidal.pages.HomePage;
import io.qameta.allure.Allure;

import static com.tidal.wave.webelement.ElementFinder.find;

public class HomeActions {

    public static void searchForItem(String keyWord){
        Allure.step("Search for the keyword "+keyWord);
        find(HomePage.SEARCH_INPUT).sendKeys(keyWord).pressEnter();
    }
}
