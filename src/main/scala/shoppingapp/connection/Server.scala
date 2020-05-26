package shoppingapp.connection

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.ActorMaterializer
import shoppingapp.model._
import shoppingapp.{AppLogging, Registry, RequestHandler}
import spray.json.JsonParser.ParsingException
import spray.json.{DeserializationException, _}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class Server(handler: RequestHandler) extends HttpApp

  with AppLogging
  with UserJsonSupport
  with ShoppingProductJsonSupport
  with CardItemsJsonSupport {

  implicit val system: ActorSystem = Registry.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  override def routes: server.Route = {
    apiRoutes
  }


  //Routes that map http endpoints to methods in this object

  def apiRoutes: server.Route =

  /** **************BASIC OPERATIONS ****************/
    pathPrefix("shop") {
      pathPrefix("users") {
        path("add") {
          entity(as[String]) {
            jsonString => addUser(jsonString)
          }
        }
      } ~
        pathPrefix("products") {
          path("add") {
            entity(as[String]) {
              jsonString => addProduct(jsonString)
            }
          } ~
            path("carditems") {
              entity(as[String]) {
                jsonString => AddCardItems(jsonString)
              }
            } ~
            path("checkout") {
              checkout()
            }
        }
    }

  /**Creates a new user for the website
    *
    * @param UserString
    * @return
    */
  def addUser(UserString: String): server.Route = Route.seal {


    post {
      log.debug(s"POST shop/users/add has been called, parameter is: $UserString")
      try {
        val paramInstance: ShoppingUser = UserString.parseJson.convertTo[ShoppingUser](authShoppingUserFormat)
        handler.handleAddUser(paramInstance) match {
          case Success(userId) =>
            complete {
              userId.toString
            }
          case Failure(ex) =>
            log.error(ex, "Failed to handle create user request.")
            complete(HttpResponse(StatusCodes.BadRequest, entity = "Username already taken."))
        }
      } catch {
        case dx: DeserializationException =>
          log.error(dx, "Deserialization exception")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter delphi user with message ${dx.getMessage}."))
        case px: ParsingException =>
          log.error(px, "Failed to parse JSON while registering")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Failed to parse JSON entity with message ${px.getMessage}"))
        case x: Exception =>
          log.error(x, "Uncaught exception while deserializing.")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "An internal server error occurred."))
      }

    }
  }

  /**Creates a new product for the website
    *
    * @param ProductString
    * @return
    */
  def addProduct(ProductString: String): server.Route = Route.seal {

    post {
      log.debug(s"POST shop/products/add has been called, parameter is: $ProductString")
      try {
        val paramInstance: ShoppingProduct = ProductString.parseJson.convertTo[ShoppingProduct](ShoppingProductFormat)
        handler.handleAddProduct(paramInstance) match {
          case Success(productId) =>
            complete {
              productId.toString
            }
          case Failure(ex) =>
            log.error(ex, "Failed to add product.")
            complete(HttpResponse(StatusCodes.BadRequest, entity = "Product was not added."))
        }
      } catch {
        case dx: DeserializationException =>
          log.error(dx, "Deserialization exception")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter user with message ${dx.getMessage}."))
        case px: ParsingException =>
          log.error(px, "Failed to parse JSON while registering")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Failed to parse JSON entity with message ${px.getMessage}"))
        case x: Exception =>
          log.error(x, "Uncaught exception while deserializing.")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "An internal server error occurred."))
      }
    }

  }

  /**Adds products associated with a particular user to Card
    *
    * @param ItemString
    * @return
    */
  def AddCardItems(ItemString: String): server.Route = Route.seal {

    post {
      log.debug(s"POST shop/products/carditems has been called, parameter is: $ItemString")
      try {
        val paramInstance: CardItems = ItemString.parseJson.convertTo[CardItems](CardItemsJsonSupportFormat)
        handler.handleAddCardItems(paramInstance) match {
          case Success(productId) =>
            complete {
              productId.toString
            }
          case Failure(ex) =>
            log.error(ex, "Failed to add items to card.")
            complete(HttpResponse(StatusCodes.BadRequest, entity = "Items not added to card."))
        }
      } catch {
        case dx: DeserializationException =>
          log.error(dx, "Deserialization exception")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter user with message ${dx.getMessage}."))
        case px: ParsingException =>
          log.error(px, "Failed to parse JSON while registering")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Failed to parse JSON entity with message ${px.getMessage}"))
        case x: Exception =>
          log.error(x, "Uncaught exception while deserializing.")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "An internal server error occurred."))
      }
    }

  }

  /**Returns the checkout item list
    *
    * @return
    */
  def checkout(): server.Route = parameters('UserName.as[String].?) { userNameString =>
    get {
      log.debug(s"GET shop/products/checkout?UserName=$userNameString has been called")

      val emptyListMsg = s"The Card is empty for $userNameString. Please add items"

      if (userNameString != null) {

        val itemList = handler.getItemList(userNameString.get)

        if (itemList.isEmpty) {

          complete {
            emptyListMsg
          }

        }
        else {
          complete {
            handler.getItemList(userNameString.get)
          }
        }
      }
      else {
        log.warning(s"Failed to deserialize parameter string $userNameString ")
        complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter string $userNameString to ComponentType"))
      }
    }
  }


}

