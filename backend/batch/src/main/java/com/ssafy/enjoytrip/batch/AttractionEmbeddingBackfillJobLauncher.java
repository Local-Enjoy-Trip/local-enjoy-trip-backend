package com.ssafy.enjoytrip.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
class AttractionEmbeddingBackfillJobLauncher implements ApplicationRunner, ExitCodeGenerator {
    private static final String SOURCE_VERSION_PARAMETER = "sourceVersion";
    private static final String DRY_RUN_PARAMETER = "dryRun";
    private static final String LIMIT_PARAMETER = "limit";
    private static final String RUN_ID_PARAMETER = "run.id";

    private final JobLauncher jobLauncher;
    private final Job attractionEmbeddingBackfillJob;

    private int exitCode;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            JobParameters jobParameters = buildJobParameters(args, Clock.systemUTC());
            log.info(
                    "Launching {} with parameters {}",
                    attractionEmbeddingBackfillJob.getName(),
                    jobParameters
            );

            JobExecution execution = jobLauncher.run(attractionEmbeddingBackfillJob, jobParameters);
            BatchStatus status = execution.getStatus();
            log.info("Finished {} executionId={} status={} exitStatus={}",
                    attractionEmbeddingBackfillJob.getName(),
                    execution.getId(),
                    status,
                    execution.getExitStatus());

            if (status != BatchStatus.COMPLETED) {
                exitCode = 1;
                throw new IllegalStateException("Batch job " + attractionEmbeddingBackfillJob.getName()
                        + " finished with status " + status
                        + " and exit status " + execution.getExitStatus());
            }
        } catch (Exception ex) {
            exitCode = 1;
            throw ex;
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    static JobParameters buildJobParameters(ApplicationArguments args, Clock clock) {
        String sourceVersion = requiredParameter(args, SOURCE_VERSION_PARAMETER);
        boolean dryRun = booleanParameter(args, DRY_RUN_PARAMETER, false);
        int limit = intParameter(args, LIMIT_PARAMETER, 0);
        if (limit < 0) {
            throw new IllegalArgumentException("limit job parameter must be greater than or equal to 0");
        }

        return new JobParametersBuilder()
                .addString(SOURCE_VERSION_PARAMETER, sourceVersion)
                .addString(DRY_RUN_PARAMETER, Boolean.toString(dryRun))
                .addLong(LIMIT_PARAMETER, (long) limit)
                .addLong(RUN_ID_PARAMETER, clock.millis())
                .toJobParameters();
    }

    private static String requiredParameter(ApplicationArguments args, String name) {
        return optionalParameter(args, name)
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(name + " job parameter is required. "
                        + "Example: --" + name + "=tourapi-2026-06-05"));
    }

    private static boolean booleanParameter(ApplicationArguments args, String name, boolean fallback) {
        return optionalParameter(args, name)
                .map(String::strip)
                .map(value -> value.isEmpty() || Boolean.parseBoolean(value))
                .orElse(fallback);
    }

    private static int intParameter(ApplicationArguments args, String name, int fallback) {
        return optionalParameter(args, name)
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(Integer::parseInt)
                .orElse(fallback);
    }

    private static Optional<String> optionalParameter(ApplicationArguments args, String name) {
        if (args.containsOption(name)) {
            List<String> values = args.getOptionValues(name);
            if (values == null || values.isEmpty()) {
                return Optional.of("");
            }
            return Optional.ofNullable(values.getFirst());
        }

        String rawPrefix = name + "=";
        String dashedPrefix = "--" + rawPrefix;
        for (String sourceArg : args.getSourceArgs()) {
            if (sourceArg.startsWith(rawPrefix)) {
                return Optional.of(sourceArg.substring(rawPrefix.length()));
            }
            if (sourceArg.startsWith(dashedPrefix)) {
                return Optional.of(sourceArg.substring(dashedPrefix.length()));
            }
        }
        return Optional.empty();
    }
}
