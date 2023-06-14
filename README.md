# mimic-iv-to-fhir
Mapping of MIMIC-IV data to FHIR R4 by using toFHIR (https://github.com/srdc/tofhir) engine.

This project will provide the mapping definitions and configurations for converting MIMIC-IV v2.0 data (https://mimic.mit.edu/) 
into HL7 FHIR R4 resources and storing them to a FHIR server by using the toFHIR engine(https://github.com/srdc/tofhir). 
toFHIR is an easy-to-use data mapping and high-performant data transformation tool to transform existing datasets from 
various types of sources to HL7 FHIR. It provides a simple template based mapping language to define your mappings from 
your source data format into a specific FHIR resource. You can find more information in toFHIR GitHub page. 

The followings are the brief description of the folder hierarchy.
* In the ['schemas'](./schemas) older, you can find the schemas of tables for MIMIC-IV data in terms of FHIR StructureDefinition which is 
required by toFHIR to perform a quick validation on source data. For example, ['patients.json'](./schemas/hosp/patients.json) provides the schema of 
'patients' table under 'hosp' module of MIMIC-IV.
* In the ['mappings'](./mappings) folder, you can find mapping definitions each defining a mapping from a MIMIC-IV table to a specific 
FHIR resource. For example, ['patients.json'](./mappings/hosp/patients.json) provide the mapping that will convert each row in MIMIC-IV 'patient' table 
into a FHIR Patient resource.
* In the ['jobs'](./jobs) folder, you will find a range of job configurations that will convert data in a subset or all MIMIC-IV tables and 
store them into a configured FHIR server or file system. For example, ['mimic-hosp-csv-to-fhir-server.json'](./jobs/mimic-hosp-csv-to-fhir-server.json) 
will map all information in 'hosp' module given in CSV files into FHIR resources and store them a FHIR server where base 
URL can be provided with environment variable 'FHIR_REPO_URL'.
* In the ['terminology](./terminology) folder, you can find the CSV files that corresponds to FHIR ConceptMap or CodeSystem definitions 
used during some mapping. For example 'mimic_labitems_to_loinc.csv' includes the mapping of MIMIC-IV labitem identifiers 
to LOINC codes that is used for mapping 'labevents' into FHIR Observation resource to also convert the lab result code 
into LOINC.

The following sections describes the mappings defined until now and some assumptions as well as possibilities that you do 
with toFHIR while converting the data to FHIR.

## Mappings for Hosp module
### Mapping of ['patients'](https://mimic.mit.edu/docs/iv/modules/hosp/patients/) table into ['FHIR Patient'](http://www.hl7.org/FHIR/patient.html) resources
There is not much detail in this mapping, only we use anchor_year and anchor_age to calculate the birth year of patient 
to set birthDate element.

### Mapping of ['labevents'](https://mimic.mit.edu/docs/iv/modules/hosp/labevents/) table into ['FHIR Observation'](http://www.hl7.org/FHIR/observation.html) resources
* As you may see from the mapping definition, the mapping produces an FHIR Observation resource compliant with [US-Core Observation Lab profile](http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab). 
Similar to this you can update the mapping definition to produce resources that are compliant with other profiles you need.
* The 'itemid' that is the identifier indicating the lab test in MIMIC-IV data is mapped to a corresponding LOINC code. 
For this mapping, we have used the mapping [file](https://github.com/MIT-LCP/mimic-code/blob/main/mimic-iv/concepts/concept_map/d_labitems_to_loinc.csv) provided in [MIMIC-Code repository](https://github.com/MIT-LCP/mimic-code).
and convert it to the toFHIR CSV representation of FHIR ConceptMap, [mimic_labitems_to_loinc.csv](./terminology/mimic_labitems_to_loinc.csv).
Both the 'itemid' and the corresponding LOINC code (if exists) are set in Observation.code element as they may be both 
beneficial for the users. 
* As you may know, when the lab result is non-numeric, textual data is provided in MIMIC data. By using toFHIR terminology 
mapping facilities, we have shown how you can map some of these to corresponding LOINC or SNOMED-CT codes to enhance the 
semantics of your data while converting it to FHIR. The file ['labitem_coded_values_to_loinc.csv'](./terminology/labitem_coded_values_to_loinc.csv)
provides the ConceptMap file including some examples.For example, the values 'NEG' or 'NEGATIVE' are transformed into the LOINC 
code 'LA6577-6'. You can update the file with new mappings to map the values to your own value sets.
If the 'value' column is null (as there are many in the data), we also try to parse the 'comments' column and convert it to a corresponding code by 
writing some simple FHIR path expressions
* Numeric lab results provided in the 'valuenum' column as well as the statements like '>1.05' provided in the 
'value' column are converted to quantified values and set to Observation.valueQuantity.
* If we cannot extract a numeric or coded value with the above methods, then value is set as valueString in Observation.
* For 'specimen_id' column indicating the identifier of the specimen, we use FHIR Logical referencing as there is no further 
table or data related with this specimen to link with.
* For 'priority' column, we use an [extension](http://hl7.org/fhir/us/cdmh/StructureDefinition/cdmh-pcornet-lab-test-priority) defined by Common Data Models Harmonization (CDMH) project.

### Mapping of ['admissions'](https://mimic.mit.edu/docs/iv/modules/hosp/admissions/) table into ['FHIR Encounter'](http://www.hl7.org/FHIR/ecnounter.html) resources
* Based on the 'admission_type', we infer the category of the encounter (Encounter.class) by using the mapping context 
map file ['admission-type-concept-map'](mapping-contexts/admission-type-concept-map.csv). Context maps are used in toFHIR 
to get a corresponding value based on a key. The first column in the file is the key and other columns are the attributes 
for that key to access within your mappings. You can use 'mpp:getConcept(...)' function to access these attributes.
* We join the 'admissions' table with 'transfers' table to get the locations of patient during whole hospital stay and
use this information to populate 'Encounter.location' element. By using another mapping we map unique care units
listed in 'transfers' table into FHIR Location recources which we give reference to within 'Encounter.location.location'.
* We also join the 'admissions' table with 'services' table to get the services patient get during the encounter. We map 
these into encounter type (Encounter.type) using the original service code in MIMIC also mapping it to a corresponding 
SNOMED-CT code ([Mapping of Service codes to SNOMED-CT](/terminology/services-to-snomed.csv)).
* The 'admission_location' is similarly mapped to a suitable HL7 code from the [FHIR encounter-admit-source ValueSet](http://hl7.org/fhir/ValueSet/encounter-admit-source)  by using the ['admission-location-to-hl7.csv'](./terminology/admission-location-to-hl7.csv).
Note that the concept map also includes mapping the location into a SNOMED-CT code if there is no direct mapping to one of 
the concepts in the value set to distinguish such cases (e.g. WALK-IN/SELF REFERRAL).
* The 'discharge_location' is similarly mapped to a suitable  code from the [FHIR encounter-discharge-disposition ValueSet](http://hl7.org/fhir/ValueSet/encounter-discharge-disposition)  by using the ['discharge-location-to-hl7.csv'](./terminology/discharge-location-to-hl7.csv).
Note that the concept map also includes mapping the location into a SNOMED-CT code if there is no direct mapping to one of
the concepts in the value set to distinguish such cases (e.g. HOME HEALTH CARE).
* In order not to lose the information Emergency department admission and discharge times ('edregtime' and 'edouttime'), 
we use them in 'Encounter.location' element where the location is specified by the code indicating it is an Emergency 
department.
* The 'insurance' information is put as an extension to the resource with url 'https://mimic.mit.edu/fhir/StructureDefinition/ext-insurance'
with string value.
* Other demographic information ('race' and 'marital_status') are not directly put into Encounter resource as 
they are part of Patient resource in FHIR. toFHIR supports FHIR Patch interaction and enable users to 
map the content into FHIR Patch contents (FHIR Path Patch or JSON Patch) that are used to partially update a resource. 
We use this mechanism here and define another mapping expression within the same mapping to update FHIR Patient resources 
with this partial information.
  * The 'race' is mapped to an extension as defined in [US-Core](http://hl7.org/fhir/us/core/StructureDefinition/us-core-race) 
  where the race is mapped to a suitable code from [FHIR Race ValueSet](http://terminology.hl7.org/ValueSet/v3-Race) by 
  using the concept map ['race-to-hl7.csv'](./terminology/race-to-hl7.csv).
  * The 'marital_status' is mapped to 'Patient.maritalStatus' element by using the concept map ['marital-status-to-hl7.csv'](./terminology/marital-status-to-hl7.csv).
* Please note that, as there is no much information about the meaning of the concepts used for admission/discharge type, location 
we have used our best knowledge to choose the corresponding codes from the FHIR or SNOMED-CT codes. You can check and update 
the mappings according to your requirements.
* We also join the 'diagnoses_icd' and 'procedures_icd' tables into the 'admissions' table by using the 'hadm_id' column. 
Then we use the data to populate the 'Encounter.diagnosis' element with references to diagnoses and procedures applied 
during the admission.

### Mapping of ['diagnoses_icd'](https://mimic.mit.edu/docs/iv/modules/hosp/diagnoses_icd/) table into ['FHIR Condition'](http://hl7.org/fhir/condition.html) resources
* Unique identifier for a Condition resource is generated via hashing by using the 'hadm_id' and 'seq_no'.
* The ['d_icd_diagnoses'] table is also joined to the source data to get the display text for ICD codes.
* Category is indicated as 'encounter-diagnosis' for all as it is the logic for the MIMIC data.
* Given 'icd_code' is processed and converted to actual ICD-9-CM or ICD-10-CM code (with the dot) and set into 'Condition.code' element.
* ICD-9 codes are also mapped to ICD-10 codes by using the General Equivalence Mappings (GEM) file provided by CMS. The [mapping file](mapping-contexts/icd9toicd10cmgem.csv) is used as context map file in toFHIR. 
Only mappings that do not use combinations of codes (combination of ICD-10 codes for a single ICD-9 code) are performed.  

### Mapping of ['procedures_icd'](https://mimic.mit.edu/docs/iv/modules/hosp/procedures_icd/) table into ['FHIR Procedure'](http://hl7.org/fhir/procedure.html) resources
* Unique identifier for a Condition resource is generated via hashing by using the 'hadm_id' and 'seq_no'.
* The ['d_icd_procedures'] table is also joined to the source data to get the display text for ICD codes.
* For ICD-9 codes, given 'icd_code' is processed and converted to actual ICD-9-PCS code (with the dot) and set into 'Procedure.code' element.
* ICD-9 codes are also mapped to ICD-10 codes by using the General Equivalence Mappings (GEM) file provided by CMS. The [mapping file](mapping-contexts/icd9toicd10pcsgem.csv) is used as context map file in toFHIR.
Only mappings that do not use combinations of codes (combination of ICD-10 codes for a single ICD-9 code) are performed.

### Mapping of ['prescriptions'](https://mimic.mit.edu/docs/iv/modules/hosp/prescriptions/) table into ['FHIR MedicationRequest'](http://hl7.org/fhir/medicationrequest.html) and ['FHIR Medication'](http://hl7.org/fhir/medication.html) resources
In this mapping we have used FHIR Medication resources to represent the medications mentioned in MIMIC database. 
For each distinct medication (drug name + NDC code) in prescriptions table, we have created the FHIR Medication resource 
based on the information (drug, gsn, formulary_drug_cd, prod_strength, form_unit_disp) given in the table. The following items give information about this mapping.
* Unique identifier for a Medication resource is generated via hashing by using the 'drug', 'gsn', 'ndc', 'formulary_drug_cd'
* We have mapped 'ndc' and 'formulary_drug_cd' to Medication.code
* We use RxNorm API to retrieve the medication details given NDC code (corresponding RxNorm Concept id, its ingredients and their ATC codes, dose form, etc)
* By using the RxNorm API, we use the RxNorm Concept id for dose form of medication for medication form code (Medication.form)
* By using the RxNorm API, we use the corresponding ATC codes for found ingredients for medication and map the dose units 
and ingredient strengths coming from RxNorm API

For prescriptions;
* The route code given in the table is transformed to SNOMED-CT (http://hl7.org/fhir/ValueSet/route-codes)
* Dosing units are converted to UCUM or codes from http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm if possible

## How to run the ETL jobs
You can download the latest release of toFHIR from the GitHub page or download the source code and build it to get the 
toFHIR executable, an executable standalone JAR file. Copy the JAR file into this project directory.

Mapping jobs provided in the ...(to be continued)




