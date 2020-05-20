package Streaming
import Streaming.Logging.setupLogging
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WordCount {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Hello").master("local[*]").getOrCreate()
    setupLogging()
    spark.range(1000).toDF().show()
    val ssc = new StreamingContext(spark.sparkContext, Seconds(10))
    val lines = ssc.socketTextStream("localhost", 8888)
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map( x => (x,1)).reduceByKey(_ + _)
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()


  }
}
