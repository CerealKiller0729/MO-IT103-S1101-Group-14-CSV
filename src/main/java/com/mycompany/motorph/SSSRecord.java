/*
 * Class representing an SSS deduction record with a compensation range and contribution amount.
 */
package com.mycompany.motorph;

public class SSSRecord {
    private final String compensationRange; // Compensation range (e.g., "0-1000")
    private final double contribution; // Contribution amount for the range

    /**
     * Constructor for SSSRecord.
     * @param compensationRange The compensation range.
     * @param contribution The contribution amount.
     */
    public SSSRecord(String compensationRange, double contribution) {
        if (compensationRange == null || compensationRange.trim().isEmpty()) {
            throw new IllegalArgumentException("Compensation range cannot be null or empty.");
        }
        if (contribution < 0) {
            throw new IllegalArgumentException("Contribution must be non-negative.");
        }

        this.compensationRange = compensationRange;
        this.contribution = contribution;
    }

    /**
     * Returns the compensation range.
     * @return The compensation range.
     */
    public String getCompensationRange() {
        return compensationRange;
    }

    /**
     * Returns the contribution amount.
     * @return The contribution amount.
     */
    public double getContribution() {
        return contribution;
    }

    @Override
    public String toString() {
        return "SSSRecord{" +
                "compensationRange='" + compensationRange + '\'' +
                ", contribution=" + contribution +
                '}';
    }
}
