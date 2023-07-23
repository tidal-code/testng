package com.tidal.actions;

import com.tidal.pages.SearchResultsPage;
import io.qameta.allure.Allure;

import static com.tidal.wave.webelement.ElementFinder.find;

public class SearchActions {

    public static void selectFromSearchResults(int position){
        Allure.step("Select the item at position "+position);
        find(String.format(SearchResultsPage.SEARCH_RESULT_ITEM,position)).click();
    }
}
