package shoppingapp

import akka.actor.ActorSystem
import akka.stream._
import shoppingapp.daos.AuthDAO
import shoppingapp.model.{CardItems, ShoppingProduct, ShoppingUser}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


class RequestHandler(configuration: Configuration, authDao: AuthDAO) extends AppLogging {


  implicit val system: ActorSystem = Registry.system
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  def initialize(): Unit = {
    authDao.initialize()
    log.info("request Handler initialize")
  }

  def shutdown(): Unit = {
    authDao.shutdown()
    log.info("shutdown")
  }

  def handleAddUser(user: ShoppingUser): Try[Long] = {

    val noIdUser = ShoppingUser(userId = user.userId, userName = user.userName, emailId = user.emailId, bankAcctNo = user.bankAcctNo)

    authDao.addUser(noIdUser) match {
      case Success(userId) =>
        log.info(s"Successfully added user")
        Success(userId)
      case Failure(x) => Failure(x)
    }
  }

  def handleAddProduct(product: ShoppingProduct): Try[Long] = {

    val noIdProduct = ShoppingProduct(productId = product.productId, productName = product.productName, price = product.price, currency = product.currency, productDesc = product.productDesc, itemCount = product.itemCount)

    authDao.addProduct(noIdProduct) match {
      case Success(productId) =>
        log.info(s"Successfully added product")
        Success(productId)
      case Failure(x) => Failure(x)
    }
  }


  def handleAddCardItems(cardItems: CardItems): Try[Long] = {

    val noIdCardItems = CardItems(userId = cardItems.userId, userName = cardItems.userName, productId = cardItems.productId, productName = cardItems.productName, prodDesc = cardItems.prodDesc, quantity = cardItems.quantity, price = cardItems.price, currency = cardItems.currency)

    authDao.addCardItems(noIdCardItems) match {
      case Success(id) =>
        log.info(s"Successfully added item to cart")
        Success(id)
      case Failure(x) => Failure(x)
    }
  }

  def getItemList(userName: String): List[CardItems] = {
    authDao.getCardDetails(userName)
  }


}

