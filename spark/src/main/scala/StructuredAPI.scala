import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.{col, column, expr, lit, when}
import org.apache.spark.sql.types.{IntegerType, LongType, Metadata, StringType, StructField, StructType}

object StructuredAPI {
  /* tool for manipulating unstructured data like logs, semi-structured data like csv, highly structured data like parquet
 These API refers to 3 core distributed collection API's. 1)Datasets 2)DataFrames 3)SQL tables and views

 DataFrames and DataSets are (distributed) table-like collections with well defined rows and columns. Each column should
 have same number of rows as other columns(can use `null` for absence of values).

 Schemas - Schemas are the column names and types of a DataFrame. Can define schemas manually or read from data source.
 Spark Types - Spark uses an engine called `catalyst` to store its own type information. Spark purely operates on data
 with its own spark types and NOT the programming specific(Scala,Python) data types.

 In Structured API, there are 2 API's. 1) Untyped DataFrames 2)Typed Datasets. To say that DataFrames are untyped is
 aslightly inaccurate; they have types, but Spark maintains them completely and only checks whether those types
 line up to those specified in the schema at runtime. Datasets, on the other hand, check whether types conform to
 the specification at compile time. Datasets are only available to Java Virtual Machine (JVM)–based languages
 (Scala and Java) and we specify types with case classes or Java beans.

 To Spark (in Scala), DataFrames are simply Datasets of Type Row. The “Row” type is Spark’s internal
 representation of its optimized in-memory format for computation.
 */
  def main(args: Array[String]): Unit = {


    // BASIC STRUCTURED OPERATIONS
    val spark = SparkSession
      .builder()
      .appName("StructuredAPI")
      .master("local[*]")
      .getOrCreate()
    val df = spark.read.format("json").load("/home/max/Spark-The-Definitive-Guide-master/data/flight-data/json/2015-summary.json")

    //to look at the df Schema. we can either let data source decide the schema(schema-on-read) or explicitly mention
    //the types. For prod scenarios, its best to explicitly mention.
    df.printSchema()

    /* A schema is a StructType made up of a number of fields, StructFields, that have a name,type and a Boolean flag
    which specifies whether that column can contain missing or null values. If the types in the data (at runtime)
    do not match the schema, Spark will throw an error */

    // defining Schema explicitly. The metadata is a way of storing information about this column
    val myManualSchema = StructType(Array(StructField("DEST_COUNTRY_NAME",StringType,true),StructField("ORIGIN_COUNTRY_NAME",
      StringType, true), StructField("count", LongType, false, Metadata.fromJson("{\"hello\":\"world\"}"))))

    val dfWithManualSchema = spark.read
      .format("json")
      .schema(myManualSchema)
      .load("/home/max/Spark-The-Definitive-Guide-master/data/flight-data/json/2015-summary.json")

    dfWithManualSchema.printSchema()


    //columns,rows,expression. Columns are same as what we see in a table. and the transformation performed on the
    //column is called expression. rows are simply a record in dataframe of type Row.
    print(dfWithManualSchema.col("count"))

    //rows can be created manually. the order of values should match the dataframe order
    print(dfWithManualSchema.first())
    val myRow = Row("Hello",null, 1, false)

    //accessing the rows.
    print(myRow.getString(0)) //returns string
    print(myRow(0).asInstanceOf[String]) //returns string

    """ DATAFRAME Transformations. 1)add or remove rows/columns 2)transform row -> column or vice versa 3)sort the rows"""

    //register as table to query in SQL style
    df.createOrReplaceTempView("dfTable")

    //creating DF on the fly.
    val mySchema = StructType(Array(StructField("col1", StringType, true), StructField("col2", LongType, true)))
    val myRows = Seq(Row("Hello",1L))
    val myRDD = spark.sparkContext.parallelize(myRows) //ParallelCollectionRDD
    myRDD.collect.foreach(println)
    val manualDF = spark.createDataFrame(myRDD, mySchema)
    manualDF.show()

    // other way to create DF. but not recommended for production since toDF cause issues with `null` types.
    import spark.sqlContext.implicits._
    val manualDF1 = Seq(("Hello", 2, 1L)).toDF("col1", "col2", "col3")
    manualDF1.show()

    // select
    df.select("DEST_COUNTRY_NAME").show(2)
    df.select("ORIGIN_COUNTRY_NAME", "DEST_COUNTRY_NAME").show(2)

    // more ways to do the select. all results the same.
    df.select(
      df.col("DEST_COUNTRY_NAME"),
      col("DEST_COUNTRY_NAME"),
      column("DEST_COUNTRY_NAME"),
      'DEST_COUNTRY_NAME,
      $"DEST_COUNTRY_NAME",
      expr("DEST_COUNTRY_NAME")).show(2)

    //expr and selectExpr
    df.select(expr("DEST_COUNTRY_NAME as destination")).show(2)
    df.select(expr("DEST_COUNTRY_NAME as destination")
      .alias("DEST_COUNTRY_NAME"))
      .show(2) //equivalent to withColumnRenamed function.

    // select followed by expression is best expressed by selectExpr function.
    df.selectExpr("DEST_COUNTRY_NAME as destination", "ORIGIN_COUNTRY_NAME").show(2)

    //more expressions
    df.selectExpr("ORIGIN_COUNTRY_NAME as origin", "(ORIGIN_COUNTRY_NAME = DEST_COUNTRY_NAME) as within" +
      "_country").show(20)
    //other way to do for above expression with when function
    df.withColumn("within_country", when(col("ORIGIN_COUNTRY_NAME").===("DEST_COUNTRY_NAME"), true)
      .otherwise(false)).show(20)
    df.selectExpr("avg(count)", "count(distinct(DEST_COUNTRY_NAME))").show()

    //Literals. For converting a programming specific literal value to spark types. cant use selectExpr here, since lit
    // function needs to be used and it cant be specified inside expr. lit is a function itself like expr and col/column.
    //Lit is useful when you might need to check whether a value is greater than some constant
    //or other programmatically created variable.
    df.select(expr("*"), lit(4000).as("One"))
      .where("count > One")
      .show(20)


  }
}
