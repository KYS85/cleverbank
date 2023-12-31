package com.vlasova.cleverbank.dao.impl.account;

import com.vlasova.cleverbank.dao.AbstractCrudDao;
import com.vlasova.cleverbank.dao.DaoInterface;
import com.vlasova.cleverbank.entity.accounte.Account;
import com.vlasova.cleverbank.exception.DataAccessException;
import com.vlasova.cleverbank.exception.MappingException;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@ApplicationScoped
public class AccountDao extends AbstractCrudDao<Account> implements DaoInterface<Long, Account> {
    private static final String SAVE = "INSERT INTO accounts (balance, is_active, bank_id, currency_id, customer_id, opening_date, type_id, number) VALUES (?,?,?,?,?,?,?,?)";
    private static final String UPDATE = "UPDATE accounts SET balance = ?, is_active = ? WHERE id = ?";
    protected String findByIdQuery = "select * FROM accounts WHERE id = ?";
    protected String deleteByIdQuery = "DELETE FROM accounts WHERE id = ?";
    protected String findAllQuery = "SELECT * FROM accounts ORDER BY id LIMIT ? OFFSET ?";

    @Override
    public Account save(Account account) throws DataAccessException {
        try (Connection connection = pool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE, RETURN_GENERATED_KEYS)) {
            preparedStatement.setBigDecimal(1, account.getBalance());
            preparedStatement.setBoolean(2, account.isActive());
            preparedStatement.setLong(3, account.getBank().getId());
            preparedStatement.setLong(4, account.getCurrency().getId());
            preparedStatement.setLong(5, account.getCustomer().getId());
            preparedStatement.setDate(6, new Date(account.getOpeningDate().getEpochSecond())); //TODO check!
            preparedStatement.setLong(7, account.getType().getId());
            preparedStatement.setString(8, account.getNumber());

            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                account.setId((long) resultSet.getInt(1));
            }
            return account;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean update(Account account) throws DataAccessException {
        try (Connection connection = pool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
            preparedStatement.setBigDecimal(1, account.getBalance());
            preparedStatement.setBoolean(2, account.isActive());
            preparedStatement.setLong(3, account.getId());

            return preparedStatement.executeUpdate() > 1;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    protected Account mapFromResultSet(ResultSet resultSet) {
        try {
            Account account = new Account();
            account.setBalance(resultSet.getBigDecimal("balance"));
            account.setActive(resultSet.getBoolean("is_active"));
            account.setOpeningDate(Instant.from(resultSet.getDate("opening_date").toInstant()));
            account.setNumber(resultSet.getString("number"));
            account.setId(resultSet.getLong("id"));

            return account;
        } catch (SQLException e) {
            throw new MappingException(e);
        }
    }
}
