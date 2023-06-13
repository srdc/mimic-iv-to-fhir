package io.tofhir.mimic

import io.tofhir.engine.{Boot, Execution}
import io.tofhir.rxnorm.RxNormApiFunctionLibraryFactory

object MimicMappingEngine extends App {
  val rxNormApiFunctionLibraryFactory = new RxNormApiFunctionLibraryFactory("https://rxnav.nlm.nih.gov", 2)

  Boot.init(args, functionLibraryFactories = Map("rxn" -> rxNormApiFunctionLibraryFactory))
}
