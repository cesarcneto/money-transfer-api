package io.cesarcneto.moneytransfer.account.repository;

import io.cesarcneto.moneytransfer.account.model.Account;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    @SqlUpdate("INSERT INTO accounts (id, version, balance) VALUES (RANDOM_UUID(), RANDOM_UUID(), :balance)")
    @GetGeneratedKeys({"id", "version"})
    @RegisterConstructorMapper(Account.class)
    UUID save(@BindBean Account account);

    @SqlQuery("SELECT * FROM accounts WHERE id = ?")
    @RegisterConstructorMapper(Account.class)
    Optional<Account> findById(UUID id);

    @SqlQuery("SELECT * FROM accounts WHERE id IN (<accountIds>) ORDER BY id ASC FOR UPDATE")
    @RegisterConstructorMapper(Account.class)
    List<Account> getAccountsById(@BindList List<UUID> accountIds);

    @SqlBatch("UPDATE accounts SET balance = :balance, version = RANDOM_UUID() WHERE id = :id AND version = :version")
    int[] updateAccountsBalances(@BindBean List<Account> accountList);
}
