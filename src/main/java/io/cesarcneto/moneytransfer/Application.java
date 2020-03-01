package io.cesarcneto.moneytransfer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.cesarcneto.moneytransfer.account.controller.AccountController;
import io.cesarcneto.moneytransfer.account.mapper.AccountMapper;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.cesarcneto.moneytransfer.transfer.controller.TransferController;
import io.cesarcneto.moneytransfer.transfer.exception.InsufficientBalanceInAccountException;
import io.cesarcneto.moneytransfer.transfer.exception.UpdateBalanceException;
import io.cesarcneto.moneytransfer.transfer.mapper.TransferMapper;
import io.cesarcneto.moneytransfer.transfer.service.TransferService;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;

import javax.sql.DataSource;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.cesarcneto.moneytransfer.shared.service.ApplicationPropertiesService.getProperty;
import static io.javalin.apibuilder.ApiBuilder.*;
import static org.eclipse.jetty.http.HttpStatus.*;

public class Application {

    public static final String ACCOUNTS_ENDPOINT = "accounts";
    public static final String TRANSFERS_ENDPOINT = "transfers";

    public static void main(String[] args) {

        int serverPort  = Integer.parseInt(getProperty("server.port"));

        DataSource dataSource = getDataSource();
        migrateDatabase(dataSource);
        Jdbi jdbi = initializeJdbi(dataSource);

        AccountService accountService = createAccountService(jdbi);
        AccountController accountController = createAccountController(accountService);
        TransferController transferController = createTransferController(jdbi, accountService);

        // 2019-02-13T10:00:00.000Z
        // TODO - It seems that it's not working - isolate it later and check with Javalin folks
        // Defaulting to parse it in actual check predicate
        // JavalinValidation.register(Instant.class, Instant::parse);

        JavalinValidation.register(UUID.class, UUID::fromString);
        Javalin app = Javalin.create()
                .exception(Exception.class, (ex, ctx) -> ctx.status(INTERNAL_SERVER_ERROR_500).json(ex.getMessage()))
                .exception(DateTimeParseException.class, (ex, ctx) -> ctx.status(BAD_REQUEST_400).json(ex.getMessage()))
                .exception(InsufficientBalanceInAccountException.class,
                        (ex, ctx) -> ctx.status(BAD_REQUEST_400).json(ex.getMessage()))
                .exception(UpdateBalanceException.class,
                        (ex, ctx) -> ctx.status(BAD_REQUEST_400).json(ex.getMessage()))
                .exception(NoSuchElementException.class, (ex, ctx) -> ctx.status(NOT_FOUND_404).json(ex.getMessage()))
                .error(NOT_FOUND_404, ctx -> ctx.json("Not found"))
                .start(serverPort);

        app.routes(() -> {
            path(ACCOUNTS_ENDPOINT, () -> {
                path("/:accountId", () -> {
                    get(accountController::getByAccountId);
                });
                post(accountController::createAccount);
            });

            path(TRANSFERS_ENDPOINT, () -> post(transferController::performTransfer));
        });

    }

    @NotNull
    private static TransferController createTransferController(Jdbi jdbi, AccountService accountService) {
        TransferService transferService = new TransferService(jdbi, accountService);
        TransferMapper transferMapper = Mappers.getMapper(TransferMapper.class);
        return new TransferController(transferService, transferMapper);
    }

    @NotNull
    private static AccountService createAccountService(Jdbi jdbi) {
        AccountRepository accountRepository = jdbi.onDemand(AccountRepository.class);
        return new AccountService(accountRepository);
    }

    @NotNull
    private static AccountController createAccountController(AccountService accountService) {
        AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
        return new AccountController(accountService, accountMapper);
    }

    @NotNull
    private static Jdbi initializeJdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new H2DatabasePlugin());
        return jdbi;
    }

    @NotNull
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
