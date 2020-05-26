package shoppingapp.connection

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import shoppingapp.daos.{InMemoryDAO, ShopDAO}
import shoppingapp.model._
import shoppingapp.{Configuration, RequestHandler}
import spray.json._


class ServerTest
  extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with CardItemsJsonSupport
    with UserJsonSupport
    with ShoppingProductJsonSupport
    with CheckoutJsonSupport {

  private val configuration: Configuration = new Configuration()
  private val shopDAO: ShopDAO = new InMemoryDAO(configuration)
  private val requestHandler: RequestHandler = new RequestHandler(configuration, shopDAO)
  private val server: Server = new Server(requestHandler)

  //JSON CONSTANTS

  private val validJsonShoppingiUser = ShoppingUser(userId = None,
    userName = "validUser", emailId = "validUser@email.de",
    bankAcctNo = "1234567").toJson(authShoppingUserFormat
  ).toString

  private val sameUsernameJson = validJsonShoppingiUser.replace(""""userName":"validUser",""", """"userName":"admin",""")
  private val noEmailId = validJsonShoppingiUser.replace(""""emailId":"validUser@email.de",""", "")
  private val sameEmailId = validJsonShoppingiUser.replace(""""emailId":"validUser@email.de",""", """"emailId":"validUser@email.de",""")

  private val validJsonShoppingProduct = ShoppingProduct(productId = None,
    productName = "SampleProduct", price = 25, currency = None,
    productDesc = None, itemCount = 10).toJson(ShoppingProductFormat
  ).toString

  private val noProductName = validJsonShoppingProduct.replace(""""productName":"SampleProduct",""", "")
  private val sameProductName = validJsonShoppingProduct.replace(""""productName":"SampleProduct",""", """"productName":"SampleProduct",""")


  private val validJsonCardItems = CardItems(userId = None,
    userName = "validUser", productId = None,
    productName = "SampleProduct", prodDesc = None,
    quantity = 5, price = 125, currency = None).toJson(CardItemsJsonSupportFormat).toString()
  private val noCardUser = validJsonCardItems.replace(""""userName":"validUser",""", "")
  private val noCardProduct = validJsonCardItems.replace(""""productName":"SampleProduct",""", "")

  private val validJsonCheckout = Checkout(orderId = None, userName = "validUser",
    price = 125, quantity = 1, deliveryAddress = "DummyAddress").toJson(CheckoutJsonSupportFormat
  ).toString


  private val noUserName = validJsonCheckout.replace(""""userName":"validUser",""", "")


  override def beforeAll(): Unit = {
    requestHandler.initialize()
    shopDAO.initialize()
    shopDAO.addUser(ShoppingUser(None, "admin", "admin@email.de", "0123456"))
  }

  override def afterAll(): Unit = {
    requestHandler.shutdown()
  }

  "The Server" should {

    "successfully create user when everything is valid" in {
      Post("/shop/users/add", HttpEntity(ContentTypes.`application/json`, validJsonShoppingiUser.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }
    }
    "not create user if request is invalid" in {

      //should use valid request method
      Get("/shop/users/add") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }

      //not all required parameter given
      Post("/shop/users/add", HttpEntity(ContentTypes.`application/json`, noEmailId.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }

      //trying to insert same username
      Post("/shop/users/add", HttpEntity(ContentTypes.`application/json`, sameUsernameJson.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }

      //trying to insert same emailid
      Post("/shop/users/add", HttpEntity(ContentTypes.`application/json`, sameEmailId.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }

    }

    "successfully add product when everything is valid" in {
      Post("/shop/products/add", HttpEntity(ContentTypes.`application/json`, validJsonShoppingProduct.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }

    }
    "not add product if request is invalid" in {

      //should use valid request method
      Get("/shop/products/add") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
      //not all required parameter given
      Post("/shop/products/add", HttpEntity(ContentTypes.`application/json`, noProductName.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }

      //try to insert same product
      Post("/shop/products/add", HttpEntity(ContentTypes.`application/json`, sameProductName.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }
    }

    "successfully add products to card when everything is valid" in {
      Post("/shop/products/carditems", HttpEntity(ContentTypes.`application/json`, validJsonCardItems.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }

    }

    "not add item to card if request is invalid" in {

      //should use valid request method
      Get("/shop/products/carditems") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }

      //no user
      Post("/shop/products/carditems", HttpEntity(ContentTypes.`application/json`, noCardUser.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }

      //no product given
      Post("/shop/products/carditems", HttpEntity(ContentTypes.`application/json`, noCardProduct.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }

    }
    //retrieve checkout list
    "successfully retrieve the checkout list" in {

      Get("/shop/products/viewcard?UserName=validUser") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
        responseAs[String].parseJson.convertTo[List[CardItems]](listFormat(CardItemsJsonSupportFormat))
      }

      //should retrieve a empty list with message
      Get("/shop/products/viewcard?UserName=dummy") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
        // responseAs[String].parseJson.convertTo[List[CardItems]](listFormat(CardItemsJsonSupportFormat))
      }
    }
    "not retrieve the checkout list" in {

      //no parameter
      Get("/shop/products/viewcard?UserName") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
        // responseAs[String].parseJson.convertTo[List[CardItems]](listFormat(CardItemsJsonSupportFormat))
      }
    }

    "successfully checkout and generate order id" in {

      Post("/shop/products/checkout", HttpEntity(ContentTypes.`application/json`, validJsonCheckout.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }

    }

    "not generate order id" in {

      //no parameter
      Post("/shop/products/checkout", HttpEntity(ContentTypes.`application/json`, noUserName.stripMargin)) ~>
        Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.BAD_REQUEST)
      }
    }
  }
}
