package shoppingapp.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait CardItemsJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val CardItemsJsonSupportFormat: JsonFormat[CardItems] = jsonFormat8(CardItems)
}

final case class CardItems(userId: Option[Long], userName: String, productId: Option[Long],
                           productName: String, prodDesc: Option[String],
                           quantity: Long, price: Long,
                           currency: Option[String])



