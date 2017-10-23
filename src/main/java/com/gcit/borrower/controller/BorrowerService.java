/**
 * @Author Shuo Zhang <shuo_zhang@gcitsolutions.com>
 * @Date Sep 28, 2017
 */
package com.gcit.borrower.controller;


import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gcit.borrower.dao.*;
import com.gcit.borrower.domain.*;

@RestController
@RequestMapping(value = "/Borrower")
public class BorrowerService {
	// =================================================================================================================
	// SPRING DAOs
	// =================================================================================================================

	@Autowired
	BookDAO bookDao;
	
	@Autowired
	AuthorDAO authorDao;
	
	@Autowired
	GenreDAO genreDao;

	@Autowired
	BookCopiesDAO bookCopiesDao;

	@Autowired
	BookLoansDAO bookLoansDao;

	@Autowired
	LibraryBranchDAO libraryBranchDao;

	@Autowired
	BorrowerDAO borrowerDao;
	
	
	// =================================================================================================================
	// POST
	// =================================================================================================================
	
	/*
	 * INSERT OR UPDATE (check-out or check-in) a book loan based on if dateOut attribute is NULL
	 */
	@Transactional
	@RequestMapping(value = "/updateBookLoan_borrower",
					method = RequestMethod.POST, consumes = "application/json")
	public void updateBookLoan(@RequestBody BookLoans bookLoan) throws SQLException {
		Book 	   book 	   = bookDao.readOneBook(bookLoan.getBook().getBookId());
		BookCopies bookCopy    = new BookCopies();
		Integer    noOfCopies  = book.getBranchCopies().get(bookLoan.getLibraryBranch().getBranchId());
		bookCopy.setBook(book);
		bookCopy.setLibraryBranch(bookLoan.getLibraryBranch());
		if (bookLoan.getDateOut() == null) {
			bookCopy.setNoOfCopies(noOfCopies - 1);
			bookLoansDao.addBookLoan(bookLoan);
			bookCopiesDao.updateBookCopies(bookCopy);
		} else {
			bookCopy.setNoOfCopies(noOfCopies + 1);
			bookLoansDao.updateBookLoan(bookLoan);
			bookCopiesDao.updateBookCopies(bookCopy);
		}
	}
	
	// =================================================================================================================
	// GET
	// =================================================================================================================
	
	/*
	 * READ ALL books' information from tbl_book
	 */
	@RequestMapping(value = "/readBooks_borrower/{searchString}/{pageNo}",
					method = RequestMethod.GET, produces = {"application/json", "application/xml"})
	public List<Book> readBooks(@PathVariable String searchString, @PathVariable Integer pageNo) throws SQLException {
		List<Book> books = bookDao.readBooks(null, pageNo);
		for (Book book : books) {
			book.setAuthors(authorDao.readAuthorsByBook(book));
			book.setGenres(genreDao.readGenresByBook(book));
			book.setBorrowers(borrowerDao.readBorrowersByBook(book));
			book.setBranchCopies(bookCopiesDao.readBookCopiesByBook(book));
		}
		return books;
	}

	/*
	 * READ ALL library branches' information from tbl_library_branch
	 */
	@RequestMapping(value = "/readLibraryBranches_borrower/{searchString}/{pageNo}",
					method = RequestMethod.GET, produces = {"application/json", "application/xml"})
	public List<LibraryBranch> readLibraryBranches(@PathVariable String searchString, @PathVariable Integer pageNo) throws SQLException {
		return libraryBranchDao.readLibraryBranches(searchString, pageNo);
	}
	
	/*
	 * READ ONE borrower's information given cardNo
	 */
	@RequestMapping(value = "/readOneBorrower_borrower/{cardNo}",
					method = RequestMethod.GET, produces = {"application/json", "application/xml"})
	public Borrower readOneBorrower(@PathVariable Integer cardNo) throws SQLException {
		Borrower borrower = borrowerDao.readOneBorrower(cardNo);
		borrower.setBookLoans(bookLoansDao.readBookLoanByBorrower(borrower));
		return borrower;
	}
	
	/*
	 * READ ONE book loan's information given cardNo and branchId
	 */
	@RequestMapping(value = "/readOneBookLoan_borrower/{cardNo}/{branchId}",
					method = RequestMethod.GET, produces = {"application/json", "application/xml"})
	public List<BookLoans> readOneBookLoan(@PathVariable Integer cardNo, @PathVariable Integer branchId) throws SQLException {
		return bookLoansDao.readOneBookLoan(cardNo, branchId);
	}
	
}
