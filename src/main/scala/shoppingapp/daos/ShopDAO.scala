package shoppingapp.daos

import shoppingapp.model.{CardItems, Checkout, ShoppingProduct, ShoppingUser}

import scala.util.Try

trait ShopDAO {

  /**Adds user
    *
    * @param shoppingUser
    * @return
    */
  def addUser(shoppingUser: ShoppingUser): Try[Long]

  /**Initializes ShopDAO
    *
    */
  def initialize(): Unit

  /** Shuts down ShopDAO
    *
    */
  def shutdown(): Unit

  /**Adds product
    *
    * @param shoppingProduct
    * @return
    */
  def addProduct(shoppingProduct: ShoppingProduct): Try[Long]

  /**Adds items to Card
    *
    * @param cardItems
    * @return
    */
  def addCardItems(cardItems: CardItems): Try[Long]

  /**Checkout function
    *
    * @param checkout
    * @return
    */
  def addCheckout(checkout: Checkout): Try[Long]

  /**Returns Product by taking Product Id
    *
    * @param id
    * @return
    */

  def getProductWithId(id: Long): Option[ShoppingProduct]


    /**Returns Product by taking Product Name
    *
    * @param productName
    * @return
    */
  def getProductWithName(productName: String): Option[ShoppingProduct]

  /**Returns User details by taking User Name
    *
    * @param userName
    * @return
    */
  def getUserWithName(userName: String): Option[ShoppingUser]

  /**Returns item list for specified user
    *
    * @param userName
    * @return
    */
  def getCardDetails(userName: String): List[CardItems]


}
