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
store them into a configured FHIR server or file system.
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

## How to run the ETL jobs
You can download the latest release of toFHIR from the GitHub page or download the source code and build it to get the 
toFHIR executable, an executable standalone JAR file. Copy the JAR file into this project directory.

Mapping jobs provided in the ...(to be continued)




