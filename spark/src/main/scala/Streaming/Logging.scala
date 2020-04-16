package Streaming

import org.apache.log4j.{Level, Logger}


object Logging {

  def setupLogging(): Unit = {

    val rootLogger = Logger.getRootLogger
    rootLogger.setLevel(Level.ERROR)

  }

}
