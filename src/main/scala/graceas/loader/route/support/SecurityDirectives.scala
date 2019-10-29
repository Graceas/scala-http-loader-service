package graceas.loader.route.support

import akka.http.scaladsl.server.directives.HeaderDirectives.headerValueByName
import akka.http.scaladsl.server.directives.{BasicDirectives, RouteDirectives}
import BasicDirectives._
import RouteDirectives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, ResponseEntity}
import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1, StandardRoute}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.Logger
import graceas.loader.config.Config
import graceas.loader.GraceasLoaderModule._
import graceas.loader.core.{AuthTokenContent, UserGroup, sync}

import pdi.jwt.exceptions.{JwtExpirationException, JwtValidationException}
import pdi.jwt.{Jwt, JwtAlgorithm}
import scaldi.Injectable

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}


case class AuthError(message: String) extends Exception(message)

object AuthError extends Injectable {

  private val jackson = inject[ObjectMapper with ScalaObjectMapper]

  implicit def authError2HttpEntity(authError: AuthError): ResponseEntity = {
    HttpEntity(
      contentType = ContentTypes.`application/json`,
      jackson.writeValueAsString(authError)
    )
  }
}

class CredentialsNotProvided(override val message: String = "Authentication credentials were not provided.") extends AuthError(message)

trait SecurityDirectives extends Injectable {
  private val logger = Logger[SecurityDirectives]
  private val AuthHeaderPrefix = "Bearer"
  private val jackson = inject[ObjectMapper with ScalaObjectMapper]
  private val secretKey = inject[Config].secretKey
  private val mockAuth = inject[Config].http.mockAuth

  private val mockAuthTokenContent = AuthTokenContent(
    userId      = "0b628710-a11b-4e44-a1ae-bb640d0be0ae",
    exp         = 0,
    origIat     = 0,
    group       = UserGroup.system.toString
  )

  private def authError(message: String): StandardRoute = {
    complete(HttpResponse(Unauthorized, entity = AuthError(message)))
  }

  def systemOnly: Directive1[AuthTokenContent] = {
    authenticate.flatMap { userToken =>
      if (userToken.group == UserGroup.system.toString) {
        provide(userToken)
      } else {
        authError("The endpoint requested is system-only")
      }
    }
  }

  def partnerOnly: Directive1[AuthTokenContent] = {
    authenticate.flatMap { userToken =>
      if (userToken.group == UserGroup.partner.toString) {
        provide(userToken)
      } else {
        authError("The endpoint requested is partner-only")
      }
    }
  }

  def userOnly: Directive1[AuthTokenContent] = {
    authenticate.flatMap { userToken =>
      if (userToken.group == UserGroup.user.toString) {
        provide(userToken)
      } else {
        authError("The endpoint requested is user-only")
      }
    }
  }

  def encodeToken(tokenContent: AuthTokenContent): String =
    Jwt.encode(jackson.writeValueAsString(tokenContent), secretKey, JwtAlgorithm.HS256)

  def tryAuth(token: String): Option[AuthTokenContent] = {
    if (token.startsWith(AuthHeaderPrefix)) {
      Jwt.decodeRaw(token.drop(AuthHeaderPrefix.length + 1), secretKey, Seq(JwtAlgorithm.HS256)).flatMap { decodedValue =>
        Try(jackson.readValue[AuthTokenContent](decodedValue))
      } match {
        case Success(user) => Some(user)
        case Failure(_)    => None
      }
    } else {
      None
    }
  }

  def authenticate: Directive1[AuthTokenContent] = {
    if (mockAuth) {
      provide(mockAuthTokenContent)
    } else {
      headerValueByName("Authorization").map { headerValue =>
        if (headerValue.startsWith(AuthHeaderPrefix)) {
          Jwt.decodeRaw(headerValue.drop(AuthHeaderPrefix.length + 1), secretKey, Seq(JwtAlgorithm.HS256)).flatMap { decodedValue =>
            Try(jackson.readValue[AuthTokenContent](decodedValue))
          }
        } else {
          Failure(new CredentialsNotProvided)
        }
      }.flatMap {
        case Success(user) =>
          provide(user)
        case Failure(ex) =>
          ex match {
            case _: JwtExpirationException =>
              authError("Signature has expired.")
            case _: CredentialsNotProvided =>
              authError("Authentication credentials were not provided.")
            case _: JwtValidationException =>
              authError("Error decoding signature.")
            case t =>
              logger.error("Error while authenticating with JWT", t)
              reject(AuthorizationFailedRejection)
          }
      }
    }
  }

}

object MockTokenEncode extends SecurityDirectives {

  private val mockAuthTokenContent = AuthTokenContent(
    userId   = "0b628710-a11b-4e44-a1ae-bb640d0be0ae",
    exp      = (System.currentTimeMillis() / 1000).toInt + 300000,
    origIat  = (System.currentTimeMillis() / 1000).toInt,
    group    = UserGroup.system.toString
  )

  def getMockAccessToken:String = {
    encodeToken(mockAuthTokenContent)
  }

  def getMockAccessTokenPartner:String = {
    encodeToken(mockAuthTokenContent.copy(group = UserGroup.partner.toString))
  }

  def getMockAccessTokenSystem:String = {
    encodeToken(mockAuthTokenContent.copy(group = UserGroup.system.toString))
  }

  def getMockAccessTokenUser(userId: String):String = {
    encodeToken(mockAuthTokenContent.copy(userId = userId, group = UserGroup.user.toString))
  }

}