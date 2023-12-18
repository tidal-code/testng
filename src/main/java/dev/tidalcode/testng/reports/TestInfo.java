package dev.tidalcode.testng.reports;

import dev.tidalcode.testng.utils.TestScenario;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.*;

public class TestInfo extends TestListenerAdapter {

    private String stepData = "NULL";
    private String previousStepData = "NULL";
    private final List<SubStep> subSteps = new LinkedList<>();
    private Status status = Status.PASSED;
    private static final ThreadLocal<TestInfo> testInfo = ThreadLocal.withInitial(TestInfo::returnNewTestInfo);


    @Override
    public void onTestFailure(ITestResult tr) {
      status = Status.FAILED;
    }

    private static TestInfo returnNewTestInfo() {
        if (null != TestScenario.getFeature()) {
            Allure.feature(TestScenario.getFeature());
        }
        if (null != TestScenario.getStory()) {
            Allure.story(TestScenario.getStory());
        }
        return new TestInfo();
    }

    public static void given(String preCondition) {
        String givenData = getTestInfo().stepData;
        getTestInfo().stepData = givenData.contains("Given") ? "And " + preCondition : "Given " + preCondition;
        addStep();
    }


    public static void when(String action) {
        String whenData = getTestInfo().stepData;
        getTestInfo().stepData = whenData.contains("When") ? "And " + action : "When " + action;
        addStep();
    }

    public static void And(String action) {
        getTestInfo().stepData = "And " + action;
        addStep();
    }

    public static void then(String result) {
        String thenData = getTestInfo().stepData;
        getTestInfo().stepData = thenData.contains("Then") ? "And " + result : "Then " + result;
        addStep();
    }

    public static void step(String step) {
        getTestInfo().subSteps.add(new SubStep());
        getLastSubStepEntry().setSubStep(step);
        getLastSubStepEntry().setStatus(getTestInfo().status);
        addStep();
    }

    public static void evidence(String description, String evidence) {
        if (getTestInfo().subSteps.isEmpty()) {
            getTestInfo().subSteps.add(new SubStep());
            getLastSubStepEntry().setSubStep("Test Evidence: ");
        }

        if (null != description) {
            getLastSubStepEntry().setAttachments(description, evidence == null ? "NULL" : evidence);
            getLastSubStepEntry().setStatus(getTestInfo().status);
            addStep();
        }
    }

    public static SubStep getLastSubStepEntry(){
        return getTestInfo().subSteps.get(getTestInfo().subSteps.size() - 1);
    }

    public static void addIssueLink(String name, String issueLink) {
        Allure.issue(name, issueLink);
    }

    public static void remove(Status passOrFail) {
        getTestInfo().status = passOrFail;
        getTestInfo().stepData = "NULL";
        addStep();
    }

    private static TestInfo getTestInfo() {
        return testInfo.get();
    }

    public static void addStep() {
        if (!getTestInfo().stepData.equals(getTestInfo().previousStepData)) {
            if (!getTestInfo().subSteps.isEmpty() && getTestInfo().status == Status.PASSED) {
                Allure.step(getTestInfo().previousStepData, () -> {
                    getTestInfo().subSteps.forEach(c -> {
                        if (!c.getAttachments().isEmpty()) {
                            Allure.step(c.getSubStep(), () -> {
                                c.getAttachments().forEach(Allure::addAttachment);
                            });
                        } else {
                            Allure.step(c.getSubStep(), c.getStatus());
                        }
                    });
                });
                getTestInfo().subSteps.clear();
            } else if (!getTestInfo().previousStepData.equals("NULL")) {
                Allure.step(getTestInfo().previousStepData, getTestInfo().status);
            }
        }
        getTestInfo().previousStepData = getTestInfo().stepData;
    }

}

class SubStep {
    private String subStep;
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private final Map<String, String> attachments = new LinkedHashMap<>();

    public String getSubStep() {
        return subStep;
    }

    public void setSubStep(String subStep) {
        this.subStep = subStep;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(String description, String value) {
        attachments.put(description, value);
    }
}

