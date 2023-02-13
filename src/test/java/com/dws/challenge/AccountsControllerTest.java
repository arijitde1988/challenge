package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.util.List;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	/**
	 * Test method which checks successful transfer of amount between two accounts
	 * and also checks the updated amount.
	 * 
	 * @author Arijit De
	 */
	@Test
	void account_transfer() throws Exception {
		Account fromAccount = new Account("Id-123");
		fromAccount.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-345");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(fromAccount);
		assertThat(this.accountsService.getAccount("Id-345")).isEqualTo(toAccount);

		this.mockMvc
				.perform(post("/v1/accounts/transferAmount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-345\",\"balance\":1000}"))
				.andExpect(status().isAccepted());
		Account account = accountsService.getAccount("Id-345");
		assertThat(account.getAccountId()).isEqualTo("Id-345");
		assertThat(account.getBalance()).isEqualByComparingTo("1500");
	}

	/**
	 * Test method which checks account not exists.
	 * 
	 * @author Arijit De
	 */
	@Test
	void account_transfer_account_not_exists() throws Exception {

		Account toAccount = new Account("Id-345");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-345")).isEqualTo(toAccount);

		this.mockMvc
				.perform(post("/v1/accounts/transferAmount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-890\",\"toAccountId\":\"Id-345\",\"balance\":1000}"))
				.andExpect(status().isNotFound());
	}

	/**
	 * Test method which checks for insufficient balance.
	 * 
	 * @author Arijit De
	 */
	@Test
	void account_transfer_account_insufficient_balance() throws Exception {

		Account fromAccount = new Account("Id-123");
		fromAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-345");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(fromAccount);
		assertThat(this.accountsService.getAccount("Id-345")).isEqualTo(toAccount);

		this.mockMvc
				.perform(post("/v1/accounts/transferAmount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-345\",\"balance\":1500}"))
				.andExpect(status().isNotAcceptable());
	}

}
