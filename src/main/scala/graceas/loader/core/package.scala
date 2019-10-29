package graceas.loader

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

package object core {
  type Id = String
  type UserID = String
  type RoomID = String
  type OptionalId = Option[Id]

  val timeout: FiniteDuration = 5.seconds

  final case class AuthTokenContent(
    userId:  UserID,
    exp:     Int,
    origIat: Int,
    group:   String
  )

  final object UserGroup extends Enumeration {
    type UserGroup = Value
    val system, partner, user = Value
  }

  def sync[T](future: Future[T]): T = {
    Await.result(future, timeout)
  }

  class ChatException(val message: String) extends Exception(message)
  class InvalidIncomingMessageException(message: String) extends ChatException(message)
  class UndefinedMessageTypeException(message: String) extends ChatException(message)
  class UserNotFoundException(message: String) extends ChatException(message)
  class RoomNotFoundException(message: String) extends ChatException(message)
}
