package com.library.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Demo defaults; tighten for production via configuration.
 */
@ConfigurationProperties(prefix = "app.library")
@Getter
@Setter
public class LibraryProperties {

	private int maxActiveLoans = 5;
	private int loanDays = 14;
}
