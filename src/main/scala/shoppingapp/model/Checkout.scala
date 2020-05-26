package shoppingapp.model


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait CheckoutJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val CheckoutJsonSupportFormat: JsonFormat[Checkout] = jsonFormat5(Checkout)
}

final case class Checkout(orderId: Option[Long], userName: String,
                          price : Long, quantity: Long, deliveryAddress: String)
