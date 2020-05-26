package shoppingapp


import akka.actor._
import akka.stream.ActorMaterializer
import shoppingapp.connection.Server
import shoppingapp.daos.{ShopDAO, InMemoryDAO}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object Registry extends AppLogging {
  implicit val system: ActorSystem = ActorSystem("shopping-app")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  val configuration = new Configuration()

  private val shopDAO: ShopDAO = new InMemoryDAO(configuration)

  private val requestHandler = new RequestHandler(configuration, shopDAO)

  private val server: Server = new Server(requestHandler)

  def main(args: Array[String]): Unit = {

    requestHandler.initialize()
    server.startServer(configuration.bindHost, configuration.bindPort)
    requestHandler.shutdown()
    system.terminate()
  }
}