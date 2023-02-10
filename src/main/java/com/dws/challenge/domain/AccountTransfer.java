package com.dws.challenge.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Used as Request Body for transferring amount from from-account to to-account
 * @author Arijit De
 */ 
@Data
public class AccountTransfer implements Serializable {

	private static final long serialVersionUID = 3049178681393574082L;

	/**
     * From account Id
     * Not Null
     * @author Arijit De
     */ 
	@NotNull
	@NotEmpty
	private final String fromAccountId;

	/**
     * To account Id
     * Not Null
     * @author Arijit De
     */ 
	@NotNull
	@NotEmpty
	private final String toAccountId;

	/**
     * Transfer amount 
     * Greater than 0 positive value
     * @author Arijit De
     */
	@NotNull
	@Min(value = 1, message = "Initial balance must be positive.")
	private BigDecimal balance;

	/**
     * AccountTransfer object used to create/tag JSON property
     * @author Arijit De
     */
	@JsonCreator
	public AccountTransfer(@JsonProperty("fromAccountId") String fromAccountId,
			@JsonProperty("toAccountId") String toAccountId, @JsonProperty("balance") BigDecimal balance) {
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.balance = balance;
	}
}
