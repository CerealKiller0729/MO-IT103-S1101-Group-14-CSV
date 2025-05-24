package com.mycompany.motorph;

public class WithholdingTax extends Calculation {
    private double tax;
    private final double taxableIncome;
    private final Grosswage grosswage;

    public WithholdingTax(Grosswage grosswage, double taxableIncome) {
        if (grosswage == null) {
            throw new IllegalArgumentException("Grosswage cannot be null");
        }
        this.grosswage = grosswage;
        this.taxableIncome = taxableIncome;
    }

    @Override
    public double calculate() {
        // Updated Philippine tax brackets (2024)
        if (taxableIncome <= 20832) {
            tax = 0;
        } else if (taxableIncome <= 33333) {
            tax = (taxableIncome - 20832) * 0.20;
        } else if (taxableIncome <= 66667) {
            tax = 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166667) {
            tax = 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666667) {
            tax = 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            tax = 200833.33 + (taxableIncome - 666667) * 0.35;
        }
        return tax;
    }

    // Getters
    public double getTax() {
        return tax;
    }

    public double getTaxableIncome() {
        return taxableIncome;
    }

    public Grosswage getGrosswage() {
        return grosswage;
    }
}