package dev.tidalcode.testng.actions;

import dev.tidalcode.testng.pages.HomePage;
import dev.tidalcode.testng.reports.TestInfo;

import static dev.tidalcode.wave.webelement.ElementFinder.find;

public class HomeActions {

    public static void searchForItem(String keyWord){
        TestInfo.step("Search for the keyword "+keyWord);
        find(HomePage.SEARCH_INPUT).sendKeys(keyWord).pressEnter();
    }
}
