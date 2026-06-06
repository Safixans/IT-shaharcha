package com.itshaharcha.assessment.scoring;

/**
 * IELTS Listening raw→band conversion (out of 40), the commonly published Academic table.
 * Reading single-passage units don't band (raw + % only); only full 40-question Listening does.
 */
public final class BandTable {

    private BandTable() {
    }

    public static double listening(int rawCorrect) {
        int r = Math.max(0, rawCorrect);
        if (r >= 39) return 9.0;
        if (r >= 37) return 8.5;
        if (r >= 35) return 8.0;
        if (r >= 32) return 7.5;
        if (r >= 30) return 7.0;
        if (r >= 26) return 6.5;
        if (r >= 23) return 6.0;
        if (r >= 18) return 5.5;
        if (r >= 16) return 5.0;
        if (r >= 13) return 4.5;
        if (r >= 10) return 4.0;
        if (r >= 7) return 3.5;
        if (r >= 5) return 3.0;
        if (r >= 3) return 2.5;
        return r >= 1 ? 2.0 : 0.0;
    }
}
