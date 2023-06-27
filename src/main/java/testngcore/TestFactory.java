package testngcore;

import com.tidal.utils.propertieshandler.PropertiesFinder;
import com.tidal.wave.exceptions.RuntimeTestException;
import org.testng.*;
import org.testng.internal.Configuration;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * TestFactory class is used to generate the testng runner dynamically without testng xml file
 */
public class TestFactory implements ITestRunnerFactory {
    @Override
    public TestRunner newTestRunner(ISuite iSuite, XmlTest xmlTest, Collection<IInvokedMethodListener> collection, List<IClassListener> list) {
        try {
            String[] includedTestGroups = PropertiesFinder.getProperty("testIncludedGroups").split(",");
            String[] excludedTestGroups = PropertiesFinder.getProperty("testExcludedGroups").split(",");
            String[] packagesToRun = PropertiesFinder.getProperty("testPackageNames").split(",");
            int threadCount = Integer.parseInt(PropertiesFinder.getProperty("threadCount"));
            int dataProviderThreadCount = Integer.parseInt(PropertiesFinder.getProperty("dataProviderThreadCount"));
            XmlSuite xmlSuite = new XmlSuite();
            xmlSuite.setParallel(XmlSuite.ParallelMode.METHODS);
            xmlSuite.setThreadCount(threadCount);
            xmlSuite.setDataProviderThreadCount(dataProviderThreadCount);
            xmlSuite.setName(PropertiesFinder.getProperty("testSuiteName"));
            XmlTest test = new XmlTest(xmlSuite);
            for (String testGroup : includedTestGroups) {
                test.addIncludedGroup(testGroup);
            }
            for (String testGroup : excludedTestGroups) {
                test.addExcludedGroup(testGroup);
            }
            ArrayList<XmlPackage> xmlPackages = new ArrayList<XmlPackage>();
            for (String eachPackage : packagesToRun) {
                xmlPackages.add(new XmlPackage(eachPackage));
            }
            test.setPackages(xmlPackages);
            return new TestRunner(new Configuration(), iSuite, test, true, collection, list);
        } catch (Exception ex) {
            throw new RuntimeTestException("Unable to generate testng runner, exception " + ex.getMessage());
        }
    }

}
