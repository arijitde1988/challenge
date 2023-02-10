package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.TransferFailureException;

import lombok.Synchronized;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	/**
	 * This is synchronized method which transfers amount from from-account to
	 * to-account.
	 * 
	 * @param accountTransfer it accepts AccountTransfer object Its also checks from
	 *                        account negative balance and add that non-negative
	 *                        amount to to account balance.
	 * @throws InsufficientBalanceException if the from account balance is negative
	 *                                      after subtraction of transfer amount.
	 * @throws TransferFailureException     if transfer computation fails
	 * @return true if transfer successful else false.
	 * @author Arijit De
	 */
	@Override
	@Synchronized
	public boolean transferAmount(AccountTransfer accountTransfer) {
		
		boolean isTransfered = false;
		Account toAccount = accounts.get(accountTransfer.getToAccountId());
		Account frmAccount = accounts.get(accountTransfer.getFromAccountId());
		BigDecimal remAmnt = frmAccount.getBalance().subtract(accountTransfer.getBalance());
		if (remAmnt.compareTo(BigDecimal.ZERO) == -1) {
			throw new InsufficientBalanceException(
					"Insufficient Balance Account id " + frmAccount.getAccountId() + "!!!");
		} else {

			try {
				frmAccount = accounts.computeIfPresent(frmAccount.getAccountId(), (accountId, account) -> {
					account.setBalance(account.getBalance().subtract(accountTransfer.getBalance()));
					return account;
				});

				toAccount = accounts.computeIfPresent(toAccount.getAccountId(), (accountId, account) -> {
					account.setBalance(account.getBalance().add(accountTransfer.getBalance()));
					return account;
				});

				isTransfered = true;

			} catch (Exception e) {
				throw new TransferFailureException("Failed to transfer balance from account id - "
						+ frmAccount.getAccountId() + " to account id - " + toAccount.getAccountId() + "!!!");
			}

		}

		return isTransfered;
	}

}
