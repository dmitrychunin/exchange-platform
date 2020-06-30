#!/bin/bash
sudo systemctl restart sshd
#docker stop nodemanager datanode namenode historyserver resourcemanager
hdfs namenode -format
start-dfs.sh && start-yarn.sh
start-hbase.sh
zeppelin-daemon.sh start
