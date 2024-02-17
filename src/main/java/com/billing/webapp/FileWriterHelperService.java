package com.billing.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
    * This class is used to create a file or files for each claim generated for each client
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class FileWriterHelperService {

    private static final Logger logger = LoggerFactory.getLogger(FileWriterHelperService.class);
    LocalDateTime currentDateTime = LocalDateTime.now(); // current date and time
    String currentTime = currentDateTime.format(DateTimeFormatter.ofPattern("HHmm")); // current time
    String bhtTransactionSetCreationTime = currentTime; // Transaction Set Creation Time
    //----------------------------------------------------------------------------------------------------------------------
    // Interchange Control Header (ISA)
    String isaAuthInfoQualifier = "00"; //00=no auth info present, 01=password, 02=sec cert, 03=pin, 04=sec token
    String isaAuthInfo = "          "; //10 spaces (if auth info qualifier is 00), else password, sec cert, pin, or sec token
    String isaSecurityInfoQualifier = "00"; //00=no security info present, 01=none, 02=DES, 03=3DES, 04=DES with MD5, 05=3DES with MD5, 06=DES with SHA-1, 07=3DES with SHA-1
    String isaSecurityInfo = "          "; //10 spaces (if security info qualifier is 00), else security info (e.g., password)
    String isaInterchangeIdQualifier = "ZZ"; //mutually defined (e.g., ZZ=Mutually Defined, 01=Duns (Dun & Bradstreet), 08=UCC EDI Communications ID (Comm ID), 12=Phone Number)
    @Value("${clearinghouse.id}")
    String isaInterchangeSenderId; // Clearinghouse ID (ISA06)

    //String isaInterChangeID
    @Value("${clearinghouse.id}")
    String isaInterchangeReceiverId; // Clearinghouse ID (ISA08)
    String isaInterChangeDate = currentDateTime.format(DateTimeFormatter.ofPattern("yyMMdd")); //current date
    String isaRepetitionSeparator = "^"; //component element separator (default is ^) (ISA16)
    String isaInterchangeControlVersionNumber = "00501"; //00501 for 5010, 00401 for 4010 (ISA12)
    String isaInterchangeControlNumber = ""; // Dynamic Control Number (ISA13) (9 digits) (e.g., 000000001)
    String isaAcknowledgmentRequested = "0"; // Is used for requesting an acknowledgment (ISA14) (0=no, 1=request) (default is 0)
    String isaUsageIndicator = "P"; //I=info, P=prod, T=test (ISA15)
    //----------------------------------------------------------------------------------------------------------------------
    // Functional Group Header (GS)
    String gsFunctionalIdentifierCode = "HC"; // HC=Health Care Claim (837), HP=Health Care Claim Payment Advice (835), or RA=Remittance Advice (820)
    @Value("${clearinghouse.id}")
    String gsApplicationSenderCode; // Clearinghouse ID (GS02)

    @Value("${clearinghouse.id}")
    String gsApplicationReceiverCode; // Clearinghouse ID (GS03)
    String gsTransactionDate = currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String gsGroupControlNumber = "000000001"; // Group Control Number (GS06) (9 digits) (e.g., 000000001)
    String gsResponsibleAgencyCode = "X"; //X: Accredited Standards Committee X12 (ASC X12)
    String gsVersion = "005010X222A1"; //EDI Standard: Version / Release / Industry Identifier Code (GS08)
    //----------------------------------------------------------------------------------------------------------------------
    // Transaction Set Header (ST)
    String stTransactionSetIdentifierCode = "837"; //837 for Health Care Claim
    String stTransactionSetControlNumber = "0001"; // Transaction Set Control Number (min=4, max=9 digits) (e.g., 0001)
    String stImplementationConventionReference = "005010X222A1"; //EDI Standard: Version / Release / Industry Identifier Code
    //----------------------------------------------------------------------------------------------------------------------
    // Beginning of Hierarchical Transaction (BHT)
    String bhtHierarchicalStructureCode = "0019"; //0019 for Information Source, Information Receiver, Subscriber, and Dependent
    String bhtTransactionSetPurposeCode = "00"; //00=Original, 01 Cancellation, 05=Replace, 18=Re-issue
    String bhtOriginatorApplicationTransactionIdentifier = "SDS146732000000136"; //The inventory file number of the transmission assigned by the submitter's system (Clearinghouse)
    String bhtTransactionSetCreationDate = currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String bhtTransactionTypeCode = "CH"; //type of transaction: Use CH when the transaction contains only fee for service claims, CH=Chargeable, RP=Reporting, TH=Therapy, IN=Information, AD=Adjustment
    //----------------------------------------------------------------------------------------------------------------------
    // Submitter Name and Other Identifier Information (NM1)
    String submitterEntityIdentifierCode = "41"; //41=submitter (clearinghouse) or 40=receiver (payer) or 85=provider (billing service) or 87=third party administrator
    String submitterEntityTypeQualifier = "2"; //Non-Person Entity (2 for Non-Person Entity) or Person (1 for Person)
    @Value("${submitter.name}")
    String submitterLastOrOrganizationName; //Submitter Last or Organization Name
    String submitterFirstName = ""; //not applicable since organization
    String submitterMiddleNameOrInitial = ""; //not applicable since organization
    String submitterIdCodeQualifier = "46"; //46=Electronic Transmitter Identification Number (ETIN),
    @Value("${clearinghouse.id}")
    String submitterIdentifier; //Clearinghouse ID (ETIN) assigned by the Centers for Medicare and Medicaid Services (CMS)
    //----------------------------------------------------------------------------------------------------------------------
    // Submitter EDI Contact Information (PER)
    String submitterContactFunctionCode = "IC"; //IC=Information Contact (the person or office to whom communications should be directed)
    @Value("${submitter.contact.name}")
    String submitterContactName; //Submitter Contact Name (Organizational name) or Submitter Contact Person Name (Last or Organization Name)
    String submitterCommunicationEmailQualifier = "EM"; //EM for email, TE for Telephone Number Qualifier (Telephone Number is the default)
    @Value("${submitter.contact.email}")
    String submitterContactEmail; //Submitter Contact Email
    String submitterCommunicationNumberQualifier = "TE"; //TE for Telephone Number Qualifier (Telephone Number is the default)
    @Value("${submitter.contact.phone}")
    String submitterContactPhoneNumber; //Submitter Contact Phone Number
    //----------------------------------------------------------------------------------------------------------------------
// Receiver Information(NM1)
    String receiverEntityIdentifierCode = "40"; //40=receiver (payer) or 41=submitter (clearinghouse) or 85=provider (billing service) or 87=third party administrator
    String receiverEntityTypeQualifier = "2"; //Non-Person Entity
    @Value("${submitter.name}")
    String receiverName; //Receiver Name (Organizational name)
    String receiverIdCodeQualifier = "46"; //46=Electronic Transmitter Identification Number (ETIN) or XX=Health Care Financing Administration National Plan ID
    @Value("${clearinghouse.id}")
    String receiverPrimaryIdentifier; //Clearinghouse ID (ETIN) assigned by the Centers for Medicare and Medicaid Services (CMS)
    //----------------------------------------------------------------------------------------------------------------------
    // Variables for HL1 segment
    String hierarchicalIdNumber = "1"; // HL01: Unique number assigned by the sender to identify a particular data segment in a hierarchical structure
    String hierarchicalParentId = ""; // No parent; this is the first level
    String hierarchicalLevelCode = "20"; // HL03: Code defining the characteristic of a level in a hierarchical structure. "20" typically represents Information Source
    String hierarchicalChildCode = "1"; // HL04: Code indicating if there are hierarchical child data segments subordinate to the level being described. "1" means there are additional subordinate HL data segments
    //----------------------------------------------------------------------------------------------------------------------
    // Provider Name/Identity and other Information (NM1)
    String providerEntityIdentifierCode = "85"; //85=provider (billing service) or 40=receiver (payer) or 41=submitter (clearinghouse) or 87=third party administrator
    String providerEntityTypeQualifier = "2"; //Non-Person Entity Qualifier
    @Value("${provider.name}")
    String providerLastOrOrganizationName; //Provider Last or Organization Name
    String providerFirstName = ""; //not applicable since organization
    String providerMiddleNameOrInitial = ""; //not applicable since organization
    String providerNameSuffix = ""; //not applicable since organization
    String providerIdCodeQualifier = "XX"; //XX=Centers for Medicare and Medicaid Services National Provider Identifier
    @Value("${provider.npi}")
    String providerIdentifier; //NPI assigned by the Centers for Medicare and Medicaid Services (CMS)
    //----------------------------------------------------------------------------------------------------------------------
    // Provider Address
    @Value("${provider.address.line}")
    String providerAddressLine; // Provider Address Line 1 (Required)
    @Value("${provider.city.name}")
    String providerCityName; // Provider City Name (Required)
    @Value("${provider.state.code}")
    String providerStateOrProvinceCode; // Provider State or Province Code (Optional, required if in the U.S. or Canada)
    @Value("${provider.zip.code}")
    String providerPostalZoneOrZipCode; // Provider Postal Zone or ZIP Code (Optional)
    //----------------------------------------------------------------------------------------------------------------------
    //Provider Tax Identification
    String providerReferenceIdQualifier = "EI"; //EI=employer's id num, or SY=social sec num, or PI=payer id num, or X5=state license number, or 0B=state Medicaid number, or G2=provider commercial number
    @Value("${provider.tax.id}")
    String providerTaxIdNumber; //Provider Tax Identification Number (Required) or Social Security Number (Required) or Employer Identification Number (Required)
    //----------------------------------------------------------------------------------------------------------------------
    // Provider Contact Information (PER)
    String providerContactFunctionCode = "IC"; //IC=Information Contact (the person or office to whom communications should be directed)
    @Value("${provider.name}")
    String providerContactName; //Provider Contact Name (Organizational name) or Provider Contact Person Name (Last or Organization Name)
    String providerCommunicationNumberQualifier = "TE"; //TE for Telephone Number Qualifier (Telephone Number is the default)
    @Value("${provider.contact.phone}")
    String providerContactPhoneNumber; //Provider Contact Phone Number
    //----------------------------------------------------------------------------------------------------------------------
    // Variables for HL1 segment
    String H2hierarchicalIdNumber = "2"; // HL01: Unique number assigned by the sender to identify a particular data segment in a hierarchical structure
    String H2hierarchicalParentId = "1"; // H1 is the parent; this is the second level (HL02) in the hierarchy (HL01) defined by the H1 segment above
    String H2hierarchicalLevelCode = "22"; // HL03: Code defining the characteristic of a level in a hierarchical structure. "20" typically represents Information Source
    String H2hierarchicalChildCode = "0"; // HL04: Code indicating if there are hierarchical child data segments subordinate to the level being described. "1" means there are additional subordinate HL data segments
    //----------------------------------------------------------------------------------------------------------------------
    //SBR: To record information specific to the primary insured and the insurance carrier for that insured
    String sbr01_payerResponsibilitySequenceNumberCode = "P"; // P=Primary (Required) or S=Secondary or T=Tertiary or 1=Primary or 2=Secondary or 3=Tertiary
    String sbr02_individualRelationshipCode = "18"; // 18=Self (Required) or 01=Spouse or 19=Child or 20=Employee or 21=Unknown or 39=Organ Donor or 40=Cadaver Donor or 53=Life Partner or G8=Other Relationship
    String sbr03_subscriberGroupOrPolicyNumber = ""; // Optional, left blank in the example
    String sbr04_subscriberGroupName = ""; // Optional, left blank in the example
    String sbr05_insuranceTypeCode = ""; // Optional, left blank in the example
    String sbr06_insuranceTypeCode = ""; //
    String sbr07_insuranceTypeCode = ""; //
    String sbr08_insuranceTypeCode = ""; //
    String sbr09_claimFilingIndicatorCode = "CI"; // CI=Commercial Insurance (Required) or MC=Medicare Part C or MA=Medicare Part A or MB=Medicare Part B or OF=Other Federal Program or CH=Medicaid
    //----------------------------------------------------------------------------------------------------------------------
    //NM1 Subscriber Identifiers
    String nm1_01_entityIdentifierCode = "IL"; // IL=Insured or Subscriber (Required) or PR=Payer or PE=Payee or QC=Patient or IN=Injured Party or 77=Attending Physician or DN=Referring Provider or 82=Rendering Provider
    String nm1_02_entityTypeQualifier = "1"; // 1=Person (Required) or 2=Non-Person Entity or 3=Non-Person Organization
    String nm1_05_subscriberMiddleNameOrInitial = ""; // Optional, Subscriber Middle Name or Initial
    String nm1_07_subscriberNameSuffix = ""; // Optional, Subscriber Name Suffix
    String nm1_08_identificationCodeQualifier = "MI"; // MI=Member Identification Number (Required) or PI=Payer Identification Number or XN=Health Insurance Claim (HIC) Number or SY=Social Security Number
    //----------------------------------------------------------------------------------------------------------------------
    //Subscriber Address
    @Value("${subscriber.address.line1}")
    String n301_subscriberAddressLine1; // Subscriber Address Line 1 (Required)
    String n302_subscriberAddressLine2 = ""; // Subscriber Address Line 2 (Optional)
    //---------------------------------------------------------------------------------------------------------------------
    //Client/Subscriber City, State, ZIP Code
    @Value("${subscriber.city.name}")
    String n401_subscriberCityName; // Subscriber City Name (Required)
    @Value("${subscriber.state.code}")
    String n402_subscriberStateCode; // Subscriber State Code (Optional, required if in the U.S. or Canada)
    String n404_countryCode = ""; // Country Code (Optional, required if N407 is present)
    String n407_countrySubdivisionCode = ""; // Country Subdivision Code (Optional)
    //----------------------------------------------------------------------------------------------------------------------
    //Subscriber Demographic Information
    String dmg01_dateTimePeriodFormatQualifier = "D8"; // D8=Date Expressed in Format CCYYMMDD (Required) or RD8=Range of Dates Expressed in Format CCYYMMDD-CCYYMMDD
    String dmg03_subscriberGenderCode = "F"; // Subscriber Gender Code (Required, "F" for Female, "M" for Male, "U" for Unknown)
    // ----------------------------------------------------------------------------------------------------------------------
    // Payer Name (NM1)
    String nm1_01_payerEntityIdentifierCode = "PR"; // PR=Payer (Required) or PE=Payee or IL=Insured or Subscriber or QC=Patient or IN=Injured Party or 77=Attending Physician or DN=Referring Provider or 82=Rendering Provider
    String nm1_02_payerEntityTypeQualifier = "2"; // 2=Non-Person Entity (Required) or 1=Person or 3=Non-Person Organization
    @Value("${payer.name}")
    String nm1_03_payerName; // Payer Name (Required) or Payee Name or Insured or Subscriber Name or Patient Name or Injured Party Name or Attending Physician Name or Referring Provider Name or Rendering Provider Name
    String nm1_08_payerIdentificationCodeQualifier = "PI"; // PI=Payer Identification Number (Required) or MI=Member Identification Number or XN=Health Insurance Claim (HIC) Number or SY=Social Security Number
    @Value("${payer.id}")
    String nm1_09_payerIdentifier; // Payer Identifier (Required) or Payee Identifier or Insured or Subscriber Identifier or Patient Identifier or Injured Party Identifier or Attending Physician Identifier or Referring Provider Identifier or Rendering Provider Identifier
    // ----------------------------------------------------------------------------------------------------------------------
    //CLM Claim Information
    String clm01_patientControlNumber = "NA"; // NA=Not Applicable (Required) or Patient Control Number (Required)
    String clm05_01_placeOfServiceCode = "12"; // 12=Home (Required part of Health Care Service Location Information)
    String clm05_02_facilityCodeQualifier = "B"; // B=Location Where Service is Rendered (Required part of Health Care Service Location Information)
    String clm05_03_claimFrequencyCode = "1"; // 1=Original (Required part of Health Care Service Location Information) or 7=Replacement or 8=Void or 9=Reopening of Prior Claim or 0=Not Specified or 4=Adjustment
    String clm06_providerOrSupplierSignatureIndicator = "Y"; // Y=Signature on File (Required) or N=Signature Not on File or P=Signature Generated by Provider or S=Signature Generated by Supplier or G=Signature Generated by Guarantor
    String clm07_assignmentOrPlanParticipationCode = "A"; // A=Assignment (Required) or B=No Assignment or C=Assignment Accepted on Clinical Lab Services Only or D=Assignment Not Accepted or P=Plan Participation
    String clm08_benefitsAssignmentCertificationIndicator = "Y"; // Y=Assignment of Benefits Accepted (Required) or N=Assignment of Benefits Not Accepted or P=Plan Participation or U=Benefits Assignment Certification Document on File or W=Waiver of Liability
    String clm09_releaseOfInformationCode = "Y"; // Y=Release of Information to Carrier or Intermediary (Required) or N=No Release of Information to Carrier or Intermediary or A=Release of Information with No Signature on File or B=Release of Information with Signature on File or C=Release of Information with Signature on File on Paper Media or D=Release of Information with Signature on File on Electronic Media
    // Define optional fields, setting them to empty or specific values as needed
    String clm10_patientSignatureSourceCode = ""; // Patient Signature Source Code (Optional) or Paper Document or Electronic Device or None on File
    String clm12_specialProgramIndicator = ""; // Special Program Indicator (Optional) or WC=Workers' Compensation or CH=Champus or CI=Commercial Insurance or HM=Health Maintenance Organization (HMO) or LI=Commercial Liability Insurance or LT=Lifetime Reserve Days or MA=Medicare Part A or MB=Medicare Part B or MC=Medicaid or OF=Other Federal Program or TV=Title V or VA=Veterans Affairs Plan or WC=Workers' Compensation
    String clm20_delayReasonCode = ""; // Delay Reason Code (Optional) or 01=The claim/service was submitted too late for filing consideration or 02=The time limit for filing has expired or 03=The time limit for filing has not expired or 04=This is not a Medicare Part B Drug CAP claim or 05=The payer does not cover this service for this patient or 06=The patient has not met the required eligibility requirements or 07=The patient has not met the required waiting requirements or 08=The claim/service was not submitted within the payer's specified time limit or 09=The time frame for filing has been overridden
    // ------------------------------------------------------------------------------------------------------------------
    //REF: Claim Identifier For Transmission Intermediaries
    String ref01_referenceIdentificationQualifier = "D9"; // D9=Claim Number (Required) or F8=Original Reference Number or G1=Prior Authorization Number or G3=Referral Number or GY=Statutory Exclusion Number or P4=Repriced Claim Reference Number or P5=Repriced Line Item Reference Number or P6=Adjusted Repriced Claim Reference Number or P7=Adjusted Repriced Line Item Reference Number or P8=Prior Payment Repriced Claim Reference Number or P9=Prior Payment Repriced Line Item Reference Number or T4=Line Item Control Number or X4=Provider Control Number or 6R=Managed Care Organization (MCO) Assigned Number or 9F=Repriced Claim Number or 9K=Repriced Line Item Reference Number or 9X=Adjusted Repriced Claim Number or 9Y=Adjusted Repriced Line Item Reference Number or FJ=Claim Adjustment Indicator or P4=Repriced Claim Reference Number or P5=Repriced Line Item Reference Number or P6=Adjusted Repriced Claim Reference Number or P7=Adjusted Repriced Line Item Reference Number or P8=Prior Payment Repriced Claim Reference Number or P9=Prior Payment Repriced Line Item Reference Number or T4=Line Item Control Number or X4=Provider Control Number or 6R=Managed Care Organization (MCO) Assigned Number or 9F=Repriced Claim Number or 9K=Repriced Line Item Reference Number or 9X=Adjusted Repriced Claim Number or 9Y=Adjusted Repriced Line Item Reference Number
    String ref02_valueAddedNetworkTraceNumber = "SDS146732000000136"; // Value Added Network Trace Number (Required) or Reference Identification (Required)
    //------------------------------------------------------------------------------------------------------------------
    //Medical Record Number
    String ref01_medicalRecordReferenceIdentificationQualifier = "EA"; // EA=Electronic Attachment Control Number (Required) or EJ=Patient Account Number or G1=Prior Authorization Number or G3=Referral Number or GY=Statutory Exclusion Number or P4=Repriced Claim Reference Number or P5=Repriced Line Item Reference Number or P6=Adjusted Repriced Claim Reference Number or P7=Adjusted Repriced Line Item Reference Number or P8=Prior Payment Repriced Claim Reference Number or P9=Prior Payment Repriced Line Item Reference Number or T4=Line Item Control Number or X4=Provider Control Number or 6R=Managed Care Organization (MCO) Assigned Number or 9F=Repriced Claim Number or 9K=Repriced Line Item Reference Number or 9X=Adjusted Repriced Claim Number or 9Y=Adjusted Repriced Line Item Reference Number
    String ref02_medicalRecordNumber = "NA"; // Medical Record Number (Required) or Reference Identification (Required), NA=Not Applicable
    //------------------------------------------------------------------------------------------------------------------
    //HI: Health Care Diagnosis Code
// Health Care Code Information (HI) - Diagnosis Type Code and Diagnosis Code
    String hi01_01_diagnosisTypeCode = "ABK"; // Diagnosis Type Code (Required, e.g., ABK for Principal Diagnosis) or BK=Admitting Diagnosis or ABF=Principal Diagnosis or ABK=Other Diagnosis or ABN=External Cause of Injury or ABP=External Cause of Injury or ABQ=External Cause of Injury or ABR=External Cause of Injury or ABS=External Cause of Injury or ABT=External Cause of Injury or ABU=External Cause of Injury or ABV=External Cause of Injury or ABW=External Cause of Injury or ABX=External Cause of Injury or ABY=External Cause of Injury or ABZ=External Cause of Injury or AC0=External Cause of Injury or AC1=External Cause of Injury or AC2=External Cause of Injury or AC3=External Cause of Injury or AC4=External Cause of Injury or AC5=External Cause of Injury or AC6=External Cause of Injury or AC7=External Cause of Injury or AC8=External Cause of Injury or AC9=External Cause of Injury or ACA=External Cause of Injury or ACB=External Cause of Injury or ACC=External Cause of Injury or ACD=External Cause of Injury or ACE=External Cause of Injury or ACF=External Cause of Injury or ACG=External Cause of Injury or ACH=External Cause of Injury or ACI=External Cause of Injury or ACJ=External Cause of Injury or ACK=External Cause of Injury or ACL=External Cause of Injury or ACM=External Cause of Injury or ACN=External Cause of Injury or ACO=External Cause of Injury or ACP=External Cause of Injury or ACQ=External Cause of Injury or ACR=External Cause of Injury or ACS=External Cause of Injury or ACT=External Cause of Injury or ACU=External Cause of Injury or ACV=External Cause of Injury or ACW=External Cause of Injury or ACX=External Cause of Injury or ACY=External Cause of Injury or ACZ=External Cause of Injury
    String hi01_02_diagnosisCode = "I10"; // I10=Essential (primary) hypertension (Required) or Diagnosis Code (Required)
    //------------------------------------------------------------------------------------------------------------------
    //Referring Provider Name
    String nm101_referringProviderEntityIdentifierCode = "DN"; // DN=Referring Provider (Required)
    String nm102_referringProviderEntityTypeQualifier = "1"; // Person (Required)
    @Value("${referring.provider.last.name}")
    String nm103_referringProviderLastName; // Referring Provider Last Name (Required)
    @Value("${referring.provider.first.name}")
    String nm104_referringProviderFirstName; // Referring Provider First Name (Optional)
    String nm105_referringProviderMiddleNameOrInitial = ""; // Referring Provider Middle Name or Initial (Optional)

    // Related Causes Information (C024) and other optional fields should be added similarly if required
    String nm107_referringProviderNameSuffix = ""; // Referring Provider Name Suffix (Optional)
    String nm108_referringProviderIdentificationCodeQualifier = "XX"; // XX = Centers for Medicare and Medicaid Services National Provider Identifier
    @Value("${referring.provider.identifier}")
    String nm109_referringProviderIdentifier; // Referring Provider Identifier (Optional)
    //------------------------------------------------------------------------------------------------------------------
    //not necessary in this case, but we will just include it
    String nm101_renderingProviderEntityIdentifierCode = "82"; // 82=Rendering Provider (Required)
    String nm102_renderingProviderEntityTypeQualifier = "1"; // Person (Required) or 2=Non-Person Entity
    @Value("${rendering.provider.name}")
    String nm103_renderingProviderLastNameOrOrganizationName; // Rendering Provider Last or Organization Name (Required)
    @Value("${rendering.provider.first.name}")
    String nm104_renderingProviderFirstName; // Rendering Provider First Name (Optional)
    String nm105_renderingProviderMiddleNameOrInitial = ""; // Rendering Provider Middle Name or Initial (Optional)
    String nm107_renderingProviderNameSuffix = ""; // Rendering Provider Name Suffix (Optional)
    String nm108_renderingProviderIdentificationCodeQualifier = "XX"; // XX = Centers for Medicare and Medicaid Services National Provider Identifier
    String nm109_renderingProviderIdentifier = ""; // Rendering Provider Identifier (Optional)
    //------------------------------------------------------------------------------------------------------------------
    //Other Subscriber Information
    //SBR
    String sbr01_otherPayerResponsibilitySequenceNumberCode = "P"; // Payer Responsibility Sequence Number Code (Required), P=Primary or S=Secondary or T=Tertiary or 1=Primary or 2=Secondary or 3=Tertiary
    String sbr02_otherIndividualRelationshipCode = "18"; // Individual Relationship Code (Required), 18=Self or 01=Spouse or 19=Child or 20=Employee or 21=Unknown or 39=Organ Donor or 40=Cadaver Donor or 53=Life Partner or G8=Other Relationship
    String sbr03_otherInsuredGroupOrPolicyNumber = ""; // Insured Group or Policy Number (Optional)
    String sbr04_otherInsuredGroupName = ""; // Other Insured Group Name (Optional)
    String sbr05_otherInsuranceTypeCode = ""; // Insurance Type Code (Optional)
    String sbr09_otherClaimFilingIndicatorCode = "CI"; // Claim Filing Indicator Code (Required), CI=Commercial Insurance or MC=Medicare Part C or MA=Medicare Part A or MB=Medicare Part B or OF=Other Federal Program or CH=Medicaid
    //------------------------------------------------------------------------------------------------------------------
    //Other Insurance Coverage Information
    String oi03_benefitsAssignmentCertificationIndicator = "Y"; // Benefits Assignment Certification Indicator (Required), Y=Assignment of Benefits Accepted or N=Assignment of Benefits Not Accepted or P=Plan Participation or U=Benefits Assignment Certification Document on File or W=Waiver of Liability
    String oi04_patientSignatureSourceCode = ""; // Patient Signature Source Code (Optional)
    String oi06_releaseOfInformationCode = "Y"; // Release of Information Code (Required), Y=Release of Information to Carrier or Intermediary or N=No Release of Information to Carrier or Intermediary or A=Release of Information with No Signature on File or B=Release of Information with Signature on File or C=Release of Information with Signature on File on Paper Media or D=Release of Information with Signature on File on Electronic Media
    //------------------------------------------------------------------------------------------------------------------
    //Other Subscriber/Insured Name
    String nm101_otherSubscriberEntityIdentifierCode = "IL"; // Insured or Subscriber (Required), IL=Insured or Subscriber or PR=Payer or PE=Payee or QC=Patient or IN=Injured Party or 77=Attending Physician or DN=Referring Provider or 82=Rendering Provider
    String nm102_otherSubscriberEntityTypeQualifier = "1"; // Person (Required), 1=Person or 2=Non-Person Entity or 3=Non-Person Organization
    String nm105_otherSubscriberMiddleName = ""; // Other Insured Middle Name (Optional)
    String nm107_otherSubscriberNameSuffix = ""; // Other Insured Name Suffix (Optional)
    String nm108_otherSubscriberIdentificationCodeQualifier = "MI"; // Identification Code Qualifier (Required), MI=Member Identification Number or PI=Payer Identification Number or XN=Health Insurance Claim (HIC) Number or SY=Social Security Number
    //------------------------------------------------------------------------------------------------------------------
    //Other Subscriber/Insured Address
    String n302_otherSubscriberAddressLine2 = ""; // Other Subscriber Address Line 2 (Optional)
    //------------------------------------------------------------------------------------------------------------------
    //Other Subscriber City, State, ZIP Code
    @Value("${subscriber.city.name}")
    String n401_otherSubscriberCityName; // Other Subscriber City Name (Required)
    @Value("${subscriber.state.code}")
    String n402_otherSubscriberStateOrProvinceCode; // Other Subscriber State or Province Code (Optional)
    @Value("${other.subscriber.zip.code}")
    String n403_otherSubscriberPostalZoneOrZIPCode; // Other Subscriber Postal Zone or ZIP Code (Optional)
    String n404_otherSubscriberCountryCode = ""; // Country Code (Optional)
    String n407_otherSubscriberCountrySubdivisionCode = ""; // Country Subdivision Code (Optional)
    //------------------------------------------------------------------------------------------------------------------
    //Other Payer Name
    String nm101_otherPayerEntityIdentifierCode = "PR"; // Payer (Required), PR=Payer or PE=Payee or IL=Insured or Subscriber or QC=Patient or IN=Injured Party or 77=Attending Physician or DN=Referring Provider or 82=Rendering Provider
    String nm102_otherPayerEntityTypeQualifier = "2"; // Non-Person Entity (Required), 2=Non-Person Entity or 1=Person or 3=Non-Person Organization
    @Value("${payer.name}")
    String nm103_otherPayerOrganizationName; // Other Payer Organization Name (Required)
    String nm108_otherPayerIdentificationCodeQualifier = "PI"; // Identification Code Qualifier (Required), PI=Payer Identification Number or MI=Member Identification Number or XN=Health Insurance Claim (HIC) Number or SY=Social Security Number
    @Value("${payer.id}")
    String nm109_otherPayerPrimaryIdentifier; // Other Payer Primary Identifier (Required)
    //------------------------------------------------------------------------------------------------------------------
    //SE: Transaction Set Trailer
    String se02_transactionSetControlNumber = stTransactionSetControlNumber; // Transaction Set Control Number (Required)
    //------------------------------------------------------------------------------------------------------------------
    //GE: Functional Group Trailer
    String ge01_numberOfTransactionSetsIncluded = "1"; // Number of Transaction Sets Included (Required), 1=One Transaction Set Included, increment for each additional transaction set
    String ge02_groupControlNumber = "000000001"; // Group Control Number (Required), must match GS06 in the header (GS) segment above (increment for each additional functional group)
    //------------------------------------------------------------------------------------------------------------------
    //Interchange Control Trailer
    String iea01_numberOfIncludedFunctionalGroups = "1"; // Number of Included Functional Groups (Required), 1=One Functional Group Included, increment for each additional functional group
    private LegacyService legacyService;
    @Autowired
    private SftpUploadService sftpUploadService; // Class to upload files to SFTP server
    @Autowired
    private LegacyDataRepository legacyDataRepository; // Repository to access legacy data from the database (MySQL)
    @Autowired
    private LegacyDataService legacyDataService;
    private String currentFileName; // Name of the current file being written to
    private BufferedWriter currentBufferedWriter; // Writer for the current file being written to (initialized in initializeFile())
    private List<String> generatedFilePaths = new ArrayList<>(); // List of file paths of generated files (used for SFTP upload)
    private int currentLxCounter = 1; // Counter for the current LX segment (incremented for each new LX segment) (initialized in initializeFile())
    private int FileNumber = 0; // Counter for the current file number (incremented for each new file) (initialized in initializeFile())
    //------------------------------------------------------------------------------------------------------------------
    //END OF VARIABLE DECLARATIONS HERE


    // This method is called by the LegacyService class to set the LegacyService bean (to avoid circular dependency issues)
    // Lazy load the LegacyService bean
    @Autowired
    public void setLegacyService(@Lazy LegacyService legacyService) {
        this.legacyService = legacyService;
    }

    public void writeFileLine(String line) throws IOException {
        // If the current file has reached the maximum number of LX segments, create a new file
        if (shouldCreateNewFile()) {
            // Close the current file writer
            closeWriter();
            //reset the currentLxCounter to 1 for the new file
            currentLxCounter = 1;
            // Create a new file with the same name as the current file, but with the file number appended to the end of the file name (before the file extension)
            initializeFile(this.currentFileName.substring(0, this.currentFileName.lastIndexOf(".")) + "_" + this.currentLxCounter + ".txt", legacyService.getTotalClaimSumCurrentFile());
        }
        // Write the line to the current file
        this.currentBufferedWriter.write(line);
        //flush the buffer to ensure the line is written to the file immediately (otherwise it will be written when the buffer is full)
        this.currentBufferedWriter.flush();
        // Increment the LX counter for the current file (incremented for each new LX segment)
        this.currentLxCounter++;
    }

    public void initializeFile(String fileName, Double totalClaimSumCurrentFile) throws IOException {
        // Create a new file with the given file name (the file will be created in the same directory as the application)
        this.currentFileName = fileName;
        // Create a new file writer for the new file (append to the file if it already exists)
        this.currentBufferedWriter = new BufferedWriter(new FileWriter(currentFileName));
        // Increment the file number (incremented for each new file)
        this.FileNumber++;
        // Set the currentLxCounter to 1 for the new file
        this.currentLxCounter = 1;
        // set the totalClaimSumCurrentFile to the totalClaimSumCurrentFile for the new file
        double clm02_totalClaimChargeAmount = totalClaimSumCurrentFile;
        //Add the file path of the new file to the list of generated file paths (used for SFTP upload) (the file path is the same as the file name since files are in the same directory as the application)
        generatedFilePaths.add(currentFileName);
        //Get the generated control number for the new file
        isaInterchangeControlNumber = generateRandomControlNumber();
        String nm1_03_subscriberLastName = legacyService.getLastName(); // Assign Subscriber Last Name
        String nm1_04_subscriberFirstName = legacyService.getFirstName(); // Assign Subscriber First Name
        String nm1_09_subscriberPrimaryIdentifier = legacyService.getIdNumber(); // Assign Subscriber Primary Identifier
        String n403_subscriberPostalZoneOrZIPCode = legacyService.getZipcode(); // Assign Subscriber Postal Zone or ZIP Code
        String dmg02_subscriberBirthDate = legacyService.getBirthday(); // Assign Subscriber Birth Date
        String nm103_otherSubscriberLastName = legacyService.getLastName(); // Assign Other Insured Last Name (Required)
        String nm104_otherSubscriberFirstName = legacyService.getFirstName(); // Assign Other Insured First Name (Required)
        String nm109_otherSubscriberIdentifier = legacyService.getIdNumber(); // Assign Other Insured Identifier (Required)
        String n301_otherSubscriberAddressLine1 = legacyService.getAddress(); // Assign Other Subscriber Address Line 1 (Required)

        /*
         * Write the ISA, GS, ST, BHT, NM1, PER, NM1, HL, NM1, N3, N4, REF, PER, HL, SBR, NM1, N3, N4, DMG, NM1, CLM segments to the file
         * The values for the segments are set in the variable declarations above or are passed from the LegacyService class (for variables that are set dynamically)
         * The purpose the code below overall is to write the segments to the file in the correct order and format (as specified in the 837P Implementation Guide)
         * The code below is not intended to be modified, but it can be modified if needed to meet the requirements of the 837P Implementation Guide
         * The group of segments below is run once for each file (the values are the same for all files of each client)
         * Lines are concatenated with the "+" operator to avoid having to use escape characters for the double quotes in the segments
         * The "~" character is used to indicate the end of a segment (as specified in the 837P Implementation Guide)
         * The "~\n" is used to indicate the end of a segment and the end of a line (the "\n" is a line feed character)
         * This can be optimized by using a StringBuilder instead of concatenating strings but I chose this for improved readability since it is run once for each file
         */
        currentBufferedWriter.write("ISA*" + isaAuthInfoQualifier + "*" + isaAuthInfo + "*" + isaSecurityInfoQualifier + "*" + isaSecurityInfo + "*" + isaInterchangeIdQualifier + "*" + isaInterchangeSenderId + "      *" + isaInterchangeIdQualifier + "*" + isaInterchangeReceiverId + "      *" + isaInterChangeDate + "*" + currentTime + "*" + isaRepetitionSeparator + "*" + isaInterchangeControlVersionNumber + "*" + isaInterchangeControlNumber + "*" + isaAcknowledgmentRequested + "*" + isaUsageIndicator + "*:~\n" +
                //GS: Functional Group Header (Required) - GS01 through GS08 are set in the variable declarations above (the values are the same for all files of each client)
                "GS*" + gsFunctionalIdentifierCode + "*" + gsApplicationSenderCode + "*" + gsApplicationReceiverCode + "*" + gsTransactionDate + "*" + currentTime + "*" + gsGroupControlNumber + "*" + gsResponsibleAgencyCode + "*" + gsVersion + "~\n" +
                //ST: Transaction Set Header (Required) - ST01 through ST03 are set in the variable declarations above (the values are the same for all files of each client)
                "ST*" + stTransactionSetIdentifierCode + "*" + stTransactionSetControlNumber + "*" + stImplementationConventionReference + "~\n" +
                //BHT: Beginning of Hierarchical Transaction (Required) - BHT01 through BHT06 are set in the variable declarations above (the values are the same for all files of each client)
                "BHT*" + bhtHierarchicalStructureCode + "*" + bhtTransactionSetPurposeCode + "*" + bhtOriginatorApplicationTransactionIdentifier + "*" + bhtTransactionSetCreationDate + "*" + bhtTransactionSetCreationTime + "*" + bhtTransactionTypeCode + "~\n" +
                //NM1: Submitter Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + submitterEntityIdentifierCode + "*" + submitterEntityTypeQualifier + "*" + submitterLastOrOrganizationName + "*" + submitterFirstName + "*" + submitterMiddleNameOrInitial + "***" + submitterIdCodeQualifier + "*" + submitterIdentifier + "~\n" +
                //PER: Submitter EDI Contact Information (Required) - PER01 through PER06 are set in the variable declarations above (the values are the same for all files of each client)
                "PER*" + submitterContactFunctionCode + "*" + submitterContactName + "*" + submitterCommunicationEmailQualifier + "*" + submitterContactEmail + "*" + submitterCommunicationNumberQualifier + "*" + submitterContactPhoneNumber + "~\n" +
                //NM1: Receiver Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + receiverEntityIdentifierCode + "*" + receiverEntityTypeQualifier + "*" + receiverName + "*****" + receiverIdCodeQualifier + "*" + receiverPrimaryIdentifier + "~\n" +
                //HL: Information Source Level (Required) - HL01 through HL04 are set in the variable declarations above (the values are the same for all files of each client)
                "HL*" + hierarchicalIdNumber + "*" + hierarchicalParentId + "*" + hierarchicalLevelCode + "*" + hierarchicalChildCode + "~\n" +
                //NM1: Information Source Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + providerEntityIdentifierCode + "*" + providerEntityTypeQualifier + "*" + providerLastOrOrganizationName + "*" + providerFirstName + "*" + providerMiddleNameOrInitial + "**" + providerNameSuffix + "*" + providerIdCodeQualifier + "*" + providerIdentifier + "~\n" +
                //N3: Information Source Address (Required) - N301 through N304 are set in the variable declarations above (the values are the same for all files of each client)
                "N3*" + providerAddressLine + "~\n" +
                //N4: Information Source City, State, ZIP Code (Required) - N401 through N404 are set in the variable declarations above (the values are the same for all files of each client)
                "N4*" + providerCityName + "*" + providerStateOrProvinceCode + "*" + providerPostalZoneOrZipCode + "~\n" +
                //REF: Information Source Additional Identification (Required) - REF01 through REF02 are set in the variable declarations above (the values are the same for all files of each client)
                "REF*" + providerReferenceIdQualifier + "*" + providerTaxIdNumber + "~\n" +
                //PER: Information Source EDI Contact Information (Required) - PER01 through PER06 are set in the variable declarations above (the values are the same for all files of each client)
                "PER*" + providerContactFunctionCode + "*" + providerContactName + "*" + providerCommunicationNumberQualifier + "*" + providerContactPhoneNumber + "~\n" +
                //HL: Information Receiver Level (Required) - HL01 through HL04 are set in the variable declarations above (the values are the same for all files of each client)
                "HL*" + H2hierarchicalIdNumber + "*" + H2hierarchicalParentId + "*" + H2hierarchicalLevelCode + "*" + H2hierarchicalChildCode + "~\n" +
                //SBR: Subscriber Information (Required) - SBR01 through SBR09 are set in the variable declarations above (the values are the same for all files of each client)
                "SBR*" + sbr01_payerResponsibilitySequenceNumberCode + "*" + sbr02_individualRelationshipCode + "*" + sbr03_subscriberGroupOrPolicyNumber + "*" + sbr04_subscriberGroupName + "*" + sbr05_insuranceTypeCode + "*" + sbr06_insuranceTypeCode + "*" + sbr07_insuranceTypeCode + "*" + sbr08_insuranceTypeCode + "*" + sbr09_claimFilingIndicatorCode + "~\n" +
                //NM1: Subscriber Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + nm1_01_entityIdentifierCode + "*" + nm1_02_entityTypeQualifier + "*" + nm1_03_subscriberLastName + "*" + nm1_04_subscriberFirstName + "*" + nm1_05_subscriberMiddleNameOrInitial + "**" + nm1_07_subscriberNameSuffix + "*" + nm1_08_identificationCodeQualifier + "*" + nm1_09_subscriberPrimaryIdentifier + "~\n" +
                //N3: Subscriber Address (Required) - N301 through N304 are set in the variable declarations above (the values are the same for all files of each client)
                "N3*" + n301_subscriberAddressLine1 + "~\n" +
                //N4: Subscriber City, State, ZIP Code (Required) - N401 through N404 are set in the variable declarations above (the values are the same for all files of each client)
                "N4*" + n401_subscriberCityName + "*" + n402_subscriberStateCode + "*" + n403_subscriberPostalZoneOrZIPCode + "~\n" +
                //DMG: Subscriber Demographic Information (Required) - DMG01 through DMG03 are set in the variable declarations above (the values are the same for all files of each client)
                "DMG*" + dmg01_dateTimePeriodFormatQualifier + "*" + dmg02_subscriberBirthDate + "*" + dmg03_subscriberGenderCode + "~\n" +
                //NM1: Payer Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + nm1_01_payerEntityIdentifierCode + "*" + nm1_02_payerEntityTypeQualifier + "*" + nm1_03_payerName + "*****" + nm1_08_payerIdentificationCodeQualifier + "*" + nm1_09_payerIdentifier + "~\n" +
                //CLM: Claim Information (Required) - CLM01 through CLM09 are set in the variable declarations above (the values are the same for all files of each client)
                "CLM*" + clm01_patientControlNumber + "*" + clm02_totalClaimChargeAmount + "***" + clm05_01_placeOfServiceCode + ":" + clm05_02_facilityCodeQualifier + ":" + clm05_03_claimFrequencyCode + "*" + clm06_providerOrSupplierSignatureIndicator + "*" + clm07_assignmentOrPlanParticipationCode + "*" + clm08_benefitsAssignmentCertificationIndicator + "*" + clm09_releaseOfInformationCode + "~\n" + // calculate total claimed excluding skipped dates
                //REF: Claim Identifier For Transmission Intermediaries (Required) - REF01 through REF02 are set in the variable declarations above (the values are the same for all files of each client)
                "REF*" + ref01_referenceIdentificationQualifier + "*" + ref02_valueAddedNetworkTraceNumber + "~\n" +
                //REF: Medical Record Number (Required) - REF01 through REF02 are set in the variable declarations above (the values are the same for all files of each client)
                "REF*" + ref01_medicalRecordReferenceIdentificationQualifier + "*" + ref02_medicalRecordNumber + "~\n" +
                //HI: Health Care Diagnosis Code (Required) - HI01 through HI02 are set in the variable declarations above (the values are the same for all files of each client)
                "HI*" + hi01_01_diagnosisTypeCode + ":" + hi01_02_diagnosisCode + "~\n" +
                //NM1: Referring Provider Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + nm101_referringProviderEntityIdentifierCode + "*" + nm102_referringProviderEntityTypeQualifier + "*" + nm103_referringProviderLastName + "*" + nm104_referringProviderFirstName + "*" + nm105_referringProviderMiddleNameOrInitial + "**" + nm107_referringProviderNameSuffix + "*" + nm108_referringProviderIdentificationCodeQualifier + "*" + nm109_referringProviderIdentifier + "~\n" +
                //NM1: Rendering Provider Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + nm101_renderingProviderEntityIdentifierCode + "*" + nm102_renderingProviderEntityTypeQualifier + "*" + nm103_renderingProviderLastNameOrOrganizationName + "*" + nm104_renderingProviderFirstName + "~\n" +
                //NM1: Other Subscriber Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "SBR*" + sbr01_otherPayerResponsibilitySequenceNumberCode + "*" + sbr02_otherIndividualRelationshipCode + "*" + sbr03_otherInsuredGroupOrPolicyNumber + "*" + sbr04_otherInsuredGroupName + "*" + sbr05_otherInsuranceTypeCode + "****" + sbr09_otherClaimFilingIndicatorCode + "~\n" +
                //OI: Other Insurance Coverage Information (Required) - OI01 through OI06 are set in the variable declarations above (the values are the same for all files of each client)
                "OI***" + oi03_benefitsAssignmentCertificationIndicator + "*" + oi04_patientSignatureSourceCode + "**" + oi06_releaseOfInformationCode + "~\n" +
                //NM1: Other Subscriber/Insured Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + nm101_otherSubscriberEntityIdentifierCode + "*" + nm102_otherSubscriberEntityTypeQualifier + "*" + nm103_otherSubscriberLastName + "*" + nm104_otherSubscriberFirstName + "*" + nm105_otherSubscriberMiddleName + "**" + nm107_otherSubscriberNameSuffix + "*" + nm108_otherSubscriberIdentificationCodeQualifier + "*" + nm109_otherSubscriberIdentifier + "~\n" +
                //N3: Other Subscriber/Insured Address (Required) - N301 through N304 are set in the variable declarations above (the values are the same for all files of each client)
                "N3*" + n301_otherSubscriberAddressLine1 + "~\n" +
                //N4: Other Subscriber/Insured City, State, ZIP Code (Required) - N401 through N404 are set in the variable declarations above (the values are the same for all files of each client)
                "N4*" + n401_otherSubscriberCityName + "*" + n402_otherSubscriberStateOrProvinceCode + "*" + n403_otherSubscriberPostalZoneOrZIPCode + "~\n" +
                //NM1: Other Payer Name (Required) - NM101 through NM109 are set in the variable declarations above (the values are the same for all files of each client)
                "NM1*" + nm101_otherPayerEntityIdentifierCode + "*" + nm102_otherPayerEntityTypeQualifier + "*" + nm103_otherPayerOrganizationName + "*****" + nm108_otherPayerIdentificationCodeQualifier + "*" + nm109_otherPayerPrimaryIdentifier + "~\n"
        );
    }

    // Generate a random 9-digit control number for the ISA segment (ISA13)
    // This method is called by the initializeFile() method above
    private String generateRandomControlNumber() {
        Random random = new Random();
        int number = random.nextInt(999999999); // Generates a random number up to 9 digits
        return String.format("%09d", number); // Formats to a 9-digit number with leading zeros if necessary
    }

    // Close the current file writer
    // This method is called by the LegacyService class after all files have been generated
    // This method is also called by the writeFileLine() method above if the current file has reached the maximum number of LX segments
    public void closeWriter() throws IOException {
        writeFooter(); // Ensure footer is written
        //the if statement below is to ensure that the currentBufferedWriter is not null before closing it
        if (this.currentBufferedWriter != null) {
            this.currentBufferedWriter.close();
        }
    }

    /*
     * Write the SE, GE, and IEA segments to the file
     * The method calls the findSegmentCount() method to calculate the SE01 value (the number of segments in the file)
     * The method also calls the generateRandomControlNumber() method to generate a new control number for the IEA segment
     * The method is called by the closeWriter() method above for each file after all other segments have been written
     */
    public void writeFooter() throws IOException {
        int se01_transactionSegmentCount = findSegmentCount(currentLxCounter); // Transaction Segment Count (Required)
        var iea02_interchangeControlNumber = isaInterchangeControlNumber;
        //SE: Transaction Set Trailer (Required) - SE01 through SE02 are set in the variable declarations above (the values are the same for all files of each client)
        this.currentBufferedWriter.write("SE*" + se01_transactionSegmentCount + "*" + se02_transactionSetControlNumber + "~\n" +
                //GE: Functional Group Trailer (Required) - GE01 through GE02 are set in the variable declarations above (the values are the same for all files of each client)
                "GE*" + ge01_numberOfTransactionSetsIncluded + "*" + ge02_groupControlNumber + "~\n" +
                //IEA: Interchange Control Trailer (Required) - IEA01 through IEA02 are set in the variable declarations above (the values are the same for all files of each client)
                "IEA*" + iea01_numberOfIncludedFunctionalGroups + "*" + iea02_interchangeControlNumber + "~");
    }

    /*
     * This method is called by the writeFooter() method above to calculate the SE01 value (the number of segments in the file)
     * The method is called by the writeFooter() method above for each file after all other segments have been written
     */
    public int findSegmentCount(int LxCounter) {
        // The number of lines starting from ST up to but not including LX1 = fixed = 30 (changes if additional segments are added)
        int countOtherRequiredClaimInfoStartingFromST = 30;
        // The number of lines LxCounter contains = fixed = 3 (changes if additional segments are added)
        int LxCounterLines = 3;

        // LxCounter will be +1 more than its actual count once we hit the end
        // We need to subtract 1 to get the actual count
        int adjustedLxCounter = LxCounter - 1;

        //add one to account for that one extra line for line of SE segment
        int segmentEnd = 1;

        // The total number of segments in the file = countOtherRequiredClaimInfoStartingFromST + (LxCounterLines * adjustedLxCounter) + segmentEnd
        int segmentCount;
        segmentCount = countOtherRequiredClaimInfoStartingFromST + (LxCounterLines * adjustedLxCounter) + segmentEnd;

        return segmentCount;
    }


    /*
     * This method checks if the current file has reached the maximum number of LX segments
     * The method is called by the writeFileLine() method above
     * The method is also called by the LegacyService class to determine if a new file should be created
     * The method returns true if the current file has reached the maximum number of allowable LX segments
     * The method returns false if the current file has not reached the maximum number of allowable LX segments
     *
     */
    public boolean shouldCreateNewFile() {
        var allowableClaimLength = 50; // The maximum number of allowable LX segments per file (depends on clearinghouse requirements)
        return this.currentLxCounter > allowableClaimLength; // Return true if the current file has reached the maximum number of allowable LX segments
    }

    //manage a single SFTP session for multiple uploads

    /*
     * This method is called by the LegacyService class after all files have been generated to upload the files to the SFTP server
     * The method calls the connect() method in the SftpUploadService class to connect to the SFTP server
     * The method calls the uploadFile() method in the SftpUploadService class to upload each file to the SFTP server
     * The method calls the disconnect() method in the SftpUploadService class to disconnect from the SFTP server after all files have been uploaded
     */
    public void manageFileUploads(List<String> files) {
        // The files parameter is a list of file paths of generated files (used for SFTP upload)
        // The files parameter is passed from the LegacyService class
        try {
            // Connect to the SFTP server
            sftpUploadService.connect(); // Connect once at the beginning

            // Upload each file to the SFTP server
            // The uploadFile() method is called for each file in the list of file paths of generated files (used for SFTP upload)
            for (String file : files) {
                // Get just the name of the file from the full path
                String localFileName = new File(file).getName();

                // Define the remote file path in the 'in/prod' directory (specified by the SFTP server administrator)
                String remoteFilePath = "" + localFileName;

                // Upload each file to the specified remote directory
                sftpUploadService.uploadFile(file, remoteFilePath);
            }
        } finally {
            sftpUploadService.disconnect(); // Disconnect after all uploads are complete
        }
    }

    public int getCurrentLxCounter() {
        return this.currentLxCounter;
    }

    public void setCurrentLxCounter(int currentLxCounter) {
        this.currentLxCounter = currentLxCounter;
    }

    public String getCurrentFileName() {
        return this.currentFileName;
    }

    // Return a copy of the list of file paths of generated files (used for SFTP upload)
    // This method is called by the LegacyService class after all files have been generated
    public List<String> getGeneratedFilePaths() {
        return new ArrayList<>(generatedFilePaths); // Return a copy of the list of file paths of generated files
    }
}