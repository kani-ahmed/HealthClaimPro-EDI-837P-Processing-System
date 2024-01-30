package com.billing.webapp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity // This tells Hibernate to make a table out of this class
/*
 * @Table(name = "legacy_data") // This tells Hibernate to name the table as "legacy_data" instead of "legacyData"
 * uniqueConstraint is used to make sure that the idNumber is unique in the table and that no two users have the same idNumber
 */
@Table(name = "legacy_data", uniqueConstraints = {@UniqueConstraint(columnNames = {"idNumber"})})
@CrossOrigin
public class LegacyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // This is the primary key for the table

    private String idNumber; // This is the unique identifier for each user
    @ElementCollection
    @CollectionTable(name = "skipped_dates", joinColumns = @JoinColumn(name = "legacy_data_id"))
    @Column(name = "date")
    private Set<LocalDate> skippedDates = new HashSet<>(); // This is a set of service days that no service was provided (e.g. due to lack of staffing)

    /*
     *This is a one-to-many relationship with the DateRange class (one user can have many date ranges)
     * The cascade type is set to ALL so that any changes to the user will be reflected in the date ranges as well
     * The mappedBy attribute is set to "legacyData" because the LegacyData class is the owner of the relationship
     * The JsonManagedReference annotation is used to prevent infinite recursion when serializing the object to JSON
     * The JsonManagedReference annotation is used in the LegacyData class (parent side of the relationship) (used on the side of the relationship that you want to serialize)
     * The JsonBackReference annotation is used in the DateRange class (child side of the relationship) (used on the side of the relationship that you want to ignore)
     */
    @OneToMany(mappedBy = "legacyData", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DateRange> dateRanges;
    @OneToMany(mappedBy = "legacyData", cascade = CascadeType.ALL)
    private List<TotalClaimChargePerUser> totalClaimCharges; // This is a list of total claim charges for each user (The amount billed to the payer for each user)
    private String firstName; // This is the first name of the user
    private String lastName; // This is the last name of the user
    private String address; // This is the address of the user
    private String birthday; // Assuming format YYYYMMDD as per placeholder
    private String zipcode; // This is the zipcode of the user
    private double rate;
    @Transient
    private List<String> serviceDays; // Consider how to persist lists
    @Transient
    private List<Double> hoursPerDay; // Consider how to persist lists
    @Transient
    private List<String> datesToSkip; // Consider how to persist lists

    public Set<LocalDate> getSkippedDates() {
        return skippedDates;
    }

    public void setSkippedDates(Set<LocalDate> skippedDates) {
        this.skippedDates = skippedDates;
    }

    public List<DateRange> getDateRanges() {
        return dateRanges;
    }

    public void setDateRanges(List<DateRange> dateRanges) {
        this.dateRanges = dateRanges;
    }

    public List<TotalClaimChargePerUser> getTotalClaimCharges() {
        return totalClaimCharges;
    }

    public void setTotalClaimCharges(List<TotalClaimChargePerUser> totalClaimCharges) {
        this.totalClaimCharges = totalClaimCharges;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public List<String> getServiceDays() {
        return serviceDays;
    }

    public void setServiceDays(List<String> serviceDays) {
        this.serviceDays = serviceDays;
    }

    public List<Double> getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(List<Double> hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public List<String> getDatesToSkip() {
        return datesToSkip;
    }

    public void setDatesToSkip(List<String> datesToSkip) {
        this.datesToSkip = datesToSkip;
    }
}