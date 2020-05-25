package shoppingapp.daos

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull


object StringLengthDefinitions {
  val EnumStringLength: Int = 50
  val HashStringLength: Int = 65
  val NameStringLength: Int = 100
  val UriStringLength: Int = 255
  val LongCompositeStringLenght: Int = 1000
}

class Users(tag: Tag) extends Table[(Long, String, String, String)](tag, "users") {


  def * : ProvenShape[(Long, String, String, String)] = (id, userName, emailId, bankAcctNo)

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def userName: Rep[String] = column[String]("userName", O.Length(StringLengthDefinitions.NameStringLength), NotNull)

  def emailId: Rep[String] = column[String]("emailId", O.Unique, O.Length(StringLengthDefinitions.HashStringLength), NotNull)

  def bankAcctNo: Rep[String] = column[String]("bankAcctNo", O.Length(StringLengthDefinitions.EnumStringLength), NotNull)

}

class Items(tag: Tag) extends Table[(Long, String, Long, String, String, Long, Long, String)](tag, "carditems") {


  def * : ProvenShape[(Long, String, Long, String, String, Long, Long, String)] = (userId, userName, productId, productName, prodDesc, quantity, price, currency)

  def userId: Rep[Long] = column[Long]("userId", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def userName: Rep[String] = column[String]("userName", O.Length(StringLengthDefinitions.NameStringLength), NotNull)

  def productId: Rep[Long] = column[Long]("productId", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def productName: Rep[String] = column[String]("productName", O.Length(StringLengthDefinitions.NameStringLength), NotNull)

  def prodDesc: Rep[String] = column[String]("prodDesc", O.Length(StringLengthDefinitions.HashStringLength))

  def quantity: Rep[Long] = column[Long]("quantity")

  def price: Rep[Long] = column[Long]("price")

  def currency: Rep[String] = column[String]("currency", O.Length(StringLengthDefinitions.HashStringLength), NotNull)


}

class Products(tag: Tag) extends Table[(Long, String, Long, String, String, Long)](tag, "products") {
  // scalastyle:off method.name

  def * : ProvenShape[(Long, String, Long, String, String, Long)] = (id, productName, price, currency, desc, itemCount)

  def id: Rep[Long] = column[Long]("ProductId", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def productName: Rep[String] = column[String]("productName", O.Length(StringLengthDefinitions.NameStringLength), NotNull)

  def price: Rep[Long] = column[Long]("price", NotNull)

  def currency: Rep[String] = column[String]("currency", O.Length(StringLengthDefinitions.HashStringLength), NotNull)

  def desc: Rep[String] = column[String]("desc", O.Length(StringLengthDefinitions.HashStringLength), NotNull)

  def itemCount: Rep[Long] = column[Long]("itemCount")


}