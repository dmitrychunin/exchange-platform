import java.util.Properties

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.{ArrayType, DoubleType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SparkSession}
//import ru.yandex.clickhouse._

object Streaming {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .config("spark.sql.caseSensitive", "true")
      .getOrCreate()

    val schema = ScalaReflection.schemaFor[OrderUpdateInput].dataType.asInstanceOf[StructType]
    schema.printTreeString()

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
      .as[OrderUpdateInput]

    val jdbcUrl = "jdbc:clickhouse://127.0.0.1:8123/binance"
    val ckProperties = new Properties()
    ckProperties.put("user", "default")
    ckProperties.put("password", "")

    val query = ds.writeStream
      .outputMode(OutputMode.Append())
      .format("parquet")
      .option("checkpointLocation", "/checkpoint/order/btcusd")
      .option("path", "/order/btcusd")
//      .trigger(Trigger.Continuous("1 second"))
      .start()

    val query1 = ds.writeStream.foreachBatch ( (batchDF: Dataset[OrderUpdateInput], _: Long) => {
//      batchDF.show()
//      batchDF.printSchema()
//      todo fix Can't get JDBC type for array<struct<_1:string,_2:string>>
//      val orderUpdateDs = batchDF
//        .map((input: OrderUpdateInput) => OrderUpdateOutput(
//          e = input.e,
//          E = input.E,
//          s = input.s,
//          u = input.u,
//          U = input.U,
//          a = input.a.map{ case Seq(a, b) => (a, b) },
//          b = input.b.map{ case Seq(a, b) => (a, b) }
//        ))
        val orderUpdateDs = batchDF
          .map((input: OrderUpdateInput) => OrderUpdateOutput(
            e = input.e,
            E = input.E,
            s = input.s,
            u = input.u,
            U = input.U,
            //draft array saving
            a = input.a.toString(),
            b = input.b.toString()
          ))

//      orderUpdateDs.show()
//      orderUpdateDs.printSchema()
//todo выбрать базу в которой можно хранить вложенные массивы (в clickhouse )
      orderUpdateDs
          .withColumn("created_at", current_timestamp())
        .write.mode("append").option("driver", "ru.yandex.clickhouse.ClickHouseDriver").jdbc(jdbcUrl, table = "binance.order", ckProperties)
    }).start()

    query.awaitTermination()
  }
}
