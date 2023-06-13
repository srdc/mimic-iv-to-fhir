package io.tofhir.mimic

import io.tofhir.engine.model.{FhirMappingJob, LocalFhirTerminologyServiceSettings, TerminologyServiceSettings}
import io.tofhir.engine.util.FhirMappingJobFormatter
import io.tofhir.engine.util.FhirMappingJobFormatter.formats
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MimicMappingJobTest extends AnyFlatSpec with Matchers{

  val terminologySettings = """{
                              |    "jsonClass": "LocalFhirTerminologyServiceSettings",
                              |    "folderPath": "./terminology",
                              |    "conceptMapFiles": [
                              |      {
                              |        "name": "labitems-to-loinc.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhirmimic-hosp-csv-to-fhir-server.json/ConceptMap/labitems-to-loinc",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/labitems",
                              |        "targetValueSetUrl": "http://loinc.org/vs"
                              |      },
                              |      {
                              |        "name": "labitem-coded-values-to-loinc.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/labitem-coded-values-to-other",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/labitem-coded-values",
                              |        "targetValueSetUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ValueSet/labitem-coded-values"
                              |      },
                              |      {
                              |        "name": "admission-type-to-snomedct.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/admission-type-to-snomedct",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/admission-types",
                              |        "targetValueSetUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ValueSet/admision-types"
                              |      },
                              |      {
                              |        "name": "admission-location-to-hl7.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/admission-location-to-hl7",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/admission-locations",
                              |        "targetValueSetUrl": "http://hl7.org/fhir/ValueSet/encounter-admit-source"
                              |      },
                              |      {
                              |        "name": "discharge-location-to-hl7.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/discharge-location-to-hl7",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/discharge-locations",
                              |        "targetValueSetUrl": "http://hl7.org/fhir/ValueSet/encounter-discharge-disposition"
                              |      },
                              |      {
                              |        "name": "marital-status-to-hl7.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/marital-status-to-hl7",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/marital-status",
                              |        "targetValueSetUrl": "http://hl7.org/fhir/ValueSet/marital-status"
                              |      },
                              |      {
                              |        "name": "race-to-hl7.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/race-to-hl7",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/race",
                              |        "targetValueSetUrl": "http://terminology.hl7.org/ValueSet/v3-Race"
                              |      },
                              |      {
                              |        "name": "medication-units-to-ucum-or-drugform.csv",
                              |        "conceptMapUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ConceptMap/medication-units-to-ucum-or-drugform",
                              |        "sourceValueSetUrl": "https://mimic.mit.edu/fhir/ValueSet/medication-form-units",
                              |        "targetValueSetUrl": "https://github.com/srdc/mimic-iv-to-fhir/fhir/ValueSet/medication-units"
                              |      }
                              |    ],
                              |    "codeSystemFiles": [
                              |      {
                              |        "name": "labitems-code-system.csv",
                              |        "codeSystem": "https://mimic.mit.edu/fhir/CodeSystem/labitems"
                              |      }
                              |    ]
                              |  }""".stripMargin


  "MimicMappingKJob" should "be loaded correctly" in {
    val settings = org.json4s.jackson.JsonMethods.parse(terminologySettings).extract[TerminologyServiceSettings]
    1 === 1
  }

}
