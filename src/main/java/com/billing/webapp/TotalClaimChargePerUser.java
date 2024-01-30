package com.billing.webapp;

import jakarta.persistence.*;

@Entity
@Table(name = "total_claim_charge_per_user")
public class TotalClaimChargePerUser {
    //This class represents the total claim charge per user. It is a child of LegacyData and DateRange classes.
    // It has a many-to-one relationship with both of those classes.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Double totalClaimCharge;

    @ManyToOne
    @JoinColumn(name = "legacy_data_id", nullable = false)
    private LegacyData legacyData;

    @ManyToOne
    @JoinColumn(name = "date_range_id", nullable = false)
    private DateRange dateRange;

    // Constructors, getters, and setters

    public Double getTotalClaimCharge() {
        return totalClaimCharge;
    }

    public void setTotalClaimCharge(Double totalClaimCharge) {
        this.totalClaimCharge = totalClaimCharge;
    }

    public LegacyData getLegacyData() {
        return legacyData;
    }

    public void setLegacyData(LegacyData legacyData) {
        this.legacyData = legacyData;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }
}