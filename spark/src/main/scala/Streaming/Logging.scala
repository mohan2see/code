package Streaming

object Logging {

  def setupLogging(): Unit = {

    import org.apache.log4j.{Level, Logger}

    val rootLogger = Logger.getRootLogger

    rootLogger.setLevel(Level.ERROR)

  }

}
