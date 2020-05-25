package shoppingapp

class Configuration() {
  //Where to host the http server
  val bindHost: String = "0.0.0.0"
  val bindPort: Int = 9000


  val recoveryFileName: String = "dump.temp"

  //shoppingapp.daos.AuthDAO database configuration
  val useInMemoryAuthDB = true
  val authDatabaseHost = "jdbc:mysql://localhost/"
  val authDatabaseName = ""
  val authDatabaseDriver = "com.mysql.jdbc.Driver"
  val authDatabaseUsername = ""
  val authDatabasePassword = ""

}


