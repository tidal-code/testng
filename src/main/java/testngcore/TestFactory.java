package testngcore;

import com.tidal.utils.propertieshandler.PropertiesFinder;
import com.tidal.wave.exceptions.RuntimeTestException;
import org.testng.*;
import org.testng.internal.Configuration;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.tidal.utils.utils.CheckString.isNullOrEmpty;


/**
 * TestFactory class is used to generate the testng runner dynamically without testng xml file
 */
public class TestFactory implements ITestRunnerFactory {

    @Override
    public TestRunner newTestRunner(ISuite iSuite, XmlTest xmlTest, Collection<IInvokedMethodListener> collection, List<IClassListener> list) {
        try {
            List<String> includedGroups=getIncludedTestGroups();
            List<String> excludedGroups=new ArrayList<>(List.of("ignore","wip"));
            String[] packagesToRun = PropertiesFinder.getProperty("testPackageNames").split(",");
            int threadCount = Integer.parseInt(PropertiesFinder.getProperty("threadCount"));
            int dataProviderThreadCount = Integer.parseInt(PropertiesFinder.getProperty("dataProviderThreadCount"));
            XmlSuite xmlSuite = new XmlSuite();
            xmlSuite.setParallel(XmlSuite.ParallelMode.METHODS);
            xmlSuite.setThreadCount(threadCount);
            xmlSuite.setDataProviderThreadCount(dataProviderThreadCount);
            xmlSuite.setName(PropertiesFinder.getProperty("testSuiteName"));
            XmlTest test = new XmlTest(xmlSuite);
            test.setIncludedGroups(includedGroups);
            test.setExcludedGroups(excludedGroups);
            ArrayList<XmlPackage> xmlPackages = new ArrayList<XmlPackage>();
            for (String eachPackage : packagesToRun) {
                xmlPackages.add(new XmlPackage(eachPackage));
            }
            test.setPackages(xmlPackages);
            return new TestRunner(new Configuration(), iSuite, test, true, collection, list);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeTestException("Unable to generate testng runner, exception " + ex.getMessage());
        }
    }

    private List<String> getIncludedTestGroups(){
        String[] testTags = {PropertiesFinder.getProperty("tagName"), PropertiesFinder.getProperty("tagNameTwo"), PropertiesFinder.getProperty("tagNameThree")};
        return Arrays.stream(testTags)
                .filter(value -> !isNullOrEmpty(value))
                .collect(Collectors.toList());

    }

}
