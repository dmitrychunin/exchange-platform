#todo use sbt fat jar way
echo "Compiling and assembling application..."
sbt clean package
SPARK_HOME=/home/dmitry/installations/spark-2.4.5-bin-hadoop2.7
JARFILE=`pwd`/target/scala-2.11/exchange-platform_2.11-0.1.jar
${SPARK_HOME}/bin/spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.5 --class Streaming --master local[3] $JARFILE