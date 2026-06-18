package com.project_x.core.paginationhelper;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public class PaginationAdapters {

    // PAGINATION UTILITY USED BY SPRING FRAMEWORK
    private static final long DEFAULT_BUTTON_SIZE = 5; // typical UI

    /** Deterministic default sort to avoid duplications on timestamp ties */
    private static final Sort DEFAULT_SORT =
            Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

    private static final Sort RECENT_FIRST_SORT =
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));;

    /** Cap to prevent abuse*/
    private static final int MAX_PAGE_SIZE = 35;
    private static final int DEFAULT_PAGE = 1;

   public static long resolvePage(Long requestedPage){
       long p = (requestedPage == null) ? DEFAULT_PAGE: requestedPage;
       return  Math.max(1, p);
   }

    /** Normalize page size (1.MAX_PAGE_SIZE) */
    public static long resolvePageSize(Long requestedSize) {
        long s = (requestedSize == null) ? MAX_PAGE_SIZE:requestedSize;
        if (s <= 0) return MAX_PAGE_SIZE;
        return Math.min(s, MAX_PAGE_SIZE);
    }

    /** API page is 1-based; Spring is 0-based */
    /*when you want oldest records first.*/
    public static Pageable createPageRequest(Long page, Long pageSize) {
        long oneBasedPage  = resolvePage(page);
        long zeroBasedPage = oneBasedPage -1 ;
        long size = resolvePageSize(pageSize);

        return PageRequest.of((int) zeroBasedPage, (int) size, DEFAULT_SORT);
    }

//    Use when  you want newest records first.
    public static Pageable createPageRequestWithRecentFirstsSortOrder(Long page, Long pageSize) {
        long oneBasedPage  = resolvePage(page);
        long zeroBasedPage = oneBasedPage -1 ;
        long size = resolvePageSize(pageSize);

        return PageRequest.of((int) zeroBasedPage, (int) size, RECENT_FIRST_SORT);
    }

    // in PaginationAdapters
    public static Pageable createUnsortedPageRequest(Long page, Long pageSize) {
        long oneBased = resolvePage(page);
        long zeroBased = oneBased - 1;
        long size = resolvePageSize(pageSize);
        return PageRequest.of((int) zeroBased, (int) size, Sort.unsorted());
    }

    /** Build a BasePaginationRequest from Spring's Page (your util stores total *records* in totalPage) */
    public static BasePaginationRequest fromSpringPage(Page<?> page) {
        return BasePaginationRequest.builder()
                .pageSize(page.getSize())
                .page(page.getNumber())              // Spring 0-based
                .totalPage(page.getTotalElements())  // total records
                .buttonSize(page.getSize())
                .build();
    }



    public static PaginationMeta toMeta(Page<?> page) {
        long currentPage = page.getNumber() + 1; // 1-based
        long pageSize = page.getSize();
        long totalRecords = page.getTotalElements();
        long totalPages = page.getTotalPages();
        long currentCount = page.getNumberOfElements();

        boolean empty = page.isEmpty();

        long from = 0;
        long to = 0;
        if (!empty) {
            from = (currentPage - 1) * pageSize + 1;
            to = from + currentCount - 1;
        }

        boolean hasNext = page.hasNext();
        boolean hasPrev = page.hasPrevious();

        Long nextPage = hasNext ? currentPage + 1 : null;
        Long prevPage = hasPrev ? currentPage - 1 : null;

        long lastShowing = computedLastPage(currentPage, totalPages, DEFAULT_BUTTON_SIZE);

        return PaginationMeta.builder()
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalRecordCount(totalRecords)
                .totalPages(totalPages)
                .currentCount(currentCount)
                .fromRecord(from)
                .toRecord(to)
                .hasNext(hasNext)
                .hasPrevious(hasPrev)
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .empty(empty)
                .nextPage(nextPage)
                .previousPage(prevPage)
                .sort(String.valueOf(page.getSort()))
                .lastShowingPage(lastShowing)
                .build();
    }

    // Basic windowing: show up to buttonSize pages from current
    private static long computedLastPage(long currentPage, long totalPages, long buttonSize) {
        if (totalPages <= 0) return 0;
        long last = currentPage + buttonSize - 1;
        return Math.min(last, totalPages);
    }
}
