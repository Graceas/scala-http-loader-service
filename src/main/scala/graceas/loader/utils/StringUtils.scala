package graceas.loader.utils

import java.util.Base64

object StringUtils {

  implicit class StringUtilsExtensions(val self: String) extends AnyVal {
    def stripMargins(margin: String): String = self.stripPrefix(margin).stripSuffix(margin)
  }

  implicit class ByteArrayToBase64StringExtensions(val array: Array[Byte]) extends AnyVal {
    def asBase64(): String = Base64.getEncoder.encodeToString(array)
  }

  implicit class ByteArrayFromBase64StringExtensions(val string: String) extends AnyVal {
    def fromBase64(): Array[Byte] = Base64.getDecoder.decode(string)
  }

  def byteArrayToHex(input: Array[Byte]): String = {
    input.map("%02X" format _).mkString
  }

  def cropMessage(message: String, length: Int = 20): String = {
    if (message.length > length) message.substring(0, length).trim + "..." else message
  }
}
