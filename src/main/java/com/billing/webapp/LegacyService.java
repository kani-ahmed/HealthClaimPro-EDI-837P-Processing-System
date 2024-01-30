package com.billing.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LegacyService {
    /*
     * This class is used to generate the claim files for each user.
     * The @Scope annotation is used to make sure that each request has its own instance of this class.
     * The @ScopedProxyMode annotation is used to make sure that the @Scope annotation works.
     * The @Autowired annotation is used to inject the LegacyDataRepository and the FileWriterHelperService.
     * The generateClaim() method is used to generate the claim files for each user.
     * The setDataFromRequest() method is used to set the data from the request body.
     * The generateFileName() method is used to generate the file name.
     * The getClaimLine() method is used to generate the claim line.
     * The getGeneratedFilePaths() method is used to retrieve the file paths of the generated files.
     * The calculateTotalClaimSumForCurrentFile() method is used to calculate the total claim sum for the current file.
     * The findFirstWorkday() method is used to find the first workday or service day.
     * The findLastWorkday() method is used to find the last workday or service day.
     * The resetStateForNewRequest() method is used to reset the state for each new request.
     * The generateZipFilename() method is used to generate the zip file name.
     * The findNextWorkday() method is used to find the next workday or service day.
     * The isWorkday() method is used to check if a date is a workday or service day.
     */
    private static final Logger logger = LoggerFactory.getLogger(LegacyService.class);
    //----------------------------------------------------------------------------------------------------------------------
    //Service Line Number
    //String lx01_assignedNumber = String.valueOf(writerHelper.getCurrentLxCounter()); // Assigned Number (Required)
//----------------------------------------------------------------------------------------------------------------------
    //SV1: Professional Service
    String sv101_01_productServiceIdQualifier = "HC"; // Product or Service ID Qualifier (Required), HC=Health Care Financing Administration Common Procedure Coding System (HCPCS)
    String sv101_02_procedureCode = "G0156"; // Procedure Code (Required), G0156=Services performed by a qualified physical therapist in the home health setting, each 15 minutes
    //double sv102_lineItemChargeAmount = 0.0; // Line Item Charge Amount (Required)
    String sv103_unitOrBasisForMeasurementCode = "UN"; // Unit or Basis for Measurement Code (Required), UN=Unit (1 Unit = 15 minutes)
    String sv104_serviceUnitCount = "24"; // Service Unit Count (Required), 24=1 hour (4 units per hour)
//----------------------------------------------------------------------------------------------------------------------
    // Other options in case needed in the future

    // 12=Patient's Home, 11=Office, 21=Inpatient Hospital, 22=Outpatient Hospital,
    // 23=Emergency Room - Hospital, 24=Ambulatory Surgical Center, 31=Skilled Nursing Facility, 32=Nursing Facility,
    // 33=Custodial Care Facility, 34=Hospice, 41=Ambulance - Land, 42=Ambulance - Air or Water,
    // 51=Inpatient Psychiatric Facility, 52=Psychiatric Facility - Partial Hospitalization, 53=Community Mental Health Center,
    // 54=Intermediate Care Facility/Mentally Retarded, 55=Residential Substance Abuse Treatment Facility,
    // 56=Psychiatric Residential Treatment Center, 61=Comprehensive Inpatient Rehabilitation Facility,
    // 62=Comprehensive Outpatient Rehabilitation Facility, 65=End-Stage Renal Disease Treatment Facility,
    // 71=Public Health Clinic, 72=Rural Health Clinic, 81=Independent Laboratory, 99=Other Unlisted Facility
    String sv105_placeOfServiceCode = "12"; // Place of Service Code (Required), 12=Patient's Home
//----------------------------------------------------------------------------------------------------------------------
    //Other options in case needed in the future

    //2=Secondary Diagnosis, 3=Additional Secondary Diagnosis, 4=Additional Secondary Diagnosis,
    // 5=Additional Secondary Diagnosis, 6=Additional Secondary Diagnosis, 7=Additional Secondary Diagnosis,
    // 8=Additional Secondary Diagnosis, 9=Additional Secondary Diagnosis, 0=No Diagnosis or Condition
    String sv107_01_compositeDiagnosisCodePointer = "1"; // Composite Diagnosis Code Pointer (Required), 1=Primary Diagnosis

    //----------------------------------------------------------------------------------------------------------------------
    // Define optional fields, setting them to empty or specific values as needed
    String sv103_02_procedureModifier = ""; // Procedure Modifier (Optional)
    String sv103_03_procedureModifier = ""; // Procedure Modifier (Optional)
    String sv103_04_procedureModifier = ""; // Procedure Modifier (Optional)
    String sv103_05_procedureModifier = ""; // Procedure Modifier (Optional)
    String sv103_06_procedureModifier = ""; // Procedure Modifier (Optional)
    String sv103_07_description = ""; // Description (Optional)
    // Other optional fields as per usage notes
//----------------------------------------------------------------------------------------------------------------------
    //Other options in case needed in the future

    // 102=Effective Date, 151=Admission Date, 193=Last Certification Date, 194=First Certification Date,
    // 198=Prescription Date, 286=Date of Birth, 290=Date of Death, 291=Date of Service,
    // 292=Medicare Coverage Effective Date, 295=Medicare Coverage Termination Date, 304=Latest Visit or Consultation Date,
    // 307=Certification Revision Date, 314=Initial Treatment Date, 356=Date of Last Injection, 435=Last X-Ray Date,
    // 454=Initial Disability Period Start Date, 455=Initial Disability Period Last Day Worked, 471=Service Period End Date,
    // 594=Date of Last Test, 636=Date of Most Recent Hemoglobin or Hematocrit or Both,
    // 771=Date of Most Recent Blood Pressure Reading, 773=Date of Most Recent Weight Test,
    // 774=Date of Most Recent Height Test, 851=Date of Most Recent Refraction,
    // 864=Date of Most Recent Contact Lens Prescription Written, 866=Date of Most Recent Eye Surgery
    String dtp01_dateTimeQualifier = "472"; // Date Time Qualifier (Required), 472=Service Period Start Date,
    //----------------------------------------------------------------------------------------------------------------------
    String dtp02_dateTimePeriodFormatQualifier = "D8"; // Date Time Period Format Qualifier (Required), D8=CCYYMMDD, D9=MMDDYYYY, RD=Relative Date, DT=Date Time (CCYYMMDDHHMM), DTM=Date Time (CCYYMMDDHHMMSS), TS=Time Stamp (CCYYMMDDHHMMSSNNNNNN), Y4=Year and Month (CCYYMM)
    private String firstName;
    private String lastName;
    private String start_date;
    private String end_date;
    private double rate;
    private Map<DayOfWeek, Double> hoursPerDay = new HashMap<>();
    private LocalDate start;
    private LocalDate end;
    private long daysOfService;
    private String address;
    private String birthday;
    private String zipcode;
    private String idNumber;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private Double totalClaimSumCurrentFile = 0.0;
    private Double totalClaimSumAllFilesPerUser = 0.0;
    private List<String> datesToSkip;
    private List<LocalDate> dates_to_skip = new ArrayList<>();
    private int fileSequence = 1; // This is the sequence number for the file name (e.g. 1 for the first file, 2 for the second file etc.)
    private List<DayOfWeek> serviceDays;
    @Autowired
    private FileWriterHelperService writerHelper; // Autowired property for the helper service

    // Autowired property for the repository to persist the data for each user (e.g. first name, last name, id number etc.)
    @Autowired
    private LegacyDataRepository legacyDataRepository;

    // Autowired property for the repository to persist the data for the date ranges for each user (e.g. start date, end date etc.) (one-to-many relationship with LegacyData)
    @Autowired
    private DateRangeRepository dateRangeRepository;

    // Autowired property for the repository to persist the data for the total claim charges for each user (e.g. total claim sum etc.) (one-to-many relationship with LegacyData)
    @Autowired
    private TotalClaimChargePerUserRepository totalClaimChargePerUserRepository;

    public Map<DayOfWeek, Double> getHoursPerDay() {
        return hoursPerDay;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public LocalDate getActualStart() {
        return actualStart;
    }

    public void setActualStart(LocalDate actualStart) {
        this.actualStart = actualStart;
    }

    public LocalDate getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(LocalDate actualEnd) {
        this.actualEnd = actualEnd;
    }

    public Double getTotalClaimSumCurrentFile() {
        return totalClaimSumCurrentFile;
    }

    public List<LocalDate> getDates_to_skip() {
        return dates_to_skip;
    }

    public List<DayOfWeek> getServiceDays() {
        return serviceDays;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public long getDaysOfService() {
        return daysOfService;
    }

    public String getFirstName() {
        return firstName;
    }

    public double getRate() {
        return rate;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getZipcode() {
        return zipcode;
    }
    //END OF FIELDS DECLARATIONS, GETTERS AND SETTERS
    //------------------------------------------------------------------------------------------------------------------

    /*
     * This method is used to generate the claim files for each user.
     * It loops through the dates and writes the claim lines to the file.
     * It also calculates the total claim sum for each file and for all files per user.
     * It also manages the file uploads.
     * It returns the total claim sum for all files per user to be saved into the database.
     */
    @CrossOrigin
    public Double generateClaim() throws IOException, InterruptedException {

        LocalDate currentDate = start; // Start from the beginning of the service period
        LocalDate nextFileStartDate = start; // Initialize next file's start date

        // Calculate the actual start and end dates
        this.actualStart = findFirstWorkday(this.start, this.end, this.serviceDays, this.dates_to_skip); // Find the first service day for each claim file for each user
        this.actualEnd = findLastWorkday(this.start, this.end, this.serviceDays, this.dates_to_skip); // Find the last service day for each claim file for each user

        while (currentDate.isBefore(end.plusDays(1))) {
            // Generate the file name, initialize the file and write its contents
            String fileName = generateFileName(nextFileStartDate, calculateTotalClaimSumForCurrentFile(nextFileStartDate));

            // Calculate the total claim for the current file's content
            calculateTotalClaimSumForCurrentFile(nextFileStartDate);

            writerHelper.initializeFile(fileName, totalClaimSumCurrentFile); // Initialize the file with total claim sum for the current file

            // Add the total claim sum for the current file to the total claim sum for all files per user
            totalClaimSumAllFilesPerUser += totalClaimSumCurrentFile;

            int linesWritten = 0; // Initialize the counter for the number of lines written to the file

            // Loop through the dates to write up to 50 lines or until the end of service period date is reached
            while (linesWritten < 50 && ! currentDate.isAfter(end)) {
                // Check if the current date is a service day and not a date to skip
                if (! dates_to_skip.contains(currentDate) && serviceDays.contains(currentDate.getDayOfWeek())) {

                    String claimLine = getClaimLine(currentDate); // Generate the claim line for the current date

                    writerHelper.writeFileLine(claimLine); // Write the claim line to the file for the current date

                    linesWritten++; // Increment the counter for the number of lines written to the file
                }
                if (linesWritten < 50) { // Write to file as long as the number of lines written is less than 50
                    currentDate = currentDate.plusDays(1); // Move to the next date
                }
            }
            // Close the current file after writing up to 50 lines or reaching the end of service period date.
            writerHelper.closeWriter();


            if (linesWritten == 50 && ! currentDate.isAfter(end)) {
                // If 50 lines have been written and end of service date is not reached yet,
                // set the start for the next file to the next service date in the billing cycle
                nextFileStartDate = currentDate.plusDays(1);
            }

            // If the end date has been reached or 50 lines have been written, prepare for a new file
            if (currentDate.isAfter(end) || linesWritten == 50) {
                // Check if the end date has been reached
                if (! currentDate.isAfter(end)) {
                    // If the end date has not been reached, set the start for the next file to the next service date in the billing cycle
                    currentDate = currentDate.plusDays(1);
                    // Reset the Lx counter for the next file to 1 for the new file
                    writerHelper.setCurrentLxCounter(1);
                } else {
                    // If the end of service period is reached, break out of the loop (we are done generating files for this user)
                    break;
                }
            }
        }
        //after the while loop, all files are generated for the current user. Therefore, start uploading processes.

        // Notify the FileWriterHelper to upload the files to the SFTP server
        writerHelper.manageFileUploads(getGeneratedFilePaths());

        // Return the total claim sum for all files per user to be saved into the database
        return totalClaimSumAllFilesPerUser;
    }

    /*
     * This method is used to calculate the total claim sum for the current file of the current user (amount billed to the payer for the current file).
     * It sets the field totalClaimSumCurrentFile to the calculated value of the sum.
     * It loops through the dates and calculates the total claim sum for the next 50 lines or until the end of service period date is reached.
     * It returns the last date a service was provided for this file. This is used to set the start date for the next file.
     */
    @CrossOrigin
    private LocalDate calculateTotalClaimSumForCurrentFile(LocalDate startOfCalculation) {
        totalClaimSumCurrentFile = 0.0; // Reset the total claim sum for the new file
        LocalDate calculationDate = startOfCalculation; // Initialize the calculation date to the start of calculation

        // Initialize to one day before the start of calculation to ensure correct date for the first service day.
        LocalDate lastServiceDate = startOfCalculation.minusDays(1);

        // Initialize the counter for the number of lines written to the file to 0
        int counter = 0;

        // Loop through the dates to calculate total claim sum for the next 50 lines or until the end
        while (calculationDate.isBefore(end.plusDays(1)) && counter < 50) {
            // Check if the current date is a service day and not a date to skip
            if (! dates_to_skip.contains(calculationDate) && serviceDays.contains(calculationDate.getDayOfWeek())) {

                // Extract the hours for the current day from the hoursPerDay map
                double hoursForDay = hoursPerDay.get(calculationDate.getDayOfWeek());

                // Calculate the total claim sum for the current file using the rate and hours for the current day
                totalClaimSumCurrentFile += rate * hoursForDay;

                // Update the last service date to the current date since a service was provided on this date
                lastServiceDate = calculationDate;

                // Increment the counter for the number of lines written to the file
                counter++;
            }
            calculationDate = calculationDate.plusDays(1); // Always Move to the next date until constraint is met
        }

        // Return the last date a service was provided for this file (used to set the start date for the next file)
        return lastServiceDate;
    }

    /*
     * This method is used to generate the file name.
     * It uses the first name, last name, start date, end date and file sequence number to generate the file name.
     * It returns the generated file name.
     * The file name is used to initialize the file and write its contents.
     * The file name is also used to track the file paths of the generated files.
     * The file paths are used to upload the files to the SFTP server.
     * This is a good naming scheme as each generated file for each user has its the accurate service date range in the file name.
     */
    @CrossOrigin
    private String generateFileName(LocalDate fileStartDate, LocalDate fileEndDate) {
        // Here's a simple naming scheme: PatientName_StartDate-EndDate_FileSequence.txt

        // Format the start date to match the naming scheme
        String formattedStartDate = fileStartDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Format the end date to match the naming scheme
        String formattedEndDate = fileEndDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Generate the file name using the first name, last name, start date, end date and file sequence number
        String fileName = lastName + "," + firstName + "_" + formattedStartDate + "-" + formattedEndDate + "_" + fileSequence + ".txt";

        // Increment the file sequence number for the next file (e.g. 1 for the first file, 2 for the second file etc.)
        fileSequence++;

        // Return the generated file name
        return fileName;
    }

    /*
     * This method is used to generate the claim line.
     * It uses the date to generate the claim line.
     * This method also records each service line with its total charge per service line (service date) as required by the 837P format.
     * It finally returns the generated claim segments
     */
    @CrossOrigin
    public String getClaimLine(LocalDate date) {
        // Get the day of week for the current date to use as a key for the hoursPerDay map
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Get the hours for the current date from the hoursPerDay map or use 0.0 if not found
        double dailyHours = hoursPerDay.getOrDefault(dayOfWeek, 0.0);

        // Line Item Charge Amount (Required) (Rate * Hours for the current date) (e.g. 10.00 * 8.00 = 80.00)
        var sv102_lineItemChargeAmount = rate * dailyHours;

        // Service Date (Required) (Format: CCYYMMDD) (e.g. 20210101)
        var dtp03_serviceDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Assigned Number (Required) (e.g. 1 for the first service line, 2 for the second service line etc.)
        var lx01_assignedNumber = writerHelper.getCurrentLxCounter();

        // return the generated claim segments for the current date (LX, SV1, DTP)
        return "LX*" + lx01_assignedNumber + "~\n" +
                //SV1: Professional Service (Required)
                "SV1*" + sv101_01_productServiceIdQualifier + ":" + sv101_02_procedureCode + "*" + sv102_lineItemChargeAmount + "*" + sv103_unitOrBasisForMeasurementCode + "*" + sv104_serviceUnitCount + "*" + sv105_placeOfServiceCode + "**" + sv107_01_compositeDiagnosisCodePointer + "~\n" +
                //DTP: Date - Service Date (Required) (Format: CCYYMMDD)
                "DTP*" + dtp01_dateTimeQualifier + "*" + dtp02_dateTimePeriodFormatQualifier + "*" + dtp03_serviceDate + "~\n";
    }


    /*
     * This method is used to retrieve the file paths of the generated files.
     * It returns the file paths of the generated files for each request of user.
     * The file paths are used to upload the files to the SFTP server.
     */
    public List<String> getGeneratedFilePaths() {
        return writerHelper.getGeneratedFilePaths(); // Retrieve the tracked file paths
    }


    /*
     * This method is used to find the first workday or service day.
     * It uses the start date, end date, service days and dates to skip to find the first workday or service day.
     * It returns the first workday or service day.
     * This is used to calculate the actual service start date.
     */
    private LocalDate findFirstWorkday(LocalDate start, LocalDate end, List<DayOfWeek> serviceDays, List<LocalDate> datesToSkip) {
        LocalDate current = start; // Start from the beginning of the service period

        // Loop through the dates to find the first workday or service day
        while (current.isBefore(end) || current.equals(end)) {
            // Check if the current date is a service day and not a date to skip
            if (isWorkday(current, serviceDays, datesToSkip)) {
                return current;
            }
            // Move to the next date until workday or service day is found
            current = current.plusDays(1);
        }
        return start; // Fallback to the original start date if no workday or service day is found in the service period (e.g. all dates are skipped)
    }

    /*
     * This helper method is used to check if a date is a workday or service day.
     * It uses the date, service days and dates to skip to check if a date is a workday or service day.
     * It returns true if the date is a workday or service day and false otherwise.
     */
    private boolean isWorkday(LocalDate date, List<DayOfWeek> serviceDays, List<LocalDate> datesToSkip) {
        // Check if the current date is a service day and not a date to skip
        return serviceDays.contains(date.getDayOfWeek()) && ! datesToSkip.contains(date);
    }

    /*
     * This method is used to find the last workday or service day.
     * It uses the start date, end date, service days and dates to skip to find the last workday or service day.
     * It returns the last workday or service day.
     * This is used to calculate the actual service end date.
     */
    private LocalDate findLastWorkday(LocalDate start, LocalDate end, List<DayOfWeek> serviceDays, List<LocalDate> datesToSkip) {
        // Start from the end of the service period (we do less work this way since we don't have to loop through all the dates to find the last workday or service day
        LocalDate current = end;

        // Loop through the dates to find the last workday or service day
        while (current.isAfter(start) || current.equals(start)) {
            // Check if the current date is a service day and not a date to skip
            if (isWorkday(current, serviceDays, datesToSkip)) {
                return current; // Return the last workday or service day
            }

            // Move to the previous date until workday or service day is found
            current = current.minusDays(1);
        }

        // Fallback to the original end date if no workday or service day is found in the service period (e.g. all dates are skipped)
        return end;
    }

    /*
     * This method is used to set the data from the request body.
     * It uses the request body to set the data for each request.
     * It also resets the state for each new request. This is important because the same instance of this class is used for each request.
     * This method is called from the LegacyController class to set the data from the request body.
     */
    @CrossOrigin
    public void setDataFromRequest(LegacyRequest request) {
        // Reset the state for each new request (we could also use a new instance of this class for each request)
        resetStateForNewRequest();

        this.firstName = request.getFirstName(); // Set the first name from the request body to the field firstName of this class
        this.lastName = request.getLastName(); // Set the last name from the request body to the field lastName of this class
        this.idNumber = request.getIdNumber(); // Set the id number from the request body to the field idNumber of this class
        this.address = request.getAddress(); // Set the address from the request body to the field address of this class
        this.birthday = request.getBirthday(); // Set the birthday from the request body to the field birthday of this class
        this.zipcode = request.getZipcode(); // Set the zipcode from the request body to the field zipcode of this class
        this.start_date = request.getStartDate(); // Set the start date from the request body to the field start_date of this class
        this.end_date = request.getEndDate(); // Set the end date from the request body to the field end_date of this class
        this.rate = request.getRate(); // Set the rate from the request body to the field rate of this class

        // Convert serviceDays strings to DayOfWeek and populate hoursPerDay map with DayOfWeek as key and Double as value (e.g. MONDAY, 8.00)
        this.serviceDays = new ArrayList<>();

        request.getServiceDays().forEach(dayStr -> { // Loop through the service days strings from the request body
            try {
                // Convert the service day string to DayOfWeek
                DayOfWeek day = DayOfWeek.valueOf(dayStr.toUpperCase());

                // Add the service day to the serviceDays list
                this.serviceDays.add(day);
            } catch (IllegalArgumentException e) {
                // Log or handle the invalid day string
                // In this case, No need to do anything as the frontend already sanitizes the data for this field and only sends valid data.
            }
        });

        // Populate the hoursPerDay map with DayOfWeek as key and Double as value (e.g. MONDAY, 8.00)
        this.hoursPerDay = request.getHoursPerDay();

        // This part of the code is used to convert the dates to skip from the request body to LocalDate and store them in the dates_to_skip list
        this.dates_to_skip = new ArrayList<>();
        request.getDatesToSkip().forEach(dateStr -> {
            try {
                // Convert the date string to LocalDate
                LocalDate date = LocalDate.parse(dateStr);

                // Add the date to the dates_to_skip list
                this.dates_to_skip.add(date);
            } catch (Exception e) {
                // Log or handle the invalid date string
            }
        });

        // Set the start and end dates from the request body to the fields start and end of this class
        start = LocalDate.parse(start_date);
        end = LocalDate.parse(end_date);

        // Convert string dates from the request to LocalDate
        this.start = LocalDate.parse(request.getStartDate());
        this.end = LocalDate.parse(request.getEndDate());

        // Calculate the actual start and end dates
        this.actualStart = findFirstWorkday(this.start, this.end, this.serviceDays, this.dates_to_skip);
        this.actualEnd = findLastWorkday(this.start, this.end, this.serviceDays, this.dates_to_skip);
    }

    /*
     * This method is used to reset the state for each new request.
     * It resets the fields related to processing.
     * This is important because the same instance of this class is used for each request.
     * This method is called from the setDataFromRequest() method to reset the state for each new request.
     */
    private void resetStateForNewRequest() {
        // Reset the total claim sum for all files per user for each new request
        this.totalClaimSumAllFilesPerUser = 0.0;

        // Reset the file sequence number for each new request
        this.fileSequence = 1;

        // Reset the total claim sum for the current file for each new request
        this.totalClaimSumCurrentFile = 0.0;
    }

    //Not used currently, kept for future use
    public String generateZipFilename() {
        // Format: LastNameFirstName_StartDate-EndDate.zip
        return lastName + firstName + "_" + start_date + "-" + end_date + ".zip";
    }

    // Not used currently, kept for future use
    public LocalDate findNextWorkday(LocalDate lastEndDate, List<DayOfWeek> serviceDays) {
        LocalDate nextDay = lastEndDate.plusDays(1);
        while (! serviceDays.contains(nextDay.getDayOfWeek())) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
}