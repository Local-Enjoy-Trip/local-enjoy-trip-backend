package com.ssafy.enjoytrip.batch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.boot.DefaultApplicationArguments;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttractionEmbeddingBackfillJobLauncherTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-05T00:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void buildsJobParametersFromSpringBootOptionArguments() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--sourceVersion=tourapi-2026-06-05",
                "--dryRun=true",
                "--limit=10"
        );

        JobParameters parameters = AttractionEmbeddingBackfillJobLauncher.buildJobParameters(
                args,
                FIXED_CLOCK
        );

        assertThat(parameters.getString("sourceVersion")).isEqualTo("tourapi-2026-06-05");
        assertThat(parameters.getString("dryRun")).isEqualTo("true");
        assertThat(parameters.getLong("limit")).isEqualTo(10L);
        assertThat(parameters.getLong("run.id")).isEqualTo(FIXED_CLOCK.millis());
    }

    @Test
    void buildsJobParametersFromRawBatchStyleArguments() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "sourceVersion=tourapi-2026-06-05",
                "dryRun=false",
                "limit=3"
        );

        JobParameters parameters = AttractionEmbeddingBackfillJobLauncher.buildJobParameters(
                args,
                FIXED_CLOCK
        );

        assertThat(parameters.getString("sourceVersion")).isEqualTo("tourapi-2026-06-05");
        assertThat(parameters.getString("dryRun")).isEqualTo("false");
        assertThat(parameters.getLong("limit")).isEqualTo(3L);
    }

    @Test
    void dryRunFlagWithoutValueMeansTrue() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--sourceVersion=tourapi-2026-06-05",
                "--dryRun"
        );

        JobParameters parameters = AttractionEmbeddingBackfillJobLauncher.buildJobParameters(
                args,
                FIXED_CLOCK
        );

        assertThat(parameters.getString("dryRun")).isEqualTo("true");
        assertThat(parameters.getLong("limit")).isZero();
    }

    @Test
    void sourceVersionIsRequiredBeforeOpeningBatchExecution() {
        DefaultApplicationArguments args = new DefaultApplicationArguments("--dryRun=true");

        assertThatThrownBy(() -> AttractionEmbeddingBackfillJobLauncher.buildJobParameters(args, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceVersion job parameter is required");
    }

    @Test
    void negativeLimitIsRejectedBeforeOpeningBatchExecution() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--sourceVersion=tourapi-2026-06-05",
                "--limit=-1"
        );

        assertThatThrownBy(() -> AttractionEmbeddingBackfillJobLauncher.buildJobParameters(args, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit job parameter");
    }
}
