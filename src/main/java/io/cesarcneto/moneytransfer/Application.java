package io.cesarcneto.moneytransfer;

import io.cesarcneto.moneytransfer.account.controller.AccountController;
import io.cesarcneto.moneytransfer.account.mapper.AccountMapper;
import io.cesarcneto.moneytransfer.account.repository.AccountRepository;
import io.cesarcneto.moneytransfer.account.service.AccountService;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;

import java.util.NoSuchElementException;

import static io.javalin.apibuilder.ApiBuilder.*;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;

@RequiredArgsConstructor
public class Application {

    private final AccountController accountController;

    private Javalin app;

    public void start(int httpPort) {

        app = Javalin.create()
                .exception(Exception.class, (ex, ctx) -> ex.printStackTrace())
                .exception(NoSuchElementException.class, (ex, ctx) -> ctx.status(NOT_FOUND_404).json("Not found"))
                .error(NOT_FOUND_404, ctx -> ctx.json("Not found"))
                .start(httpPort);

        app.routes(() -> {
            path("/accounts", () -> {
                path("/:accountId", () -> {
                    get(accountController::getByAccountId);
                });
                post(accountController::createAccount);
            });
        });
    }

    public void stop() {
        app.stop();
    }

    public static void main(String[] args) {
        AccountRepository accountRepository = new AccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
        AccountController accountController = new AccountController(accountService, accountMapper);
        new Application(accountController).start(8080);
    }

}
