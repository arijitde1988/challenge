package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AccountTransfer;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.TransferFailureException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  /**
   * This is a service which transfers amount from from-account to to-account by executing the repository method.
   * @param accountTransfer  It accepts AccountTransfer object
   * @throws InsufficientBalanceException if the from account balance is negative after subtraction of transfer amount.
   * @throws TransferFailureException if transfer computation fails
   * @return true if transfer successful else false.
   * @author Arijit De
   */
  public boolean transferAmount(AccountTransfer accountTransfer) {
	    return this.accountsRepository.transferAmount(accountTransfer);
  }
}
