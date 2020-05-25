package shoppingapp.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import shoppingapp.model.ShoppingUserEnums.ShoppingUserType
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val userTypeFormat: JsonFormat[ShoppingUserType] = new JsonFormat[ShoppingUserType] {

    /**
      * Custom write method for serializing an UserType
      *
      * @param userType
      * @return
      */
    def write(userType: ShoppingUserType) = JsString(userType.toString)

    /**
      * Custom read method for deserialization of an UserType
      *
      * @param value
      * @return
      */
    def read(value: JsValue): ShoppingUserType = value match {
      case JsString(s) => s match {
        case "User" => ShoppingUserType.User
        case "Admin" => ShoppingUserType.Admin
        case x => throw DeserializationException(s"Unexpected string value $x for shopping user type.")
      }
      case y => throw DeserializationException(s"Unexpected type $y during deserialization shopping user type.")
    }
  }

  implicit val authShoppingUserFormat: JsonFormat[ShoppingUser] = jsonFormat4(ShoppingUser)
}


final case class ShoppingUser(userId: Option[Long], userName: String, emailId: String, bankAcctNo: String)

object ShoppingUserEnums {

  //Type to use when working with component types
  type ShoppingUserType = ShoppingUserType.Value

  /**
    * userType enumeration defining the valid types of delphi users
    */
  object ShoppingUserType extends Enumeration {
    val User: Value = Value("User")
    val Admin: Value = Value("Admin")
  }

}