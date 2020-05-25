package shoppingapp.daos

import shoppingapp.model.{CardItems, ShoppingProduct, ShoppingUser}

import scala.util.Try

trait AuthDAO {
  def addUser(shoppingUser: ShoppingUser): Try[Long]

  /**
    *
    */
  def initialize(): Unit

  def shutdown(): Unit

  def addProduct(shoppingProduct: ShoppingProduct): Try[Long]

  def addCardItems(cardItems: CardItems): Try[Long]

  def getProductWithId(id: Long): Option[ShoppingProduct]

  def getProductWithName(productName: String): Option[ShoppingProduct]

  def getUserWithName(userName: String): Option[ShoppingUser]

  def getCardDetails(userName: String): List[CardItems]


}
