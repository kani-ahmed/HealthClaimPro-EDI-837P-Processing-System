package com.billing.webapp;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class BillingHistoryDTO {
    /*
        * This class is used to serialize the data that is sent to the frontend to share saved data and history of billing for each user.
        * It is used in the LegacyController class to handle the GET request that is sent from the frontend.
        * The data that is sent to the frontend is serialized into a BillingHistoryDTO object.
        * The BillingHistoryDTO object is then sent to the frontend.
     */
    private Double totalClaimCharge;
    private Set<LocalDate> skippedDates;
    private String firstName;
    private String lastName;
    private List<DateRangeDTO> dateRanges;

    public Double getTotalClaimCharge() {
        return totalClaimCharge;
    }

    public void setTotalClaimCharge(Double totalClaimCharge) {
        this.totalClaimCharge = totalClaimCharge;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<DateRangeDTO> getDateRanges() {
        return dateRanges;
    }

    public void setDateRanges(List<DateRangeDTO> dateRanges) {
        this.dateRanges = dateRanges;
    }

    public Set<LocalDate> getSkippedDates() {
        return skippedDates;
    }

    public void setSkippedDates(Set<LocalDate> skippedDates) {
        this.skippedDates = skippedDates;
    }

    // Constructors, Getters, and Setters
}

class DateRangeDTO {
    private String startDate;
    private String endDate;

    // Constructor
    public DateRangeDTO(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getter for startDate
    public String getStartDate() {
        return startDate;
    }

    // Getter for endDate
    public String getEndDate() {
        return endDate;
    }
}