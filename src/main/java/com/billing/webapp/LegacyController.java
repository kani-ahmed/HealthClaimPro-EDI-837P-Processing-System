package com.billing.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LegacyController {
    /*
     * This class is the controller for the legacy system.
     * It handles the requests from the legacy system.
     * It uses the LegacyService and LegacyDataService to process the requests.
     * It returns the response to the legacy system.
     * It also handles the batch requests from the legacy system.
     * It returns the combined zip file to the legacy system.
     * It uses the ZipUtil class to zip the files.
     * It uses the SftpUploadService class to upload the files to the SFTP server.
     * It uses the LegacyDataRepository and LegacyDataService to retrieve the billing history.
     * It uses the LegacyDataDetails class to format the detailed response.
     * It uses the BillingHistoryDTO class to format the billing history response.
     */

    private static final Logger logger = LoggerFactory.getLogger(LegacyController.class);

    private final LegacyService legacyService;
    private final LegacyDataService legacyDataService;

    // initialize the LegacyService and LegacyDataService beans using constructor injection
    @Autowired
    public LegacyController(LegacyService legacyService, LegacyDataService legacyDataService) {
        this.legacyService = legacyService;
        this.legacyDataService = legacyDataService;
    }

    /*
    * This endpoint is used by the interface to request usernames from the database by part of the first name.
    * It returns a list of usernames that match the search string passed from the interface.
     */
    @GetMapping("/users/search")
    @CrossOrigin
    public ResponseEntity<List<String>> searchUsersByFirstName(@RequestParam String firstName) {
        // Call the searchByFirstName method in the LegacyDataService to retrieve the list of usernames
        List<LegacyData> users = legacyDataService.searchByFirstName(firstName);
        // Map the list of LegacyData objects to a list of usernames using streams and lambdas and return the list
        List<String> usernames = users.stream() // Convert the list to a stream
                .map(LegacyData::getFirstName) // Map each LegacyData object to its username using a method reference
                .distinct() // Optional, if you want to remove duplicates
                .collect(Collectors.toList()); // Collect the stream into a list
        return ResponseEntity.ok(usernames);
    }


    /*
    * This endpoint is used by the interface to request the details of a user by username (chosen from the list of usernames returned by the previous endpoint).
    * It returns the details of the user with the latest date range.
    * It uses the LegacyDataDetails class to format the response.
    * It returns a 404 response if the user is not found.
    * It returns a 200 response with the details of the user if the user is found.
     */
    @GetMapping("/users/details")
    @CrossOrigin
    public ResponseEntity<LegacyData> getUserDetailsByFirstName(@RequestParam String firstName) {
        // Call the findByFirstName method in the LegacyDataService to retrieve the user
        LegacyData user = legacyDataService.findByFirstName(firstName);
        // If the user is found and has date ranges in the database (should be true if at least one claim has been generated for the user)
        if (user != null && user.getDateRanges() != null && ! user.getDateRanges().isEmpty()) {
            // Sort the date ranges by end date in descending order
            user.getDateRanges().sort((dr1, dr2) -> dr2.getEndDate().compareTo(dr1.getEndDate()));

            // Take the first claim date range after sorting, which is the latest date range for the user
            DateRange latestDateRange = user.getDateRanges().get(0);

            // Clear the existing date ranges and add only the latest one
            user.getDateRanges().clear();
            user.getDateRanges().add(latestDateRange);

            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /*
    * This endpoint is used to receive claim specific data (for single client or batch of clients at once) from the interface.
    * The endpoint can receive multiple requests in a single batch. The requests are sent as a LegacyRequestBatch object. See the LegacyRequestBatch class for details.
    * It uses the LegacyService to generate the claim files and the LegacyDataService to save the data in the database.
    * It uses the ZipUtil class to zip the files.
    * It uses the SftpUploadService class to upload the files to the SFTP server.
    * It returns a 200 response with the combined zip file if the files are generated and uploaded successfully.
    * It returns a 400 response if the request is empty or null.
    * It returns a 500 response if there is an error in generating or sending the files.
     */
    @PostMapping("/receiveBatchData")
    public ResponseEntity<?> receiveBatchData(@RequestBody LegacyRequestBatch batch) {
        // Check if the request is empty or null
        if (batch.getRequests() == null || batch.getRequests().isEmpty()) {
            logger.error("Received empty or null requests list in batch");
            // Return a 400 response with an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Batch requests are empty");
        }
        // Create a list to collect the file paths of the generated files
        List<String> allFilePaths = new ArrayList<>();
        // Loop through the list of requests in the batch and process each request individually to generate the claim files
        for (LegacyRequest legacyRequest : batch.getRequests()) {
            try {
                // Set data from request and generate the claim file
                legacyService.setDataFromRequest(legacyRequest);

                // Generate the claim and get total claim amount for the user from the LegacyService class
                double totalClaimAmount = legacyService.generateClaim();

                // Save the legacy data and total claim amount in the database using the LegacyDataService class
                legacyDataService.saveOrUpdateLegacyData(legacyRequest, totalClaimAmount);

                // After files are generated, collect the file paths of the generated files in a list
                List<String> filePaths = legacyService.getGeneratedFilePaths();
                allFilePaths.addAll(filePaths); // Add the file paths to the list of all file paths
            } catch (Exception e) {
                logger.error("Error generating or sending files", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return a 500 response with an error message
            }

        }
        try {
            // Zip all collected files
            String finalZipFilename = "combined.zip"; // Change this to your preferred filename (For now, I will use combined.zip)
            byte[] combinedZipBytes = ZipUtil.zipFiles(allFilePaths); // Use the ZipUtil class to zip the files

            // Prepare the final zip for download by converting the byte array to an InputStreamResource
            ByteArrayInputStream bais = new ByteArrayInputStream(combinedZipBytes);
            InputStreamResource inputStreamResource = new InputStreamResource(bais);

            // Set headers to suggest a download with the final zip filename
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + finalZipFilename);

            return ResponseEntity.ok() // Return a 200 response with the final zip file
                    .headers(headers) // Set the headers
                    .contentLength(combinedZipBytes.length) // Set the content length of the final zip file in the headers
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // Set the content type of the final zip file in the headers
                    .body(inputStreamResource); // Set the final zip file as the response body using the InputStreamResource object created above
        } catch (Exception e) {
            logger.error("Error in creating the final combined zip file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return a 500 response with an error message
        }
    }

    /*
    * This endpoint is used to retrieve the billing history from the database.
    * It uses the LegacyDataRepository and LegacyDataService to retrieve the billing history.
    * It uses the BillingHistoryDTO class to format the billing history response.
    * It returns a 200 response with the billing history if the billing history is retrieved successfully.
    * It returns a 500 response if there is an error in retrieving the billing history.
    * It returns an empty list if there is no billing history.
    * The billing history is returned as a list of BillingHistoryDTO objects.
     */
    @GetMapping("/billingHistory")
    public ResponseEntity<List<BillingHistoryDTO>> getBillingHistory() {
        List<BillingHistoryDTO> billingHistory = legacyDataService.getBillingHistory(); // Call the getBillingHistory method in the LegacyDataService to retrieve the billing history
        return ResponseEntity.ok(billingHistory); // Return a 200 response with the billing history
    }

    // New class to format the detailed response
    static class LegacyDataDetails {
        /*
            * This class is used to format the detailed response.
            * It is used to return the details of the user with the latest date range (latest claim date range).
            * It is used by the getUserDetailsByFirstName endpoint.
            * It is used by the interface to display the details of the user with the latest date range.
         */
        private Map<String, Double> serviceHoursDetails; // Map to hold the service hours details for each day of the week
        private String startDate; // Start date of the latest date range
        private String endDate; // End date of the latest date range
        // Other fields from LegacyData if needed

        public LegacyDataDetails(DateRange latestDateRange) {
            serviceHoursDetails = new HashMap<>(); // Initialize the map to hold the service hours details for each day of the week
            serviceHoursDetails.put("Monday", latestDateRange.getMondayHours());
            serviceHoursDetails.put("Tuesday", latestDateRange.getTuesdayHours());
            serviceHoursDetails.put("Wednesday", latestDateRange.getWednesdayHours());
            serviceHoursDetails.put("Thursday", latestDateRange.getThursdayHours());
            serviceHoursDetails.put("Friday", latestDateRange.getFridayHours());
            serviceHoursDetails.put("Saturday", latestDateRange.getSaturdayHours());
            serviceHoursDetails.put("Sunday", latestDateRange.getSundayHours());

            this.startDate = latestDateRange.getStartDate(); // Set the start date of the latest date range
            this.endDate = latestDateRange.getEndDate(); // Set the end date of the latest date range
            // Set other fields from the latest date range or LegacyData as needed
        }

        // Getters and setters
    }


}