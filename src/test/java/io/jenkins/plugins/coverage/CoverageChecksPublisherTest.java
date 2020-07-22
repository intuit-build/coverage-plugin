package io.jenkins.plugins.coverage;

import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksDetails.ChecksDetailsBuilder;
import io.jenkins.plugins.checks.api.ChecksOutput.ChecksOutputBuilder;
import io.jenkins.plugins.checks.api.ChecksStatus;
import io.jenkins.plugins.coverage.targets.*;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoverageChecksPublisherTest {
    @Test
    public void shouldConstructChecksDetailsWithLineAndMethodCoverage() {
        CoverageResult result = createCoverageResult((float)0.6, (float)0.4);
        when(result.getPreviousResult()).thenReturn(null);

        CoverageAction action = mock(CoverageAction.class);
        when(action.getResult()).thenReturn(result);
        when(action.getAbsoluteUrl()).thenReturn("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage");

        ChecksDetails expectedDetails = new ChecksDetailsBuilder()
                .withName("Code Coverage")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage")
                .withOutput(new ChecksOutputBuilder()
                        .withTitle("Line coverage of 60%.")
                        .withSummary("")
                        .withText("## Conditional\n* :white_check_mark: Coverage: 40%\n"
                                + "## Line\n* :white_check_mark: Coverage: 60%\n")
                        .build())
                .build();

        assertThat(new CoverageChecksPublisher(action).extractChecksDetails())
                .usingRecursiveComparison()
                .isEqualTo(expectedDetails);
    }

    @Test
    public void shouldConstructChecksDetailsWithIncreasedLineCoverageDecreasedConditionalCoverage() {
        CoverageResult result = createCoverageResult((float)0.6, (float)0.4);
        CoverageResult lastResult = createCoverageResult((float)0.5, (float)0.5);
        when(result.getPreviousResult()).thenReturn(lastResult);

        CoverageAction action = mock(CoverageAction.class);
        when(action.getResult()).thenReturn(result);
        when(action.getAbsoluteUrl()).thenReturn("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage");

        ChecksDetails expectedDetails = new ChecksDetailsBuilder()
                .withName("Code Coverage")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage")
                .withOutput(new ChecksOutputBuilder()
                        .withTitle("Line coverage of 60% is greater than the last successful build (50%).")
                        .withSummary("")
                        .withText("## Conditional\n* :white_check_mark: Coverage: 40%\n* :arrow_down: Trend: 10%\n"
                                + "## Line\n* :white_check_mark: Coverage: 60%\n* :arrow_up: Trend: 10%\n")
                        .build())
                .build();

        assertThat(new CoverageChecksPublisher(action).extractChecksDetails())
                .usingRecursiveComparison()
                .isEqualTo(expectedDetails);
    }

    @Test
    public void shouldConstructChecksDetailsWithDecreasedLineCoverageAndIncreasedConditionalCoverage() {
        CoverageResult result = createCoverageResult((float)0.4, (float)0.6);
        CoverageResult lastResult = createCoverageResult((float)0.5, (float)0.5);
        when(result.getPreviousResult()).thenReturn(lastResult);

        CoverageAction action = mock(CoverageAction.class);
        when(action.getResult()).thenReturn(result);
        when(action.getAbsoluteUrl()).thenReturn("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage");

        ChecksDetails expectedDetails = new ChecksDetailsBuilder()
                .withName("Code Coverage")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage")
                .withOutput(new ChecksOutputBuilder()
                        .withTitle("Line coverage of 40% is less than the last successful build (50%).")
                        .withSummary("")
                        .withText("## Conditional\n* :white_check_mark: Coverage: 60%\n* :arrow_up: Trend: 10%\n"
                                + "## Line\n* :white_check_mark: Coverage: 40%\n* :arrow_down: Trend: 10%\n")
                        .build())
                .build();

        assertThat(new CoverageChecksPublisher(action).extractChecksDetails())
                .usingRecursiveComparison()
                .isEqualTo(expectedDetails);
    }

    @Test
    public void shouldConstructChecksDetailsWithUnchangedLineAndConditionalCoverage() {
        CoverageResult result = createCoverageResult((float)0.6, (float)0.4);
        CoverageResult lastResult = createCoverageResult((float)0.6, (float)0.4);
        when(result.getPreviousResult()).thenReturn(lastResult);

        CoverageAction action = mock(CoverageAction.class);
        when(action.getResult()).thenReturn(result);
        when(action.getAbsoluteUrl()).thenReturn("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage");

        ChecksDetails expectedDetails = new ChecksDetailsBuilder()
                .withName("Code Coverage")
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(ChecksConclusion.SUCCESS)
                .withDetailsURL("http://127.0.0.1:8080/job/pipeline-coding-style/job/PR-3/49/coverage")
                .withOutput(new ChecksOutputBuilder()
                        .withTitle("Line coverage of 60% is the same as the last successful build (60%).")
                        .withSummary("")
                        .withText("## Conditional\n* :white_check_mark: Coverage: 40%\n* :arrow_right: Trend: 0%\n"
                                + "## Line\n* :white_check_mark: Coverage: 60%\n* :arrow_right: Trend: 0%\n")
                        .build())
                .build();

        assertThat(new CoverageChecksPublisher(action).extractChecksDetails())
                .usingRecursiveComparison()
                .isEqualTo(expectedDetails);
    }

    private CoverageResult createCoverageResult(final float lineCoverage, final float conditionCoverage) {
        CoverageResult result = mock(CoverageResult.class);

        Map<CoverageElement, Ratio> ratios = new HashMap<>(3);
        ratios.put(CoverageElement.LINE, Ratio.create(lineCoverage, 1));
        ratios.put(CoverageElement.CONDITIONAL, Ratio.create(conditionCoverage, 1));

        when(result.getResults()).thenReturn(ratios);
        when(result.getCoverage(CoverageElement.LINE)).thenReturn(Ratio.create(lineCoverage, 1));
        when(result.getCoverageTrends()).thenReturn(null);

        return result;
    }
}
