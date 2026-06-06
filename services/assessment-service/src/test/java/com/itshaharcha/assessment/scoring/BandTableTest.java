package com.itshaharcha.assessment.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BandTableTest {

    @Test
    void mapsRawCorrectToBand() {
        assertThat(BandTable.listening(40)).isEqualTo(9.0);
        assertThat(BandTable.listening(39)).isEqualTo(9.0);
        assertThat(BandTable.listening(37)).isEqualTo(8.5);
        assertThat(BandTable.listening(35)).isEqualTo(8.0);
        assertThat(BandTable.listening(30)).isEqualTo(7.0);
        assertThat(BandTable.listening(23)).isEqualTo(6.0);
        assertThat(BandTable.listening(16)).isEqualTo(5.0);
        assertThat(BandTable.listening(1)).isEqualTo(2.0);
        assertThat(BandTable.listening(0)).isEqualTo(0.0);
    }

    @Test
    void clampsNegativeToZero() {
        assertThat(BandTable.listening(-5)).isEqualTo(0.0);
    }
}
