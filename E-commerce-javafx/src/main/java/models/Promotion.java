package models;


import java.time.LocalDate;

public class Promotion {
    private int promotionId;
    private String code;
    private double discountPercentage;
    private LocalDate validFrom;
    private LocalDate validTo;

    public Promotion(int promotionId, String code, double discountPercentage, LocalDate validFrom, LocalDate validTo) {
        this.promotionId = promotionId;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }


    public String getCode() {
        return code;
    }


    public double getDiscountPercentage() {
        return discountPercentage;
    }


    public LocalDate getValidFrom() {
        return validFrom;
    }


    public LocalDate getValidTo() {
        return validTo;
    }


    public double getDiscount() {
        return discountPercentage;
    }
}
