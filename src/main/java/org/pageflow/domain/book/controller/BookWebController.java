package org.pageflow.domain.book.controller;

import lombok.RequiredArgsConstructor;
import org.pageflow.base.annotation.SecuredBookId;
import org.pageflow.base.request.AlertType;
import org.pageflow.base.request.Rq;
import org.pageflow.domain.book.constants.BookStatus;
import org.pageflow.domain.book.entity.Book;
import org.pageflow.domain.book.model.summary.BookSummary;
import org.pageflow.domain.book.model.summary.Outline;
import org.pageflow.domain.book.service.BookService;
import org.pageflow.domain.book.service.BookWriteService;
import org.pageflow.domain.user.service.AccountService;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Transactional
public class BookWebController {

    private final Rq rq;
    private final BookService bookService;
    private final AccountService accountService;
    private final BookWriteService bookWriteService;

    
    @GetMapping("/books")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "sort", defaultValue = "createdDate") String sortOption) {
        Slice<BookSummary> paging = this.bookService.getList(page, kw, sortOption);

        model.addAttribute("paging",paging);
        model.addAttribute("kw", kw);
        model.addAttribute("sort", sortOption);
        return "/user/book/cards";
    }
    
    
    @GetMapping("/books/{bookId}/details")
        public String details(@PathVariable Long bookId, Model model) {
            Outline outline = bookService.getOutline(bookId);
            model.addAttribute("outline", outline);
            return "/user/book/details";
    }
    
    
    /**
     * 나의 책 페이지
     */
    @GetMapping("/account/books")
    @PreAuthorize("isAuthenticated()")
    public String listOfAccount(Model model){
        List<BookSummary> bookSummaries = bookService.getBookSummariesByProfileId(rq.getProfile().getId());
        model.addAttribute("books", bookSummaries);
        return "/user/book/user-books";
    }
    
    
    /**
     * @return 새로운 책을 생성하고 write 페이지로 넘겨버린다.
     */
    @GetMapping("/books/create-new")
    @PreAuthorize("isAuthenticated()")
    public String createNewBookForm() {
        Book newBook = bookWriteService.createBlankBook(rq.getProfile());
        return rq.alert(AlertType.SUCCESS, "새로운 책을 만들었습니다. <br> " + rq.getUserSession().getNickname() + "님의 멋진 책을 기대하고 있겠습니다!", String.format("/write/%d", newBook.getId()));
    }
    
    /**
     * @return react book write form page
     */
    @GetMapping("/write/{bookId}/**")
    @PreAuthorize("isAuthenticated()")
    public String writeForm(@SecuredBookId @PathVariable Long bookId, Model model) {
        
        Book book = bookService.repoFindBookById(bookId);
        if(book.getStatus().equals(BookStatus.REVIEWING) || book.getStatus().equals(BookStatus.PUBLISHED) || book.getStatus().equals(BookStatus.REVIEW_REQUESTED)){
            return rq.alert(AlertType.ERROR, String.format("해당 책은 현재 %s 상태입니다. 수정하시려면 상태를 변경해야합니다.", book.getStatus().statusText), "/account/books");
        }
        
        return "forward:/react/build/book_write/index.html";
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADMIN METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * 검수 요청된 책 리스트 페이지
     */
    @GetMapping("/admin/review-books")
    public String reviewList(Model model) {
        List<BookSummary> books = bookService.repoFindBooksByStatuses(Set.of(BookStatus.REVIEW_REQUESTED, BookStatus.REVIEWING));
        model.addAttribute("books", books);
        return "/admin/book/review-requested-list";
    }
    
    
    /**
     * 검수 요청된 책 상세 리뷰 페이지
     */
    @GetMapping("/admin/review-books/{bookId}")
    public String reviewDetails(
            @SecuredBookId(adminOnly = true) @PathVariable Long bookId,
            Model model
    ) {
        Book book = bookService.repoFindBookById(bookId);
        model.addAttribute("book", book);
        return "/admin/book/review-requested-details";
    }

}
