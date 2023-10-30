package org.pageflow.domain.book.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.pageflow.base.entity.BaseEntity;
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page extends BaseEntity {

    private String content;

    private int pageNumber;

}
