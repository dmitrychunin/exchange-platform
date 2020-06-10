import java.util.Properties

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object Streaming {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    val ds = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "order")
      .load()

    val jdbcUrl = "jdbc:clickhouse://127.0.0.1:9000/binance"
    val ckProperties = new Properties()
    ckProperties.put("user", "username")
    ckProperties.put("password", "password")

//    ds.show()

    val query = ds.writeStream.foreachBatch ( (batchDF: DataFrame, batchId: Long) =>
      batchDF.write.mode("append").option("driver", "ru.yandex.clickhouse.ClickHouseDriver").jdbc(jdbcUrl, table = "order", ckProperties)
    ).start()

    query.awaitTermination()
  }
}
