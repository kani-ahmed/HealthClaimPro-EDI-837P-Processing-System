### Hi there 👋, I'm Kani. Welcome!!! You have lots to explore

---

#### Welcome to My Claims Billing System
![WELCOME TO MY CLAIMS BILLING SYSTEM](https://cdn.nbyula.com/public/community/6274c9494944b1001b466858/bannerImage/1651820974427-6274c9494944b1001b466858.jpeg)
Image credit: [link](https://cdn.nbyula.com/public/community/6274c9494944b1001b466858/bannerImage/1651820974427-6274c9494944b1001b466858.jpeg)

---

**Overview:**  

The Claims Billing System transforms healthcare claims processing by automating the generation and submission of EDI files. This automation turns tasks that would traditionally take hours into processes completed within seconds. It greatly enhances the efficiency of claims data interchange between payees or healthcare providers, such as Home Healthcare Agencies, other sectors (e.g. hospitals), and insurance companies (payors). The system ensures a seamless, secure, and swift billing cycle, revolutionizing interactions between providers and payers. 

The system generates the EDI 837 Professional file (837P) in compliance with the X12 Version 005010X222A1 standard. This ensures adherence to HIPAA requirements for healthcare transactions. The 837P format is designed for healthcare claim submissions. It promotes efficient data exchange between healthcare providers and insurance payors. By using this standard, the product I developed through this project upholds HIPAA's strict rules for patient data protection.

**Key Features:**

- **Automated EDI File Generation:** Streamlines the creation of 837P healthcare claims, significantly reducing manual effort and margin for error.
- **User-Friendly Interface:** Designed for ease of use, simplifying data input and management for seamless daily operations. (visit its repo for details)
- **Secure File Transmission:** Utilizes SFTP for the safe and compliant transfer of sensitive data (SFTP ). Is SFTP secure? depends on who you ask, right? But one thing is for sure. IBM is not too excited about it. Read: [link](https://www.ibm.com/downloads/cas/AV17R94L)
- **Enhanced Efficiency:** By automating the submission of claims, the system vastly improves the billing process's speed and accuracy.

**Technologies Used:**

- **Frontend:** React, HTML5, CSS3 (repo link: [link](https://github.com/kenny-ahmedd/HealthClaimHub-EDI-837P-Interface))
- **Backend:** Java with Spring Boot, Spring Data JPA for robust, scalable server-side logic
- **Database:** MySQL for reliable data storage and retrieval
- **Security:** Integrated Spring Security and Firebase Authentication ensure secure access and data protection.
- **APIs:** RESTful design for flexible, efficient client-server communication
- **Tools:** Gradle for powerful build automation, IntelliJ IDEA for an optimized development environment

**Skills and Tools:**
*Java, Spring Boot, Spring Data JPA, RESTful APIs, MySQL, Firebase Authentication, SFTP, Gradle, IntelliJ IDEA, Version Control, CI/CD.*

The backend processes a series of requests from the Front-end through well-defined and secure API endpoints. Below is documentation detailing each endpoint for those looking to integrate this backend with their preferred Front-end or to enhance code readability.

---
## API Documentation
---
This is the API documentation for my Healthcare Claims Billing System. This guide provides detailed information on how to interact (or how the Front-End interacts) with the system's RESTful APIs to manage the generation and submission of healthcare claims efficiently.

#### Base URL

All URLs referenced in the documentation have the base path:

http://yourdomain.com/api


Replace `http://yourdomain.com` with the actual domain where the application is hosted. In the case of running it locally: `http://localhost:port/api`

#### Authentication

Most endpoints require authentication. Use the following header for requests:

Authorization: Bearer <Your_Firebase_Token>


#### Endpoints
---
### Search Users by First Name

- **URL:** `/users/search`
- **Method:** `GET`
- **Description:** Searches for users by a partial match on their first name.
- **Authentication Required:** Yes (`Authorization: Bearer <Your_Firebase_Token>`)
- **Query Parameters:**
  - `firstName`: The partial or full first name to search for.
- **Success Response:**
  - **Code:** `200 OK`
  - **Content:** 
    ```json
    [
      "JohnDoe",
      "JohnSmith"
    ]
    ```
- **Error Response:**
  - **Code:** `404 Not Found` if no users match the criteria.

**Sample cURL Command:**
```bash
curl -X GET "http://yourdomain.com/api/users/search?firstName=John" \
     -H "Authorization: Bearer <Your_Firebase_Token>"
```

### Get User Details

- **URL:** `/users/details`
- **Method:** `GET`
- **Description:** Retrieves detailed information for a user, including the latest date range.
- **Authentication Required:** Yes (`Authorization: Bearer <Your_Firebase_Token>`)
- **Query Parameters:**
  - `firstName`: The user's first name.
- **Success Response:**
  - **Code:** `200 OK`
  - **Content:** 
    ```json
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "idNumber": "12345",
      "address": "123 Main St",
      "birthday": "1990-01-01",
      "zipcode": "10001",
      "rate": 50.00,
      "dateRanges": [
        {
          "startDate": "2023-01-01",
          "endDate": "2023-01-07",
          "mondayHours": 8,
          "tuesdayHours": 8,
          "wednesdayHours": 8,
          "thursdayHours": 8,
          "fridayHours": 8,
          "saturdayHours": 0,
          "sundayHours": 0
        }
      ]
    }
    ```
- **Error Response:**
  - **Code:** `404 Not Found` if the user does not exist.

**Sample cURL Command:**
```bash
curl -X GET "http://yourdomain.com/api/users/details?firstName=John" \
     -H "Authorization: Bearer <Your_Firebase_Token>"
```

### Receive Batch Data

- **URL:** `/receiveBatchData`
- **Method:** `POST`
- **Description:** Processes a batch of claims data, generates claim files for each request, and zips them into a single file named `combined.zip`.
- **Request Body:** JSON object representing the batch of claims. Please refer to the `LegacyRequestBatch` class for structure.
- **Authentication Required:** Yes (`Authorization: Bearer <Your_Firebase_Token>`)
- **Success Response:**
  - **Code:** `200 OK`
  - **Content:** The response will initiate a download of a ZIP file named `combined.zip` containing the processed claim files for a user or group of users for batch submissions.
  - **Headers:**
    - `Content-Type: application/octet-stream`
    - `Content-Disposition: attachment; filename="combined.zip"`
    - `Content-Length: [size of the ZIP file in bytes]`
- **Error Response:**
  - **Code:** `400 Bad Request` if the batch is empty or null.
  - **Code:** `500 Internal Server Error` for processing errors, including potential issues within individual claims in the batch.
- **Sample cURL Command:**
  ```bash
  curl -X POST http://yourdomain.com/api/receiveBatchData \
       -H "Authorization: Bearer <Your_Firebase_Token>" \
       -H "Content-Type: application/json" \
       -d '{
             "requests": [
               {
                 "firstName": "John",
                 "lastName": "Doe",
                 ...
               },
               ...
             ]
           }'
  ```

### Retrieve Billing History

- **URL:** `/billingHistory`
- **Method:** `GET`
- **Description:** Retrieves the billing history for all users.
- **Authentication Required:** Yes (`Authorization: Bearer <Your_Firebase_Token>`)
- **Success Response:**
  - **Code:** `200 OK`
  - **Content:** 
    ```json
    [
      {
        "firstName": "John",
        "lastName": "Doe",
        "totalClaimCharge": 4000.00,
        "dateRanges": [
          {
            "startDate": "2023-01-01",
            "endDate": "2023-01-07"
          }
        ]
      },
      {
        "firstName": "Jane",
        "lastName": "Doe",
        "totalClaimCharge": 3500.00,
        "dateRanges": [
          {
            "startDate": "2023-01-01",
            "endDate": "2023-01-07"
          }
        ]
      }
    ]
    ```
- **Error Response:**
  - **Code:** `500 Internal Server Error` if an error occurs during retrieval.

- **Sample cURL Command:**
  ```bash
  curl -X GET "http://yourdomain.com/api/billingHistory" \
     -H "Authorization: Bearer <Your_Firebase_Token>"
  ```
---
- 🔭 I’m currently working on this Automated Healthcare EDI Generator & Submission System along with other smaller AWS projects.
- 🌱 I’m continually learning and integrating new technologies like AWS to enhance system performance and scalability.

---
![GitHub Streak](https://streak-stats.demolab.com/?user=kenny-ahmedd)  
[![Top Langs](https://github-readme-stats.vercel.app/api/top-langs/?username=kenny-ahmedd)](https://github.com/anuraghazra/github-readme-stats)

(https://www.linkedin.com/in/kani-ahmed-343269232/)
***`Note`**: The system is currently in development, with API documentation and additional features to be released soon. Stay tuned!* Great things are on the way......Visit the Front-end before leaving.

Thank you!
