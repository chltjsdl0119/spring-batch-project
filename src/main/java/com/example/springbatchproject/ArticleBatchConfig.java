package com.example.springbatchproject;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ArticleBatchConfig {

    @Bean
    JdbcCursorItemReader<Article> articleReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Article>()
                .name("articleReader")
                .dataSource(dataSource)
                .sql("""
                        select id, title
                        from article
                        where status = 'PENDING'
                        order by id
                        """)
                .rowMapper((rs, rowNum) -> new Article(
                        rs.getLong("id"),
                        rs.getString("title")
                ))
                .build();
    }

    @Bean
    ItemProcessor<Article, ProcessedArticle> articleProcessor() {
        return article -> new ProcessedArticle(
                article.id(),
                article.title().trim()
        );
    }

    @Bean
    JdbcBatchItemWriter<ProcessedArticle> articleWriter(
            DataSource dataSource
    ) {
        return new JdbcBatchItemWriterBuilder<ProcessedArticle>()
                .dataSource(dataSource)
                .sql("""
                        insert into processed_article (
                            article_id,
                            title
                        )
                        values (
                            :articleId,
                            :title
                        )
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    Step articleStep(
            JobRepository jobRepository,
            JdbcCursorItemReader<Article> articleReader,
            ItemProcessor<Article, ProcessedArticle> articleProcessor,
            JdbcBatchItemWriter<ProcessedArticle> articleWriter
    ) {
        return new StepBuilder("articleStep", jobRepository)
                .<Article, ProcessedArticle>chunk(10)
                .reader(articleReader)
                .processor(articleProcessor)
                .writer(articleWriter)
                .build();
    }

    @Bean
    Job articleJob(
            JobRepository jobRepository,
            Step articleStep
    ) {
        return new JobBuilder("articleJob", jobRepository)
                .start(articleStep)
                .build();
    }
}