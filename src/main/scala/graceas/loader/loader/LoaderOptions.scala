package graceas.loader.loader

import com.fasterxml.jackson.core.`type`.TypeReference

object LoaderOptions extends Enumeration {
  type LoaderOptions = Value
  val RETURN_CONTENT, RETURN_REQUEST = Value
}

class LoaderOptionsTypeReference extends TypeReference[LoaderOptions.type]