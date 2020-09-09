package in.projecteka.devservice;

import com.zaxxer.hikari.HikariDataSource;
import in.projecteka.devservice.support.SupportRequestService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
class LiquibaseDataSourceProperties {
    private String url;
    private String password;
    private String driverClassName;
    private String username;
}

@Configuration
public class LiquibaseDataSourceConfiguration {
    @Autowired
    private in.projecteka.devservice.LiquibaseDataSourceProperties liquibaseDataSourceProperties;
    private static final Logger logger = LoggerFactory.getLogger(LiquibaseDataSourceConfiguration.class);

    @LiquibaseDataSource
    @Bean
    public DataSource liquibaseDataSource() {
        DataSource ds = DataSourceBuilder.create()
                .username(liquibaseDataSourceProperties.getUsername())
                .password(liquibaseDataSourceProperties.getPassword())
                .url(liquibaseDataSourceProperties.getUrl())
                .driverClassName(liquibaseDataSourceProperties.getDriverClassName())
                .build();
        if (ds instanceof HikariDataSource) {
            ((HikariDataSource) ds).setMaximumPoolSize(1);
            ((HikariDataSource) ds).setPoolName("Liquibase Pool");
        }
        logger.info("Initialized a datasource for {}", liquibaseDataSourceProperties.getUrl());
        return ds;
    }
}