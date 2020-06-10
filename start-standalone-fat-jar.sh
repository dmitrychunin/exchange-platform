echo "Compiling and assembling application..."
sbt clean assembly
SPARK_HOME=/home/dmitry/installations/spark-2.4.5-bin-hadoop2.7
JARFILE=`pwd`/target/scala-2.11/exchange-platform-assembly-0.1.jar
${SPARK_HOME}/bin/spark-submit --class Streaming --master local[3] $JARFILE