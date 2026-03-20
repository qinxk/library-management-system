package com.library.app.repository;

import com.library.app.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	boolean existsByBookIdAndReturnedAtIsNull(Long bookId);
}
