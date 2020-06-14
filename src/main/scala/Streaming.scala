import java.util.Properties

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}
//import ru.yandex.clickhouse._

object Streaming {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    val schema = ScalaReflection.schemaFor[OrderUpdate].dataType.asInstanceOf[StructType]
    import spark.implicits._
    val ds = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "order_string3")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", "false")
      .load
      .selectExpr("CAST(value AS STRING)")
      .select(from_json($"value", schema))
      .as[OrderUpdate]

      //      todo parse from json
//      .as[String]

    val jdbcUrl = "jdbc:clickhouse://127.0.0.1:8123/binance"
    val ckProperties = new Properties()
    ckProperties.put("user", "default")
    ckProperties.put("password", "")

//    ds.show()
//todo распарсить json-строку в df с несколькими колонками, тогда можно будет сконвертировать в объект
    val query = ds.writeStream.foreachBatch ( (batchDF: Dataset[OrderUpdate], _: Long) => {
      batchDF.show()
      val schema = schema_of_json(lit(batchDF.select($"value").as[String].first))
      batchDF.withColumn("jsonData", from_json($"value", schema)).select("jsonData.u")
      batchDF.show()
      batchDF
//        .select("e, E, s, U, u", col("a").toString(), col("b").toString())
        .write.mode("append").option("driver", "ru.yandex.clickhouse.ClickHouseDriver").jdbc(jdbcUrl, table = "binance.order", ckProperties)
    }).start()

    query.awaitTermination()
  }
}
