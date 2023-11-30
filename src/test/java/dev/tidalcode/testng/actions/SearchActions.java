package dev.tidalcode.testng.actions;

import dev.tidalcode.testng.pages.SearchResultsPage;
import dev.tidalcode.testng.reports.TestInfo;

import static com.tidal.wave.webelement.ElementFinder.find;

public class SearchActions {

    public static void selectFromSearchResults(int position){
        TestInfo.step("Select the item at position "+position);
        find(String.format(SearchResultsPage.SEARCH_RESULT_ITEM,position)).click();
    }
}
