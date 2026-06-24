package com.ssafy.enjoytrip.batch;

import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingBackfillReport;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingTargetRegion;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingBackfillService;
import com.ssafy.enjoytrip.batch.embedding.gms.GmsEmbeddingProperties;
import com.ssafy.enjoytrip.batch.embedding.gms.GmsKeywordExpansionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AttractionEmbeddingBackfillJobConfiguration {
    private final AttractionEmbeddingBackfillService backfillService;
    private final AttractionEmbeddingTargetRegionsProperties targetRegionsProperties;
    private final AttractionEmbeddingTargetRegionValidator targetRegionValidator;
    private final AttractionEmbeddingBatchProperties batchProperties;
    private final GmsEmbeddingProperties gmsEmbeddingProperties;
    private final GmsKeywordExpansionProperties keywordExpansionProperties;

    @Bean
    Job attractionEmbeddingBackfillJob(JobRepository jobRepository, Step attractionEmbeddingBackfillStep) {
        return new JobBuilder("attractionEmbeddingBackfillJob", jobRepository)
                .start(attractionEmbeddingBackfillStep)
                .build();
    }

    @Bean
    Step attractionEmbeddingBackfillStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("attractionEmbeddingBackfillStep", jobRepository)
                .tasklet(attractionEmbeddingBackfillTasklet(), transactionManager)
                .build();
    }

    @Bean
    Tasklet attractionEmbeddingBackfillTasklet() {
        return (contribution, chunkContext) -> {
            var parameters = chunkContext.getStepContext().getJobParameters();
            String sourceVersion = stringParameter(parameters.get("sourceVersion"));
            boolean dryRun = booleanParameter(parameters.get("dryRun"), false);
            int limit = intParameter(parameters.get("limit"), 0);
            List<AttractionEmbeddingTargetRegion> targetRegions = targetRegionsProperties.toTargetRegions();
            targetRegionValidator.validate(targetRegions);
            if (!dryRun) {
                keywordExpansionProperties.assertLiveReady();
                gmsEmbeddingProperties.assertLiveReady();
            }
            if (batchProperties.isFailOnOutsideTargetEmbeddings()) {
                long outsideTarget = backfillService.countEmbeddingsOutsideTargetRegions(targetRegions);
                if (outsideTarget > 0) {
                    throw new IllegalStateException(
                            "정식 대상 지역 밖의 관광지 임베딩 행이 "
                                    + outsideTarget
                                    + "개 발견되었습니다."
                    );
                }
            }
            AttractionEmbeddingBackfillReport report = backfillService.backfill(
                    targetRegions,
                    sourceVersion,
                    dryRun,
                    limit
            );
            var executionContext = contribution.getStepExecution().getExecutionContext();
            executionContext.putInt("selectedCount", report.selectedCount());
            executionContext.putInt("embeddedCount", report.embeddedCount());
            executionContext.putInt("skippedCount", report.skippedCount());
            executionContext.putInt("failedCount", report.failedCount());
            log.info("Attraction embedding backfill finished: {}", report);
            return RepeatStatus.FINISHED;
        };
    }

    private static String stringParameter(Object value) {
        String normalized = value == null ? "" : value.toString().strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("sourceVersion 작업 파라미터가 필요합니다.");
        }
        return normalized;
    }

    private static boolean booleanParameter(Object value, boolean fallback) {
        return value == null ? fallback : Boolean.parseBoolean(value.toString());
    }

    private static int intParameter(Object value, int fallback) {
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.toString());
    }
}
