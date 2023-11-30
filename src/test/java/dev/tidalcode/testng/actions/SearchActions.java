package dev.tidalcode.testng.actions;

import com.tidal.flow.assertions.Soft;
import dev.tidalcode.testng.pages.SearchResultsPage;
import dev.tidalcode.testng.reports.TestInfo;

import static dev.tidalcode.wave.webelement.ElementFinder.find;

public class SearchActions {

    public static void selectFromSearchResults(int position){
        TestInfo.step("Select the item at position "+position);
        TestInfo.step("Details Test");
        TestInfo.step("Details Test2");
        Soft.verify("Test in reports", "abc").isEqualTo("def");
        TestInfo.addIssueLink("Defect Raised", "https://google.co.nz");
        TestInfo.evidence("Test Data", "Step Success");
        find(String.format(SearchResultsPage.SEARCH_RESULT_ITEM,position)).click();
    }

    public static void selectFromSearchResults2(int position){
        TestInfo.step("Select the item at position "+position);
        TestInfo.step("Details Test");
        TestInfo.step("Details Test2");
        Soft.verify("Test in reports", "abc").isEqualTo("abc");
        TestInfo.addIssueLink("Defect Raised", "https://google.co.nz");
        TestInfo.evidence("Test Data", "Step Success");
        find(String.format(SearchResultsPage.SEARCH_RESULT_ITEM,position)).click();
    }
}
