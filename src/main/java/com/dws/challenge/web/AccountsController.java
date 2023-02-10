package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.TransferFailureException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;

	private final NotificationService notificationService;

	@Autowired
	public AccountsController(AccountsService accountsService, NotificationService notificationService) {
		this.accountsService = accountsService;
		this.notificationService = notificationService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	
	/**
	 * @RequestMapping Used to amount transfer
	 * @RequestBody accepts AccountTransfer object for transferring balance.
	 * @author Arijit De
	 */ 
	@RequestMapping(value = "/transferAmount", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> transferAmount(@RequestBody @Valid AccountTransfer accountTransfer) {
		log.info("Initiating account transfer {}", accountTransfer);
		try {
			if (this.accountsService.transferAmount(accountTransfer)) {
				notificationService.notifyAboutTransfer(accountsService.getAccount(accountTransfer.getFromAccountId()),
						"Dear User, Amount " + accountTransfer.getBalance() + " has been debited from account "
								+ accountTransfer.getFromAccountId());
				notificationService.notifyAboutTransfer(accountsService.getAccount(accountTransfer.getToAccountId()),
						"Dear User, Amount " + accountTransfer.getBalance() + " has been credited to account "
								+ accountTransfer.getToAccountId());
			} else {
				throw new TransferFailureException(
						"Failed to transfer balance from account id - " + accountTransfer.getFromAccountId()
								+ " to account id - " + accountTransfer.getToAccountId() + "!!!");
			}
		} catch (InsufficientBalanceException ibe) {
			return new ResponseEntity<>(ibe.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (TransferFailureException tfe) {
			return new ResponseEntity<>(tfe.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

}
