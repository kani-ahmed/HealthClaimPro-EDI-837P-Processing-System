package com.billing.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@CrossOrigin
public class LegacyRequest {
    /*
     *This class is used to represent the JSON request body from the frontend.
     * It is used to store the data from the request body and pass it to the LegacyService.
     * The @Scope annotation is used to make sure that each request has its own instance of this class.
     * The @ScopedProxyMode annotation is used to make sure that the @Scope annotation works.
     * The convertToHoursPerDayMap() method is used to convert the serviceDays and hoursPerDay lists into a map required by LegacyService.
     */

    // The logger is used to log error messages
    private static final Logger logger = LoggerFactory.getLogger(LegacyController.class);
    // The names of the properties must match the names of the properties in the JSON request body from the frontend
    private String firstName; //  The first name of the user
    private String lastName; // The last name of the user
    private String idNumber; // The unique identifier for each user
    private String address; // The address of the user
    private String birthday;  // The birthday of the user (e.g. 2021-01-01)
    private String zipcode; // The zipcode of the user (e.g. 12345)
    private String startDate; // The start date of the service (e.g. 2021-01-01)
    private String endDate; // The end date of the service (e.g. 2021-01-01)
    private double rate; // The rate of the service (e.g. 10.00)
    private List<String> serviceDays; // List of days like "MONDAY", "TUESDAY" etc.
    private List<Double> hoursPerDay; // Corresponding hours for each service day (e.g. 8.00)
    private List<String> datesToSkip; // List of dates to skip, format YYYY-MM-DD

    // Getters and setters for all properties

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public Map<DayOfWeek, Double> getHoursPerDay() {
        return convertToHoursPerDayMap();
    }

    public void setHoursPerDay(List<Double> hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    /*
     * This method converts the serviceDays and hoursPerDay lists into a map required by LegacyService.
     * It is used by the getHoursPerDay() method.
     * It is also used by the LegacyService to convert the LegacyRequest into a LegacyData object.
     * The @CrossOrigin annotation is used to allow the frontend to access this method.
     * The try-catch block is used to handle the case where the day is not valid or the lists are not of the same length.
     */
    @CrossOrigin
    public Map<DayOfWeek, Double> convertToHoursPerDayMap() {
        Map<DayOfWeek, Double> hoursMap = new HashMap<>(); // Create a new map to store the hours per day for each day of the week
        if (serviceDays != null && hoursPerDay != null) {
            // Loop through the serviceDays and hoursPerDay lists and add the corresponding values to the map
            for (int i = 0; i < serviceDays.size(); i++) {
                try {
                    // Convert the day to a DayOfWeek enum value and get the corresponding hours from the hoursPerDay list
                    DayOfWeek day = DayOfWeek.valueOf(serviceDays.get(i).toUpperCase());
                    // Set the hours for the day in the map to the corresponding value from the hoursPerDay list
                    Double hours = hoursPerDay.get(i);
                    // Add the day and hours to the map if the day is valid and the lists are of the same length
                    hoursMap.put(day, hours);
                } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                    // Handle the case where the day is not valid or the lists are not of the same length
                    //use logger to display error message
                    logger.error("Error converting service days and hours per day to map: " + e.getMessage());

                }
            }
        }
        return hoursMap; // Return the map with the hours per day for each day of the week (e.g. {MONDAY=8.0, TUESDAY=8.0})
    }

    public List<String> getDatesToSkip() {
        return datesToSkip;
    }

    public void setDatesToSkip(List<String> datesToSkip) {
        this.datesToSkip = datesToSkip;
    }

    // The toString() method is used to print the LegacyRequest object in a readable format for debugging purposes
    @Override
    public String toString() {
        return "LegacyRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", address='" + address + '\'' +
                ", birthday='" + birthday + '\'' +
                ", zipcode='" + zipcode + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", rate=" + rate +
                ", serviceDays=" + serviceDays +
                ", hoursPerDay=" + hoursPerDay +
                ", datesToSkip=" + datesToSkip +
                '}';
    }
}