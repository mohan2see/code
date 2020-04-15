import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/* Spark is a framework for Managing & coordinating the execution of tasks on clusters of computers. These clusters of
machines are managed by cluster manager, i.e., spark standalone cluster manager, mesos, YARN. User submit the spark
Application to cluster Manager, which will grant resources to complete the task.
*/

/* SPARK Applications : Spark application has driver process and executor process. Drive process runs the main() function
and sits on a node in the cluster. It's responsible for 3 things, 1) maintaining info about spark application
2) responding to user inputs 3)analyze,distribute,schedule the tasks on executor nodes. Executors are responsible for
executing the code assigned to it by driver process, and reporting the state of computation on that executor back to
driver process.
 */

/* SparkSession - SparkSession object is the entry point of Spark code. SparkSession is the driver process.The
SparkSession instance is the way Spark executes user-defined manipulations across the cluster.
There is a one-to-one correspondence between a SparkSession and a Spark Application
*/

object SparkBasics {

  def main(args: Array[String]): Unit = {
    //val conf = new SparkConf().setAppName("test").setMaster("local[*]")
    //val sc = new SparkContext(conf)
    val spark = SparkSession
      .builder()
      .appName("WordCount")
      .master("local[*]")
      .getOrCreate()
    /* Creating a simple dataframe. DataFrame is a structured API in spark. The range of numbers is distributed
      across clusters of machines. Because either too big to fit in one machine / too slow to do computation in single
      machine. DataFrame is simply a spreadsheet with names columns and rows that spread across machines. Python
      dataframe(pandas) are similar but difference is python DF stays in one machine. its easy to convert Pandas DF to
      Spark DF
      */
    val myRange = spark.range(1000).toDF("number")
    print(myRange.show())

    /* partitions - Spark breaks the data into chunks to allow parallelism. partitions is chunks of data. partition is
    a collection of row in one machine in a cluster. mostly, we should not specify the partition number, spark handles
    it according to the transformation performed on it.
     */

    /* Transformations - core data structures like DF are immutable. To modify, we need to instruct spark how to do.
    These instructions are called Transformations. 1) Narrow Transformation - usually one input partition to one output
    partition. these Transformations runs on memory 2) Wide Transformation - input partitions contribute to multiple
    output paritions, usually referred as 'Shuffle' - where spark will exchange partitions across the cluster. These
    Transformations are NOT in-memory, spark writes the results to disk.
     */
    //narrow transformation. This wont generate any output. --lazy evaluation.
    val divisBy2 = myRange.where("number % 2 = 0")

    /* lazy evaluation - Transformations are simply the plans to instruct spark what to do on data. An action must be
     performed for spark to run these instructions. Spark will optimize the transformation by creating efficient
     physical plans to run across cluster. For example, specifying a filter in the end of transformation, spark will
     create the plan to filter the data first, so it will be easy to run transformation on the filtered data rather than
     complete data set.*/

    // sample actions for spark to trigger the computation. 3 types of actions 1) actions to show output 2) actions to
    // collect data to native objects in respective languages 3) actions to write to output data sources
    divisBy2.show()
    print(divisBy2.count()) /* spark job filters the range of numbers (narrow trans) then an aggregation (wide trans)
    that performs the count on per partition basis and collect the result to native object. Check in Spark UI. Keep the
    application running to check in Spark UI.*/

    """ A REAL EXAMPLE """
    val flightData2015 = spark.read
      .option("inferschema","true") // asking spark to take the best guess of schema based on spark reads few rows.
      .option("header", "true")
      .csv("/home/max/Spark-The-Definitive-Guide-master/data/flight-data/csv/2015-summary.csv")

    //show() displays output in console. take(n) displays n rows in array. collect() displays all rows in array.
    print(flightData2015.show(5))

    //sorting the data by count. By default, spark outputs 200 partitions for shuffle. setting to 5 to reduce partitions
    spark.conf.set("spark.sql.shuffle.partitions", "5")
    flightData2015.sort("count").show(5)
    // writing the data frame to a file with header
    flightData2015.sort("count").write.option("header", "true").csv("/home/max/Spark-The-Definitive-Guide-master/data/flight-data/a.csv")


    """ DataFrames and SQL"""

    /* Spark can run the same transformations, regardless of the language, in the exact same way. You can express
    your business logic in SQL or DataFrames (either in R, Python, Scala, or Java) and Spark will compile that logic
    down to an underlying plan (that you can see in the explain plan) before actually executing your code.
    With Spark SQL, you can register any DataFrame as a table or view (a temporary table) and query it using pure SQL.
    There is no performance difference between writing SQL queries or writing DataFrame code, they both “compile”
    to the same underlying plan that we specify in DataFrame code */

    // registering the DF as a temp table
    flightData2015.createOrReplaceTempView("flight_data_2015")

    //Querying the registered table. SQL WAY. spark.sql returns a DataSet. greaterThan5000 is dataset
    val greaterThan5000 = spark.sql("select * from flight_data_2015 where count > 5000")
    for (i <- greaterThan5000.collect()) {
      print(i)
    }

    greaterThan5000
      .write
      .option("header","true").csv("/home/max/Spark-The-Definitive-Guide-master/data/flight-data/b.csv")

    // DATAFRAME WAY. Returns a DataSet. greaterThan5000DF is dataset. Both Sql way and this below code generates
    // same logical plan under the hood.  .explain() will give same result for both SQL vs DF.
    val greaterThan5000DF = flightData2015.where("count > 5000")
    greaterThan5000DF.show()
    greaterThan5000DF
      .write
      .csv("/home/max/Spark-The-Definitive-Guide-master/data/flight-data/c.csv")

    //taking max count. SQL Way
    val maxCount = spark.sql("select max(count) from flight_data_2015")
    maxCount.show()

    //DF way
    flightData2015.select(max("count")).show()

    // Top 5 destination. SQL way
    val top5Destination = spark.sql("select DEST_COUNTRY_NAME, sum(count) from flight_data_2015 group by" +
      " DEST_COUNTRY_NAME order by sum(count) DESC Limit 5")
    top5Destination.show()

    // DF way. Aggregation(sum) must follow groupBy
    flightData2015
      .groupBy("DEST_COUNTRY_NAME")
      .sum("count")
      .withColumnRenamed("sum(count)","Destination_Total")
      .sort(desc("Destination_Total"))
      .limit(5)
      .show()

  }
}