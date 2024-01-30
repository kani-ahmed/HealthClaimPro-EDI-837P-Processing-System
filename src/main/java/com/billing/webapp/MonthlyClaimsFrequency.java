//package com.example.demo;
//import jakarta.persistence.*;
//
//@Entity
//public class MonthlyClaimsFrequency {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    public LegacyData getLegacyData() {
//        return legacyData;
//    }
//
//    public void setLegacyData(LegacyData legacyData) {
//        this.legacyData = legacyData;
//    }
//
//    public int getMonth() {
//        return month;
//    }
//
//    public void setMonth(int month) {
//        this.month = month;
//    }
//
//    public int getYear() {
//        return year;
//    }
//
//    public void setYear(int year) {
//        this.year = year;
//    }
//
//    public int getClaimsCount() {
//        return claimsCount;
//    }
//
//    public void setClaimsCount(int claimsCount) {
//        this.claimsCount = claimsCount;
//    }
//
//    @ManyToOne
//    @JoinColumn(name = "legacy_data_id")
//    private LegacyData legacyData;
//
//    private int month;
//    private int year;
//    private int claimsCount;
//
//    // Constructors, getters, and setters
//}
