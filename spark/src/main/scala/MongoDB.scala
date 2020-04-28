import org.apache.spark.sql.SparkSession

object MongoDB {

  def main(args: Array[String]): Unit = {
    val my_spark = SparkSession.builder.master("local[*]").appName("myApp").config("spark.mongodb.input.uri", "mongodb://127.0.0.1/hadoop.coll").config("spark.mongodb.output.uri", "mongodb://127.0.0.1/test.coll").getOrCreate()
    val df = my_spark.read.format("mongo").option("uri","mongodb://127.0.0.1/mycollection").load()
    df.show()


  }


}
