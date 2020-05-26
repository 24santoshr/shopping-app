package shoppingapp.daos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import shoppingapp.model.{CardItems, ShoppingProduct, ShoppingUser}
import shoppingapp.{AppLogging, Configuration, Registry}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}


class InMemoryDAO(configuration: Configuration) extends AppLogging with ShopDAO {
  implicit val system: ActorSystem = Registry.system
  implicit val materializer: ActorMaterializer = Registry.materializer
  implicit val ec: ExecutionContext = system.dispatcher

  private val users: mutable.Set[ShoppingUser] = new mutable.HashSet[ShoppingUser]()
  private val products: mutable.Set[ShoppingProduct] = new mutable.HashSet[ShoppingProduct]()
  private val carditems: mutable.Set[CardItems] = new mutable.HashSet[CardItems]()

  override def initialize(): Unit = {
    log.info("Initializing ShopDAO...")
    clearData()
    log.info("Successfully initialized ShopDAO.")

  }

  override def shutdown(): Unit = {
    log.info("Shutting down ShopDAO...")
    clearData()
    log.info("Shutdown complete ShopDAO.")
  }

  def clearData(): Unit = {
    users.clear()
  }

  override def addUser(shoppingUser: ShoppingUser): Try[Long] = {

    if (hasUserWithName(shoppingUser.userName)) {
      Failure(new RuntimeException(s"Username: ${shoppingUser.userName} already exists."))
    } else {
      if (hasUserWithEmail(shoppingUser.emailId)) {
        Failure(new RuntimeException(s"Email id: ${shoppingUser.emailId} already exists."))
      } else {
        val id = nextId()
        val newUser = ShoppingUser(Some(id), shoppingUser.userName, shoppingUser.emailId.toString, shoppingUser.bankAcctNo)
        users.add(newUser)

        log.info(s"Added user ${newUser.userName} with id ${newUser.userId.get} to database.")
        Success(id)
      }
    }
  }

  def hasUserWithEmail(emailId: String): Boolean = {
    val query = users filter { i => i.emailId.toLowerCase == emailId.toLowerCase }
    query.nonEmpty
  }

  def hasUserWithName(userName: String): Boolean = {
    val query = users filter { i => i.userName.toLowerCase == userName.toLowerCase }
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
    if (hasProductWithName(shoppingProduct.productName)) {
      Failure(new RuntimeException(s"Product ${shoppingProduct.productName} already exists."))
    }
    else {
      {
        val id = productId()
        val newProduct = ShoppingProduct(Some(id), shoppingProduct.productName,
          shoppingProduct.price, shoppingProduct.currency,
          shoppingProduct.productDesc, shoppingProduct.itemCount)

        products.add(newProduct)
        log.info(s"Added product ${newProduct.productName} with id ${newProduct.productId.get} to database.")
        Success(id)
      }
    }
  }

  private def productId(): Long = {
    if (products.isEmpty) {
      0L
    } else {
      (products.map(i => i.productId.getOrElse(0L)) max) + 1L
    }
  }

  def hasProductWithName(name: String): Boolean = {
    val query = products filter { i => i.productName.toLowerCase == name.toLowerCase }
    query.nonEmpty
  }

  override def addCardItems(cardItems: CardItems): Try[Long] = {

    if (hasUserWithUsername(cardItems.userName)) {

      if (cardItems.quantity > 0) {

        val userDetails = getUserWithName(cardItems.userName)
        val productDetails = getProductWithName(cardItems.productName)

        if (hasProductWithName(cardItems.productName)) {

          val productId = productDetails.get.productId

          if (productDetails.get.itemCount > 0) {

            val productQuantity = cardItems.quantity
            val productPrice = cardItems.price * productQuantity
            val newItem = CardItems(userDetails.get.userId, userDetails.get.userName,
              productId, productDetails.get.productName,
              productDetails.get.productDesc, productQuantity, productPrice,
              productDetails.get.currency)

            if (hasItemWithId(productId.get)) {

              val updatedItem = getCardItems(productId.get)
              val itemsQuantity = updatedItem.get.quantity + productQuantity
              val itemsPrice = updatedItem.get.price + productPrice
              val newUpdatedItem = CardItems(updatedItem.get.userId,
                updatedItem.get.userName,
                updatedItem.get.productId,
                updatedItem.get.productName,
                updatedItem.get.prodDesc,
                itemsQuantity,
                itemsPrice,
                updatedItem.get.currency
              )
              carditems filter { i => i.productId.get == productId.get } map carditems.remove
              carditems.add(newUpdatedItem)
            }
            else {
              carditems.add(newItem)
            }

            val newItemCount = productDetails.get.itemCount - productQuantity
            val newProductDetails = ShoppingProduct(productDetails.get.productId, productDetails.get.productName,
              productDetails.get.price, productDetails.get.currency,
              productDetails.get.productDesc, newItemCount)

            products filter { i => i.productId.get == productId.get } map products.remove
            products.add(newProductDetails)

            log.info(s"Item ${cardItems.productName} added to shopping card")

            Success(productId.get)
          }
          else {
            Failure(new RuntimeException(s"No stocks available for Product ${cardItems.productName}."))
          }
        } else {
          Failure(new RuntimeException(s"Product ${cardItems.productName} does not exist."))
        }
      } else {
        Failure(new RuntimeException(s"Please select quantity and add to card."))
      }
    } else {
      Failure(new RuntimeException(s"User ${cardItems.userName} does not exist. Please register with valid email id."))
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

  def getCardItems(id: Long): Option[CardItems] = {
    if (hasItemWithId(id)) {
      val query = carditems filter { i => i.productId.get == id }
      val result = query.iterator.next()
      Some(dataToObjectCardItems(result))
    } else {
      None
    }
  }

  private def dataToObjectCardItems(cardItems: CardItems): CardItems = {
    CardItems(cardItems.userId, cardItems.userName, cardItems.productId, cardItems.productName, cardItems.prodDesc, cardItems.quantity, cardItems.price, cardItems.currency)
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

