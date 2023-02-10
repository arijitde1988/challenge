package com.dws.challenge.exception;


/**
 * Runtime Exception Used to throw for transfer error
 * @author Arijit De
 */ 
public class TransferFailureException extends RuntimeException {

  public TransferFailureException(String message) {
    super(message);
  }
}
