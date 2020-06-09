import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object Streaming {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName(Streaming.getClass.getName)
    val sc = new SparkContext(conf)
    implicit val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()

    val ds = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "order")
      .load()

    ds.writeStream.format("console").start()
  }
}
