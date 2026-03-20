package com.library.app.repository;

import com.library.app.domain.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

	/**
	 * Serialize inventory updates under a DB row lock for single-instance demos.
	 * (Cluster-wide consistency would need stronger coordination than {@code synchronized}.)
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select b from Book b where b.id = :id")
	Optional<Book> findByIdForUpdate(@Param("id") Long id);

	@Query("""
			select b from Book b
			where lower(b.title) like lower(concat('%', :keyword, '%'))
			   or lower(b.author) like lower(concat('%', :keyword, '%'))
			   or lower(coalesce(b.isbn, '')) like lower(concat('%', :keyword, '%'))
			""")
	Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
