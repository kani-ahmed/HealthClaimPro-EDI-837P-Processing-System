package com.billing.webapp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.DayOfWeek;
import java.util.Map;

@Entity
@Table(name = "date_ranges")
@CrossOrigin
public class DateRange {
    /*
        * This is the entity for the date_ranges table
        * It is used to map the table to the application and vice versa
        * The @Entity annotation is used to indicate that this is an entity class
        * The @Table annotation is used to indicate that this class maps to a table
        * The @Id annotation is used to indicate that the id field is the primary key
        * The @GeneratedValue annotation is used to indicate that the id field is auto generated
        * The @ManyToOne annotation is used to indicate that this is a many-to-one relationship with the LegacyData class
        * The @JoinColumn annotation is used to indicate that the legacyData field is the foreign key
        * The @JsonBackReference annotation is used to prevent infinite recursion when serializing the object to JSON
        * The @ElementCollection annotation is used to indicate that the serviceHours field is a collection of elements
        * In general, the purpose of this class is to map the date_ranges table to the application and vice versa
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Double mondayHours;
    private Double tuesdayHours;
    private Double wednesdayHours;
    private Double thursdayHours;
    private Double fridayHours;
    private Double saturdayHours;
    private Double sundayHours;
    private String startDate;
    @ElementCollection
    private Map<DayOfWeek, Double> serviceHours; // A map of service days to hours
    private String endDate;
    @ManyToOne
    @JoinColumn(name = "legacy_data_id", nullable = false)
    @JsonBackReference
    private LegacyData legacyData;

    public Double getMondayHours() {
        return mondayHours;
    }

    public void setMondayHours(Double mondayHours) {
        this.mondayHours = mondayHours;
    }

    public Double getTuesdayHours() {
        return tuesdayHours;
    }

    public void setTuesdayHours(Double tuesdayHours) {
        this.tuesdayHours = tuesdayHours;
    }

    public Double getWednesdayHours() {
        return wednesdayHours;
    }

    public void setWednesdayHours(Double wednesdayHours) {
        this.wednesdayHours = wednesdayHours;
    }

    public Double getThursdayHours() {
        return thursdayHours;
    }

    public void setThursdayHours(Double thursdayHours) {
        this.thursdayHours = thursdayHours;
    }

    public Double getFridayHours() {
        return fridayHours;
    }

    public void setFridayHours(Double fridayHours) {
        this.fridayHours = fridayHours;
    }

    public Double getSaturdayHours() {
        return saturdayHours;
    }

    public void setSaturdayHours(Double saturdayHours) {
        this.saturdayHours = saturdayHours;
    }

    public Double getSundayHours() {
        return sundayHours;
    }

    public void setSundayHours(Double sundayHours) {
        this.sundayHours = sundayHours;
    }

    public void setLegacyData(LegacyData legacyData) {
        this.legacyData = legacyData;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Map<DayOfWeek, Double> getServiceHours() {
        return serviceHours;
    }

    public void setServiceHours(Map<DayOfWeek, Double> serviceHours) {
        this.serviceHours = serviceHours;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    // Standard getters and setters
}

