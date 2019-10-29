package graceas.loader.utils

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.google.gson.JsonObject


trait JsonFormatter {

  def serialize(value: Any): String =
    JsonFormatter.mapper.writeValueAsString(value)

  def serializePretty(value: Any): String =
    JsonFormatter.mapper.writer.withDefaultPrettyPrinter().writeValueAsString(value)

  def deserialize[T: Manifest](value: String): T =
    JsonFormatter.mapper.readValue(value, typeReference[T])

  def deserialize[T: Manifest](value: JsonObject): T =
    JsonFormatter.mapper.readValue(value.toString, typeReference[T])

  def deserialize[T: Manifest](value: Array[Byte]): T =
    deserialize[T](new String(value))

  def deserialize[T: Manifest](value: String, clazz: Class[T]): T =
    deserialize(value)

  def deserialize[T: Manifest](value: Array[Byte], clazz: Class[T]): T =
    deserialize(value)

  def typeReference[T: Manifest]: TypeReference[T] = new TypeReference[T] {
    override def getType: Type = typeFromManifest(manifest[T])
  }

  private def typeFromManifest(m: Manifest[_]): Type =
    if (m.typeArguments.isEmpty) {
      m.runtimeClass
    } else {
      new ParameterizedType {
        def getRawType: Class[_] = m.runtimeClass
        def getActualTypeArguments: Array[Type] = m.typeArguments.map(typeFromManifest).toArray
        def getOwnerType: Null = null
      }
    }
}


object JsonFormatter extends JsonFormatter {

  val mapper: ObjectMapper with ScalaObjectMapper = createMapper()

  val factory = new MappingJsonFactory(mapper)

  def createMapper(): ObjectMapper with ScalaObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper
  }
}