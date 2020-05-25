package shoppingapp.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait ShoppingProductJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ShoppingProductFormat: JsonFormat[ShoppingProduct] = jsonFormat6(ShoppingProduct)
}

final case class ShoppingProduct(productId: Option[Long], productName: String, price: Option[Long], currency: Option[String], productDesc: Option[String], itemCount: Option[Long])