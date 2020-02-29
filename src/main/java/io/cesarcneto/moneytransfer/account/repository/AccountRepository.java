package io.cesarcneto.moneytransfer.account.repository;

import io.cesarcneto.moneytransfer.account.model.Account;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    @SqlUpdate("insert into accounts (id, balance) values (RANDOM_UUID(), :balance)")
    @GetGeneratedKeys({"id"})
    @RegisterConstructorMapper(Account.class)
    UUID save(@BindBean Account account);

    @SqlQuery("select * from accounts where id = ?")
    @RegisterConstructorMapper(Account.class)
    Optional<Account> findById(UUID id);
}
