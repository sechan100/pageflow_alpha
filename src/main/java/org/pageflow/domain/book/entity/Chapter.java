package org.pageflow.domain.book.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.pageflow.base.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)//접근 수준. 상속관계에 있는 클래스에서만 생성자에 접근 가능
//@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "sortPriority"}))
@DynamicUpdate
public class Chapter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore // 양방향 객체 참조로 인한 직렬화 무한루프에 빠지는 것을 막는다.
    private Book book;

    private String title;

    @Min(1)
    @Column(nullable = false)
    private Integer sortPriority;

    // Page가 Chapter를 이동할 수도 있기 때문에, Chapter에서 Page의 고아객체를 관리하지 않는다.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "chapter")
    private List<Page> pages = new ArrayList<>();

    @PrePersist
    private void autoSetSortPriority() {
        if (this.sortPriority == null) {
            List<Chapter> chapters = this.book.getChapters();

            int lastChapterSortPriority = !chapters.isEmpty() ? chapters.get(chapters.size() - 1).getSortPriority() : 0;

            // 새로운 sortPriority는 "(마지막sortPriority) + (10000)"
            int newSortPriority = lastChapterSortPriority + 10000;
            this.sortPriority = newSortPriority;
        }
    }


    // AllArgsConstructor: Pages 값을 초기화하기위해서 하드코딩
    public Chapter(Book book, String title, Integer sortPriority, List<Page> pages) {
        this.book = book;
        this.title = title;
        this.sortPriority = sortPriority;
        this.pages = Objects.requireNonNullElseGet(pages, ArrayList::new);
    }
}