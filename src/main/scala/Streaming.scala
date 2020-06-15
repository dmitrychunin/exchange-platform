import java.util.Properties

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SparkSession}
//import ru.yandex.clickhouse._

object Streaming {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .config("spark.sql.caseSensitive", "true")
      .getOrCreate()

    val schema1 = ScalaReflection.schemaFor[OrderUpdate].dataType.asInstanceOf[StructType]
    schema1.printTreeString()
    val schema: StructType = StructType(Array(
      StructField("e", StringType, nullable = false),
      StructField("E", StringType, nullable = false),
      StructField("s", StringType, nullable = false),
      StructField("U", StringType, nullable = false),
      StructField("u", StringType, nullable = false)//,
//      StructField("b", ArrayType(StructType(Array(
//        StructField("_1", StringType, nullable = false),
//        StructField("_2", StringType, nullable = false)
//      )), containsNull=false), nullable = false),
//      StructField("a", ArrayType(StructType(Array(
//        StructField("_1", StringType, nullable = false),
//        StructField("_2", StringType, nullable = false)
//      )), containsNull=false), nullable = false)
    ))
    schema.printTreeString()
//    (e: String, E: String, s: String, U: String, u: String, b: List[(String, String)], a: List[(String, String)])
    import spark.implicits._
    val ds = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "order_string3")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", "false")
      .load
      .selectExpr("CAST(value AS STRING)")
      .select(from_json($"value", schema).alias("parsed_value"))
      .select("parsed_value.*")
//      .as[OrderUpdate]

      //      todo parse from json
//      .as[String]

    val jdbcUrl = "jdbc:clickhouse://127.0.0.1:8123/binance"
    val ckProperties = new Properties()
    ckProperties.put("user", "default")
    ckProperties.put("password", "")

//    ds.show()
//todo распарсить json-строку в df с несколькими колонками, тогда можно будет сконвертировать в объект
    val query = ds.writeStream.foreachBatch ( (batchDF: Dataset[Row], _: Long) => {
      batchDF.show()
//      val schema = schema_of_json(lit(batchDF.select($"value").as[String].first))
//      batchDF.withColumn("jsonData", from_json($"value", schema)).select("jsonData.u")
//      batchDF.show()
//      batchDF
//        .select("parsed_value.e, parsed_value.E, parsed_value.s, parsed_value.U, parsed_value.u")
//        .show()

      batchDF
//        .select(
//          col("e").as("e"),
//          col("E").as("E"),
//          col("s").as("s"),
//          col("u").as("u"),
//          col("U").as("U")
//        )

        .write.mode("append").option("driver", "ru.yandex.clickhouse.ClickHouseDriver").jdbc(jdbcUrl, table = "binance.order", ckProperties)
    }).start()

    query.awaitTermination()
  }
}
