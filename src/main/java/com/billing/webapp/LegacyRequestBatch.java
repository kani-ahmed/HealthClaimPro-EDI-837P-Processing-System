package com.billing.webapp;

import java.util.List;

public class LegacyRequestBatch {
    /*
     * This class is used to deserialize the JSON request body into a Java object.
     * It is used in the LegacyController class to handle the POST request that is sent from the frontend.
     * The batch requests that are sent from the frontend are deserialized into a list of LegacyRequest objects.
     * The list of LegacyRequest objects is then used to create a list of LegacyData objects that are processed and saved to the database.
     */
    private List<LegacyRequest> requests; // List of LegacyRequest objects

    // Getter and Setter methods for the requests field of the class are needed for deserialization
    public List<LegacyRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<LegacyRequest> requests) {
        this.requests = requests;
    }
}