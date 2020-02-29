package io.cesarcneto.moneytransfer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.cesarcneto.moneytransfer.account.controller.AccountController;
import io.cesarcneto.moneytransfer.account.mapper.AccountMapper;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.cesarcneto.moneytransfer.shared.dto.ApiErrorResponseDto;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.mapstruct.factory.Mappers;

import javax.sql.DataSource;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.cesarcneto.moneytransfer.shared.service.ApplicationPropertiesService.getProperty;
import static io.javalin.apibuilder.ApiBuilder.*;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;

public class Application {

    public static void main(String[] args) {

        DataSource dataSource = getDataSource();
        migrateDatabase(dataSource);
        Jdbi jdbi = initializeJdbi(dataSource);

        AccountRepository accountRepository = jdbi.onDemand(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);
        AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
        AccountController accountController =
                new AccountController(accountService, accountMapper);

        int serverPort  = Integer.parseInt(getProperty("server.port"));

        JavalinValidation.register(UUID.class, UUID::fromString);
        Javalin app = Javalin.create()
                .exception(Exception.class, (ex, ctx) -> ex.printStackTrace())
                .exception(NoSuchElementException.class, (ex, ctx) -> ctx
                        .status(NOT_FOUND_404)
                        .json(ApiErrorResponseDto.builder().code(NOT_FOUND_404).message(ex.getMessage()).build())
                )
                .error(NOT_FOUND_404, ctx -> ctx.json("Not found"))
                .start(serverPort);

        app.routes(() -> {
            path("/accounts", () -> {
                path("/:accountId", () -> {
                    get(accountController::getByAccountId);
                });
                post(accountController::createAccount);
            });
        });

    }

    private static Jdbi initializeJdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new H2DatabasePlugin());
        return jdbi;
    }

    private static DataSource getDataSource() {
        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setJdbcUrl(getProperty("database.url"));
        poolConfig.setDriverClassName(getProperty("database.driver"));
        poolConfig.setUsername(getProperty("database.user"));
        poolConfig.setPassword(getProperty("database.password"));
        return new HikariDataSource(poolConfig);
    }

    private static void migrateDatabase(DataSource dataSource) {
        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
    }

}
