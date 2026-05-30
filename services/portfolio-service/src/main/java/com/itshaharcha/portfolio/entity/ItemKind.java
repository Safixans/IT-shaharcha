package com.itshaharcha.portfolio.entity;

/**
 * Free-form portfolio item category (spec ItemKind). Lowercase constants match the
 * wire enum exactly, so Jackson (de)serializes them without extra mapping.
 */
public enum ItemKind {
    project,
    award,
    publication,
    experience,
    skill,
    link
}
