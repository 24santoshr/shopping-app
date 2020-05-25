package shoppingapp.daos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import shoppingapp.model.{CardItems, ShoppingProduct, ShoppingUser}
import shoppingapp.{AppLogging, Registry}
import shoppingapp.Configuration

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}


class InMemoryDAO(configuration: Configuration) extends AppLogging with AuthDAO {
  implicit val system: ActorSystem = Registry.system
  implicit val materializer: ActorMaterializer = Registry.materializer
  implicit val ec: ExecutionContext = system.dispatcher

  private val users: mutable.Set[ShoppingUser] = new mutable.HashSet[ShoppingUser]()
  private val products: mutable.Set[ShoppingProduct] = new mutable.HashSet[ShoppingProduct]()
  private val carditems: mutable.Set[CardItems] = new mutable.HashSet[CardItems]()

  override def initialize(): Unit = {
    log.info("Initializing dynamic shoppingapp.daos.AuthDAO DAO...")
    clearData()
    log.info("Successfully initialized shoppingapp.daos.AuthDAO DAO.")

  }

  override def shutdown(): Unit = {
    log.info("Shutting down dynamic shoppingapp.daos.AuthDAO DAO...")
    clearData()
    log.info("Shutdown complete dynamic shoppingapp.daos.AuthDAO DAO.")
  }

  def clearData(): Unit = {
    users.clear()
  }

  override def addUser(shoppingUser: ShoppingUser): Try[Long] = {
    if (hasUserWithEmail(shoppingUser.emailId)) {
      Failure(new RuntimeException(s"EmailId ${shoppingUser.userName} already exists."))
    } else {
      val id = nextId()
      val newUser = ShoppingUser(Some(id), shoppingUser.userName, shoppingUser.emailId.toString, shoppingUser.bankAcctNo)
      users.add(newUser)

      log.info(s"Added user ${newUser.userName} with id ${newUser.userId.get} to database.")
      Success(id)
    }

  }

  def hasUserWithEmail(emailId: String): Boolean = {
    val query = users filter { i => i.emailId == emailId }
    query.nonEmpty
  }

  private def nextId(): Long = {
    if (users.isEmpty) {
      0L
    } else {
      (users.map(i => i.userId.getOrElse(0L)) max) + 1L
    }
  }

  override def addProduct(shoppingProduct: ShoppingProduct): Try[Long] = {

    {
      val id = productId()
      val newProduct = ShoppingProduct(Some(id), shoppingProduct.productName, shoppingProduct.price, shoppingProduct.currency, shoppingProduct.productDesc, shoppingProduct.itemCount)
      products.add(newProduct)
      log.info(s"Added product ${newProduct.productName} with id ${newProduct.productId.get} to database.")
      Success(id)
    }

  }


  private def productId(): Long = {
    if (products.isEmpty) {
      0L
    } else {
      (products.map(i => i.productId.getOrElse(0L)) max) + 1L
    }
  }

  override def addCardItems(cardItems: CardItems): Try[Long] = {

    if (hasUserWithUsername(cardItems.userName)) {

      val userDetails = getUserWithName(cardItems.userName)
      val productDetails = getProductWithName(cardItems.productName)

      if (hasProductWithName(cardItems.productName)) {

        val productId = productDetails.get.productId

        val productQuantity = productDetails.get.itemCount.get

        val productPrice = productDetails.get.price.get


        val quantity = getQuantity(productId.get, productQuantity)

        val total = getTotalPrice(productId.get, productPrice, productQuantity)

        val newItem = CardItems(userDetails.get.userId, userDetails.get.userName, productId, productDetails.get.productName, productDetails.get.productDesc, Some(quantity), Some(total), productDetails.get.currency)

        if (hasItemWithId(productId.get)) {
          carditems map { i => i.quantity.get == productId.get }


        }
        else {
          carditems.add(newItem)
        }
        log.info(s"Added product ${newItem.productName} with id ${newItem.productId} and ${quantity} and $total to shopping card.")
        Success(productId.get)
      }
      else {
        Failure(new RuntimeException(s"Product ${cardItems.productName} does not exist."))
      }
    } else {
      Failure(new RuntimeException(s"User ${cardItems.userName} does not exist. Please register with valid email id."))
    }
  }


  def getQuantity(id: Long, count: Long): Long = {

    val quantity = carditems filter { i => i.productId.get == id }
    if (quantity.isEmpty) {
      count
    }
    else {
      (quantity.map(i => i.quantity.getOrElse(0L)) max) + count
    }

  }

  override def getProductWithName(name: String): Option[ShoppingProduct] = {
    if (hasProductWithName(name)) {
      val query = products filter { i => i.productName == name }
      val result = query.iterator.next()
      Some(dataToObjectProduct(result))
    } else {
      None
    }
  }

  def hasProductWithName(name: String): Boolean = {
    val query = products filter { i => i.productName.toLowerCase == name.toLowerCase }
    query.nonEmpty
  }

  def getTotalPrice(id: Long, price: Long, quantity: Long): Long = {

    val totalPrice = carditems filter { i => i.productId.get == id }
    if (totalPrice.isEmpty) {
      quantity * price

    }
    else {
      (totalPrice.map(i => i.price.getOrElse(0L)) max) + quantity * price
    }

  }

  override def getUserWithName(userName: String): Option[ShoppingUser] = {
    if (hasUserWithUsername(userName)) {
      val query = users filter { i => i.userName.toLowerCase == userName.toLowerCase }
      val result = query.iterator.next()
      Some(dataToObjectUser(result))
    } else {
      None
    }
  }

  private def dataToObjectUser(shoppingUser: ShoppingUser): ShoppingUser = {

    ShoppingUser(shoppingUser.userId, shoppingUser.userName, shoppingUser.emailId, shoppingUser.bankAcctNo)

  }

  def hasUserWithUsername(username: String): Boolean = {
    val query = users filter { i => i.userName.toLowerCase == username.toLowerCase }
    query.nonEmpty
  }

  def hasItemWithId(id: Long): Boolean = {
    val query = carditems filter { i => i.productId.get == id }
    query.nonEmpty
  }

  override def getProductWithId(id: Long): Option[ShoppingProduct] = {
    if (hasProductWithId(id)) {
      val query = products filter { i => i.productId.get == id }
      val result = query.iterator.next()
      Some(dataToObjectProduct(result))
    } else {
      None
    }
  }

  private def dataToObjectProduct(shoppingProduct: ShoppingProduct): ShoppingProduct = {

    ShoppingProduct(shoppingProduct.productId, shoppingProduct.productName, shoppingProduct.price, shoppingProduct.currency, shoppingProduct.productDesc, shoppingProduct.itemCount)

  }

  def hasProductWithId(id: Long): Boolean = {
    val query = products filter { i => i.productId.get == id }
    query.nonEmpty
  }

  override def getCardDetails(userName: String): List[CardItems] = {
    val strList = List.empty[CardItems]

    if (isCardEmpty(userName)) {
      List() ++ carditems filter { i => i.userName.toLowerCase == userName.toLowerCase }
    }
    else {
      strList
    }

  }

  def isCardEmpty(userName: String): Boolean = {
    val query = carditems filter { i => i.userName.toLowerCase == userName.toLowerCase }
    query.nonEmpty
  }


}

