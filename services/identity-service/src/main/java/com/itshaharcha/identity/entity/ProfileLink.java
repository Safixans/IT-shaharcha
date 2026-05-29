package com.itshaharcha.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** External profile link (github, linkedin, ...). Stored as an ordered element collection. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProfileLink {

    @Column(name = "label", nullable = false, length = 80)
    private String label;

    @Column(name = "url", nullable = false, length = 512)
    private String url;
}
