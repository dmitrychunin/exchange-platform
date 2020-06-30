# Архитектура платформы

Зеленый - основа компонента готова (есть в проекте/установлен локально и описан в инструкции по развертыванию)

Listen WS смотреть по [ссылке](https://github.com/dmitrychunin/exchange-spark) (ветка feature-scala-streaming-into-hdfs, в качестве временного решения использован и доработан под текущую задачу предыдущий проект)

![Screenshot](https://github.com/dmitrychunin/exchange-platform/blob/master/pic/arch.png)

# Инструкция по развертыванию

* скачать текущий проект
* установить локально docker и выполнить docker-compose up -d из корня проекта
* создать базу binance и таблицу order скриптом ch_order_table_ddl.sql из любого клиента, поддерживающего jdbc
* cкачать [проект](https://github.com/dmitrychunin/exchange-spark) и запустить main()
* настроить локальное окружение (версии важны, подобрана совместимость):
    java 1.8
    scala 2.11.12
    spark 2.4.4
    hadoop 2.7.2
    zeppelin 0.9.0
    hbase 1.4.9
    phoenix 4.15.0
    
## Инструкция по настройке локального окружения

### Java 1.8
* `sudo pacman -S jdk8-openjdk`
* добавить в .bashrc `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk`

### Scala 2.11.12
* скачать и распаковать scala tar
* добавить в .bashrc `export SCALA_HOME=/home/dmitry/installations/scala-2.11.12 export PATH=$SCALA_HOME/bin:$PATH`

### Hadoop 2.7.2
* cкачать и распаковать hadoop tar
* добавить в .bashrc `export HADOOP_HOME=/home/dmitry/installations/hadoop-2.7.2 export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH`
* добавить в etc/hadoop/core-site.xml:
```xml
<property>
	<name>fs.defaultFS</name>
	<value>hdfs://localhost:9000</value>
</property>
```
* добавить в etc/hadoop/hdfs-site.xml
```xml
<property>
	<name>dfs.replication</name>
	<value>1</value>
</property>
```
* Hadoop общается между нодами кластера (в том числе псевдо-кластера) по ssh, проверить: `ssh localhost`
если ssh не настроен, то выполнить:
```bash
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 0600 ~/.ssh/authorized_keys
#to restart already configured sshd
systemctl restart sshd
#проверить еще раз
ssh localhost
```
* добавить JAVA_HOME в hadoop-env.sh: `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk`
* обновить .bashrc: `source.bashrc`
* стартовать namenode + datanode: `start-dfs.sh`
* добавить в etc/hadoop/yarn-site.xml (yarn.nodemanager.pmem-check-enabled + yarn.nodemanager.vmem-check-enabled ???)
```xml
<property>
	<name>yarn.nodemanager.aux-services</name>
	<value>mapreduce_shuffle</value>
</property>

<property>
    <name>yarn.nodemanager.pmem-check-enabled</name>
    <value>false</value>
</property>

<property>
    <name>yarn.nodemanager.vmem-check-enabled</name>
    <value>false</value>
</property>
```
* добавить в etc/hadoop/mapred-site.xml
```xml
<property>
	<name>mapreduce.framework.name</name>
	<value>yarn</value>
</property>
```
* стартовать YARN кластер менеджер: `start-yarn.s`h
* проверить процессы командой jps (должны появиться NodeManager, ResourceManager, SecondaryNameNode, DataNode, NameNode)
* проверить namenode: [http://nds.name.domain:50090/](http://localhost:50070/)
* проверить yarn [http://nds.name.domain:8088/](localhost:8088/)
### Spark 2.4.4
* скачать и распаковать spark tar
* добавить в .bashrc `export SPARK_HOME=/home/dmitry/installations/spark-2.4.4-bin-hadoop2.7 export PATH=$SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH`
* добавить в spark-env.sh `export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop`
* проверить: `spark-shell`
* проверить примером из src:
```bash
spark-submit \
--class org.apache.spark.examples.SimpleSkewedGroupByTest \
--conf spark.ui.port=12901 \
--master yarn \
--deploy-mode cluster \
--driver-memory 4g \
--executor-memory 2g \
--executor-cores 1 \
--queue default \
/home/dmitry/installations/spark-2.4.4-bin-hadoop2.7/examples/jars/spark-examples_2.11-2.4.4.jar
```
### HBase 1.4.9
* скачать и распаковать hbase tar
* добавить в .bashrc `export HBASE_HOME=/home/dmitry/installations/hbase-1.4.9 export PATH=HBASE_HOME/bin:$PATH`
* через docker уже запущен zookeeper -> 
    нужно указать хост и порт уже запущенного ZK
    hbase.cluster.distributed true (???)
    HBASE_MANAGES_ZK false
* в conf/hbase-env.sh изменить значение HBASE_MANAGES_ZK на false    
* добавить в $HBASE_HOME/conf/hbase-site.xml:  
```xml
<property>
   <name>hbase.zookeeper.quorum</name>
   <value>localhost</value>
</property>
<property>
   <name>hbase.zookeeper.property.clientPort</name>
   <value>2181</value>
</property>
<property>
  <name>hbase.cluster.distributed</name>
  <value>true</value>
</property>
<property>
  <name>hbase.tmp.dir</name>
  <value>./tmp</value>
</property>
<property>
  <name>hbase.rootdir</name>
  <value>hdfs://localhost:9000/hbase</value>
</property>
<property>
  <name>hbase.unsafe.stream.capability.enforce</name>
  <value>false</value>
</property>
```
* обновить .bashrc: `source.bashrc`
* стартовать hbase: `start-hbase.sh`
* проверить процессы командой jps (должны быть запущены HMaster и HRegionServer, HQuorum быть не должно, он запущен через докер)
* проверить hbase, создав таблицу, добавить одну строку и сделать select:
```bash
hbase shell
status
create 'newtbl', 'knowledge'
put 'newtbl', 'r1', 'knowledge:sport', 'cricket'
scan 'newtbl'
list
```
* проверить в админке мастера новую таблицу [http://localhost:16010/master-status](http://localhost:16010/master-status)     

### Apache Phoenix
* скачать и распаковать hbase tar
* добавить в .bashrc `export PHOENIX_HOME=/home/dmitry/installations/apache-phoenix-4.15.0-HBase-1.4-bin export PATH=$PATH:$PHOENIX_HOME/bin`
* скопировать server jar в $HBASE_HOME/lib из $PHOENIX_HOME: `cp phoenix-4.15.0-HBase-1.4-server.jar ~/installations $HBASE_HOME/lib` 
* рестарт HBase: `stop-hbase.sh && start-hbase.sh`
* установить python2: `sudo pacman -S python2`
* 
* проверить (используемые файлы лежат в корне проекта):
```bash
python2 bin/sqlline.py
!tables
!quit
python2 psql.py us_population.sql us_population.csv us_population_queries.sql
```
* проверить в админке мастера новую таблицу [http://localhost:16010/master-status](http://localhost:16010/master-status)     

### Zeppelin
## Troubleshooting