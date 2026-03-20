package com.library.app.repository;

import com.library.app.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

	@Query("""
			select b from Book b
			where lower(b.title) like lower(concat('%', :keyword, '%'))
			   or lower(b.author) like lower(concat('%', :keyword, '%'))
			   or lower(coalesce(b.isbn, '')) like lower(concat('%', :keyword, '%'))
			""")
	Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
