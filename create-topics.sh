#!/bin/bash
echo "Waiting for Kafka..."
sleep 30

echo "Creating topics..."
kafka-topics --bootstrap-server kafka-broker-1:29092 --create --if-not-exists --topic user-role-added --partitions 3 --replication-factor 3 --config min.insync.replicas=1
kafka-topics --bootstrap-server kafka-broker-1:29092 --create --if-not-exists --topic user-events --partitions 3 --replication-factor 3 --config min.insync.replicas=2
kafka-topics --bootstrap-server kafka-broker-1:29092 --list
