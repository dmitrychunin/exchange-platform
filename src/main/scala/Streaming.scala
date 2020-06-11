import java.util.Properties

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, SparkSession}
//import ru.yandex.clickhouse._

object Streaming {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    import spark.implicits._
    val ds = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "order")
      .option("startingOffsets", "earliest")
      .load
      .selectExpr("CAST(value AS STRING)")
      .as[String]

    val jdbcUrl = "jdbc:clickhouse://127.0.0.1:8123/binance"
    val ckProperties = new Properties()
    ckProperties.put("user", "default")
    ckProperties.put("password", "")

//    ds.show()

    val query = ds.writeStream.foreachBatch ( (batchDF: Dataset[String], _: Long) => {
      batchDF.show()
      batchDF.select("e, E, s, U, u", col("a").toString(), col("b").toString()).write.mode("append").option("driver", "ru.yandex.clickhouse.ClickHouseDriver").jdbc(jdbcUrl, table = "binance.order", ckProperties)
    }).start()

    query.awaitTermination()
  }
}
