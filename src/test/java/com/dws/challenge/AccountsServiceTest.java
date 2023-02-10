package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	void addAccount() {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	void addAccount_failsOnDuplicateId() {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}

	/**
	 * Test method which checks failed account transfer for insufficient balance.
	 * @author Arijit De
	 * */
	@Test
	void account_transfer_failed() {
		Account fromAccount = new Account("Id-123");
		fromAccount.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-345");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(fromAccount);
		assertThat(this.accountsService.getAccount("Id-345")).isEqualTo(toAccount);

		AccountTransfer accountTransfer = new AccountTransfer(fromAccount.getAccountId(), toAccount.getAccountId(),
				new BigDecimal(1500));
		try {
			this.accountsService.transferAmount(accountTransfer);
			fail("Should have failed due to insufficient negative balance in from account");
		} catch (InsufficientBalanceException ibe) {
			assertThat(ibe.getMessage())
					.isEqualTo("Insufficient Balance Account id " + fromAccount.getAccountId() + "!!!");
		}
	}

}
