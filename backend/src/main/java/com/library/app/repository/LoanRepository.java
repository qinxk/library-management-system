package com.library.app.repository;

import com.library.app.domain.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select l from Loan l
			join fetch l.book
			join fetch l.reader
			where l.id = :id
			""")
	Optional<Loan> findByIdForReturn(@Param("id") Long id);

	@Query("""
			select distinct l from Loan l
			join fetch l.book
			join fetch l.reader
			order by l.borrowedAt desc
			""")
	List<Loan> findAllWithReaderAndBookOrderByBorrowedAtDesc();
}
