package Streaming
import Streaming.Logging.setupLogging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.functions.{window, col}

object StructuredStreaming {

  def main(args: Array[String]): Unit = {

    /* Structured streaming is an API that works with existing spark High level Structured APIs in spark (DF, Datasets & SQL)
    All transformations that we use  in structured API can be used in Structured Streaming API as well with some restriction.
    only action is available in streaming API, i,e., start() to start the streaming and output the result.
    */


    val spark = SparkSession.builder().appName("StructuredStreamingJSON").master("spark://max:7077").getOrCreate()
    //calling the logging object.
    setupLogging()

    // Input streaming sources can be kafka 0.10, s3 or hdfs and socket source for testing (refer `wordcount.scala` for socket source testing)
    // output sinks can be apache kafka 0.10, most file formats, console sink, memory sink, A foreach sink for running arbitary computation on the output records
    // output modes tells how spark should write the data.
    //             1) append - appends only new information to the result
    //             2) update - update rows over time (ex, update click counts of a website)
    //             3) complete - completely overwrite the result each time.
    // Note : Important detail is that certain queries, and certain sinks, only support certain output modes. for ex, map operation wont work with complete mode


    // Triggers - tells spark when to check for input data, by default, spark looks for input data as soon as it processed the last group of input data...
    // ...To avoid spark writing many small output files when the sink is a set of files. Triggers tells spark to look for input files at fixed intervals.

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
      .option("maxFilesPerTrigger","1") //specifies to stream one file at a time for demo. not to be used in production scenario.
      .json("/home/max/Spark-The-Definitive-Guide-master/data/activity-data")


    // performing some transformation. The execution is lazy as in structured API.
    val activityCounts = streaming.groupBy("Device").count()

    val withEventTime = streaming.selectExpr("*","cast(cast(Creation_Time as double)/1000000000 as timestamp) as event_time")

    // writing to a console.
    /* val query = withEventTime.groupBy(window(col("event_time"), "10 minutes")).count()
      .writeStream
      .queryName("events_per_window")
      .format("console")
      .outputMode("complete")
      .start()
    */

    // writing to a file sink
    val query = withEventTime.withWatermark("event_time", "5 minutes").groupBy(window(col("event_time"), "10 minutes")).count()
      .writeStream
      .queryName("events_per_window")
      .format("json")
      .option("path", "/home/max/spark/testing/")
      .option("checkpointLocation", "hdfs://localhost:9000/user/checkpoint/")
      .outputMode("append")
      .start()


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
