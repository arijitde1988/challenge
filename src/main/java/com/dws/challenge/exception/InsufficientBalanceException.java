package com.dws.challenge.exception;


/**
 * Runtime Exception Used to throw for insufficient account balance
 * @author Arijit De
 */ 
public class InsufficientBalanceException extends RuntimeException {

  public InsufficientBalanceException(String message) {
    super(message);
  }
}
