package com.dws.challenge.exception;


/**
 * Runtime Exception Used to throw for transfer is going under the same account 
 * @author Arijit De
 */ 
public class AccountBusyException extends RuntimeException {

  public AccountBusyException(String message) {
    super(message);
  }
}
