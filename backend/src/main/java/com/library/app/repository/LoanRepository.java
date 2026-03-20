package com.library.app.repository;

import com.library.app.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	boolean existsByBookIdAndReturnedAtIsNull(Long bookId);

	boolean existsByReader_IdAndBook_IdAndReturnedAtIsNull(Long readerId, Long bookId);

	long countByReader_IdAndReturnedAtIsNull(Long readerId);

	@Query("""
			select distinct l from Loan l
			join fetch l.book
			where l.reader.id = :readerId
			order by l.borrowedAt desc
			""")
	List<Loan> findByReader_IdWithBookFetched(@Param("readerId") Long readerId);
}
