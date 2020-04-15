package Streaming
import Streaming.Logging.setupLogging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.functions.{window, col}

object StructuredStreaming {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("StructuredStreamingJSON").master("spark://max:7077").getOrCreate()
    setupLogging()

    // Structured Streaming does not let you perform schema inference without explicitly enabling it. setting schema inference.
    spark.conf.set("spark.sql.streaming.schemaInference", "true")

    // reading the json directory and get schema details automatically
    val static = spark.readStream.json("/home/max/Spark-The-Definitive-Guide-master/data/activity-data")
    val dataSchema = static.schema


    // inferring schema manually
    val myManualSchema = StructType(Array(StructField("id", StringType, true), StructField("name", StringType, true), StructField("device", StringType, true)))

    /* create a streaming version of the same Dataset, which will read each input file in the
    dataset one by one as if it was a stream. */
    print(dataSchema)
    val streaming = spark
      .readStream
      .schema(dataSchema)
      .option("maxFilesPerTrigger","1")
      .json("/home/max/Spark-The-Definitive-Guide-master/data/activity-data")


    val withEventTime = streaming.selectExpr("*","cast(cast(Creation_Time as double)/1000000000 as timestamp) as event_time")

    val query = withEventTime.groupBy(window(col("event_time"), "10 minutes")).count()
      .writeStream
      .queryName("events_per_window")
      .format("console")
      .outputMode("complete")
      .start()

   // performing some transformation
    val activityCounts = streaming.groupBy("Device").count()

    // setting the shuffle to 5 since its local machine.
    spark.conf.set("spark.sql.shuffle.partitions","5")

    /*// writing the stream to a console
    val activityQuery = activityCounts
      .writeStream
      .queryName("activity_counts")
      .format("console")
      .outputMode("complete")
      .start()

    spark.sql("SELECT * FROM events_per_window").show()
    activityQuery.awaitTermination() */
     query.awaitTermination()
  }

}
