package utils;

public class TestScenario {

    private String testDescription;
    private static ThreadLocal<TestScenario> testScenarioThreadLocal=ThreadLocal.withInitial(TestScenario::new);

    private TestScenario() {
    }

    public static TestScenario getCurrentTestScenario(){
        return testScenarioThreadLocal.get();
    }

    public static void removeCurrentTestScenario(){
        testScenarioThreadLocal.remove();
    }

    public static String getTestDescription() {
        return getCurrentTestScenario().testDescription;
    }

    public static void setTestDescription(String testDescription) {
        getCurrentTestScenario().testDescription = testDescription;
    }

}
