package com.library.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 512)
	private String title;

	@Column(nullable = false, length = 256)
	private String author;

	@Column(length = 32)
	private String isbn;

	@Column(length = 128)
	private String category;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false)
	private int totalCopies;

	@Column(nullable = false)
	private int availableCopies;

	@Version
	private Long version;
}
