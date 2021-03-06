package shoppingapp

import akka.actor.ActorSystem
import akka.stream._
import shoppingapp.daos.ShopDAO
import shoppingapp.model.{CardItems, Checkout, ShoppingProduct, ShoppingUser}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


class RequestHandler(configuration: Configuration, shopDAO: ShopDAO) extends AppLogging {


  implicit val system: ActorSystem = Registry.system
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  def initialize(): Unit = {
    shopDAO.initialize()
    log.info("request Handler initialize")
  }

  def shutdown(): Unit = {
    shopDAO.shutdown()
    log.info("shutdown")
  }

  /**Add user to user database
    *
    * @param user The user to add
    * @return Id assigned to that user
    */
  def handleAddUser(user: ShoppingUser): Try[Long] = {

    val noIdUser = ShoppingUser(userId = user.userId, userName = user.userName, emailId = user.emailId, bankAcctNo = user.bankAcctNo)

    shopDAO.addUser(noIdUser) match {
      case Success(userId) =>
        log.info(s"Successfully added user")
        Success(userId)
      case Failure(x) => Failure(x)
    }
  }

  /**Add product to product database
    *
    * @param product The product to add
    * @return Id assigned to that product
    */
  def handleAddProduct(product: ShoppingProduct): Try[Long] = {

    val noIdProduct = ShoppingProduct(productId = product.productId, productName = product.productName, price = product.price, currency = product.currency, productDesc = product.productDesc, itemCount = product.itemCount)

    shopDAO.addProduct(noIdProduct) match {
      case Success(productId) =>
        log.info(s"Successfully added product")
        Success(productId)
      case Failure(x) => Failure(x)
    }
  }

  /**Add card items to card database
    *
    * @param cardItems card items to add
    * @return Id assigned to that card
    */
  def handleAddCardItems(cardItems: CardItems): Try[Long] = {

    val noIdCardItems = CardItems(userId = cardItems.userId, userName = cardItems.userName, productId = cardItems.productId, productName = cardItems.productName, prodDesc = cardItems.prodDesc, quantity = cardItems.quantity, price = cardItems.price, currency = cardItems.currency)

    shopDAO.addCardItems(noIdCardItems) match {
      case Success(id) =>
        log.info(s"Successfully added item to card")
        Success(id)
      case Failure(x) => Failure(x)
    }
  }

  /**Gets checkout details
    *
    * @param userName User associated with card
    * @return Checkout list
    */
  def getItemList(userName: String): List[CardItems] = {
    shopDAO.getCardDetails(userName)
  }


  /**checkout
    *
    * @param cardItems card items to add
    * @return Id assigned to that card
    */
  def handleCheckout(checkout: Checkout): Try[Long] = {

    val noIdCheckout = Checkout(orderId = checkout.orderId, userName = checkout.userName, price = checkout.price, quantity = checkout.quantity, deliveryAddress = checkout.deliveryAddress)

    shopDAO.addCheckout(noIdCheckout) match {
      case Success(id) =>
        log.info(s"Successfully added item to card")
        Success(id)
      case Failure(x) => Failure(x)
    }
  }
}

