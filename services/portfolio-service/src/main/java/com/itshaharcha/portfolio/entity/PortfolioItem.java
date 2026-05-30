package com.itshaharcha.portfolio.entity;

import com.itshaharcha.common.entity.BaseEntity;
import com.itshaharcha.portfolio.convert.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "portfolio_items")
public class PortfolioItem extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemKind kind;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    private String url;

    @Column(name = "file_id")
    private UUID fileId;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> tags = new ArrayList<>();
}
