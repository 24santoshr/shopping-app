package shoppingapp.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val authShoppingUserFormat: JsonFormat[ShoppingUser] = jsonFormat4(ShoppingUser)
}

final case class ShoppingUser(userId: Option[Long], userName: String, emailId: String, bankAcctNo: String)

