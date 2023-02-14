package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.exception.AccountBusyException;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;

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
	 * Test method which checks concurrent account transfer acquiring the lock on
	 * top of account no.
	 * 
	 * @author Arijit De
	 */
	@Test
	void concurrent_multi_account_transfer() {

		Account fromAccount = new Account("Id-A001");
		fromAccount.setBalance(new BigDecimal(1000000));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-A002");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-A001")).isEqualTo(fromAccount);
		assertThat(this.accountsService.getAccount("Id-A002")).isEqualTo(toAccount);

		List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).parallelStream().forEach(i -> {

			if (i == 3) {
				Account frmAccount = new Account("Id-A004");
				frmAccount.setBalance(new BigDecimal(1000));
				this.accountsService.createAccount(frmAccount);

				Account tAccount = new Account("Id-A005");
				tAccount.setBalance(new BigDecimal(500));
				this.accountsService.createAccount(tAccount);

				assertThat(this.accountsService.getAccount("Id-A004")).isEqualTo(frmAccount);
				assertThat(this.accountsService.getAccount("Id-A005")).isEqualTo(tAccount);

				System.out.println("MultiThread execution " + i + ") From Acc - " + frmAccount.getAccountId()
						+ " To Acc - " + tAccount.getAccountId());
				AccountTransfer acctrnsf = new AccountTransfer(frmAccount.getAccountId(), tAccount.getAccountId(),
						new BigDecimal(750));
				this.accountsService.transferAmount(acctrnsf);

			} else if (i == 7) {
				Account frmAccount = new Account("Id-A007");
				frmAccount.setBalance(new BigDecimal(2000));
				this.accountsService.createAccount(frmAccount);

				Account tAccount = new Account("Id-A008");
				tAccount.setBalance(new BigDecimal(1500));
				this.accountsService.createAccount(tAccount);

				assertThat(this.accountsService.getAccount("Id-A007")).isEqualTo(frmAccount);
				assertThat(this.accountsService.getAccount("Id-A008")).isEqualTo(tAccount);

				System.out.println("MultiThread execution " + i + ") From Acc - " + frmAccount.getAccountId()
						+ " To Acc - " + tAccount.getAccountId());
				AccountTransfer acctrnsf = new AccountTransfer(frmAccount.getAccountId(), tAccount.getAccountId(),
						new BigDecimal(750));
				this.accountsService.transferAmount(acctrnsf);

			} else {
				System.out.println("MultiThread execution " + i + ") From Acc - " + fromAccount.getAccountId()
						+ " To Acc - " + toAccount.getAccountId());
				AccountTransfer accountTransfer = new AccountTransfer(fromAccount.getAccountId(),
						toAccount.getAccountId(), new BigDecimal(1500));
				this.accountsService.transferAmount(accountTransfer);
			}

		});

	}

	/**
	 * Test method which checks failed account transfer for insufficient balance.
	 * 
	 * @author Arijit De
	 */
	@Test
	void account_transfer_failed_insufficient_balance() {
		Account fromAccount = new Account("Id-C001");
		fromAccount.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-C002");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-C001")).isEqualTo(fromAccount);
		assertThat(this.accountsService.getAccount("Id-C002")).isEqualTo(toAccount);

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

	/**
	 * Test method which checks failed account transfer for non-existence account .
	 * 
	 * @author Arijit De
	 */
	@Test
	void account_transfer_failed_nonexistence_account() {

		AccountTransfer accountTransfer = new AccountTransfer("A1-001", "A2-002", new BigDecimal(1500));
		try {
			this.accountsService.transferAmount(accountTransfer);
			fail("Should have failed due to account not exists from account and to account");
		} catch (AccountNotFoundException anfe) {
			assertThat(anfe.getMessage()).isEqualTo("Account not found !!!");
		}
	}

	/**
	 * Test method which checks concurrent account transfer acquiring the lock on
	 * top of account no for cross transfer.
	 * 
	 * @author Arijit De
	 */
	@Test
	void concurrent_multi_cross_account_transfer() {

		Account fromAccount = new Account("Id-A0010");
		fromAccount.setBalance(new BigDecimal(100000));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-A0020");
		toAccount.setBalance(new BigDecimal(100000));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-A0010")).isEqualTo(fromAccount);
		assertThat(this.accountsService.getAccount("Id-A0020")).isEqualTo(toAccount);

		List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).parallelStream().forEach(i -> {
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (i % 2 == 0) {
				System.out.println("MultiThread execution " + i + ") From Acc - " + toAccount.getAccountId()
						+ " To Acc - " + fromAccount.getAccountId());
				AccountTransfer accountTransfer = new AccountTransfer(toAccount.getAccountId(),
						fromAccount.getAccountId(), new BigDecimal(1500));
				try {
					this.accountsService.transferAmount(accountTransfer);
				} catch (AccountBusyException abe) {
					assertThat(abe.getMessage()).isEqualTo("Transaction is processing either on From Acc or To Acc. Please wait and try after sometime.");
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("MultiThread execution " + i + ") From Acc - " + fromAccount.getAccountId()
						+ " To Acc - " + toAccount.getAccountId());
				AccountTransfer accountTransfer = new AccountTransfer(fromAccount.getAccountId(),
						toAccount.getAccountId(), new BigDecimal(1500));
				try {
					this.accountsService.transferAmount(accountTransfer);
				} catch (AccountBusyException abe) {
					assertThat(abe.getMessage()).isEqualTo("Transaction is processing either on From Acc or To Acc. Please wait and try after sometime.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

	}

}
