package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.exception.AccountBusyException;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.TransferFailureException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.util.LockUtilByAccountNo;

import lombok.Getter;
import lombok.Synchronized;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Getter
	private final LockUtilByAccountNo lockUtilByAccountNo;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, LockUtilByAccountNo lockUtilByAccountNo) {
		this.accountsRepository = accountsRepository;
		this.lockUtilByAccountNo = lockUtilByAccountNo;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * This is a service which transfers amount from from-account to to-account by
	 * executing the repository method.
	 * 
	 * @param accountTransfer It accepts AccountTransfer object
	 * @throws InsufficientBalanceException if the from account balance is negative
	 *                                      after subtraction of transfer amount.
	 * @throws TransferFailureException     if transfer computation fails
	 * @return true if transfer successful else false.
	 * @author Arijit De
	 * @throws InterruptedException
	 */

	public boolean transferAmount(AccountTransfer accountTransfer) {

		boolean isTransfered = false;
		Account toAccount = accountsRepository.getAccount(accountTransfer.getToAccountId());
		Account frmAccount = accountsRepository.getAccount(accountTransfer.getFromAccountId());

		if (toAccount == null || frmAccount == null) {
			throw new AccountNotFoundException("Account not found !!!");
		}

		System.out.println("Before Lock From Acc - " + frmAccount.getAccountId() + " To Acc - "
				+ toAccount.getAccountId() + " Thread Name - " + Thread.currentThread().getName());

		if (!lockUtilByAccountNo.isLockAquired(frmAccount.getAccountId())
				&& !lockUtilByAccountNo.isLockAquired(toAccount.getAccountId()) 
				&& lockUtilByAccountNo.tryLock(frmAccount.getAccountId())
				&& lockUtilByAccountNo.tryLock(toAccount.getAccountId()) 
		) {

			synchronized (lockUtilByAccountNo.getLockedObject(frmAccount.getAccountId())) {
				synchronized (lockUtilByAccountNo.getLockedObject(toAccount.getAccountId())) {

					BigDecimal remAmnt = frmAccount.getBalance().subtract(accountTransfer.getBalance());
					if (remAmnt.compareTo(BigDecimal.ZERO) == -1) {
						throw new InsufficientBalanceException(
								"Insufficient Balance Account id " + frmAccount.getAccountId() + "!!!");
					} else {

						try {
 
							frmAccount.setBalance(remAmnt);
							frmAccount = accountsRepository.updateAccount(frmAccount);

							toAccount.setBalance(toAccount.getBalance().add(accountTransfer.getBalance()));
							toAccount = accountsRepository.updateAccount(toAccount);
							System.err.println("Updated amount From Acc - " + frmAccount.getAccountId() + " is = "
									+ frmAccount.getBalance().doubleValue() + " and To Acc - "
									+ toAccount.getAccountId() + " is = " + toAccount.getBalance().doubleValue()
									+ " Time - " + System.currentTimeMillis() + " Thread Name - "
									+ Thread.currentThread().getName());

							isTransfered = true;

						} catch (Exception e) {

							if (frmAccount != null && toAccount != null) {
								throw new TransferFailureException(
										"Failed to transfer balance from account id - " + frmAccount.getAccountId()
												+ " to account id - " + toAccount.getAccountId() + "!!!");
							}

						} finally {

							if (frmAccount != null && toAccount != null) {
								lockUtilByAccountNo.unlock(frmAccount.getAccountId());
								lockUtilByAccountNo.unlock(toAccount.getAccountId());
								System.out.println("Lock Released From Acc - " + frmAccount.getAccountId()
										+ " To Acc - " + toAccount.getAccountId() + " Thread Name - "
										+ Thread.currentThread().getName());
							}

						}

					}
				}
			}

		} else {
			throw new AccountBusyException("Transaction is processing either on From Acc or To Acc. Please wait and try after sometime.");
		}

		return isTransfered;
	}
}
