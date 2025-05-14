## api limit

https://www.alphavantage.co/support/

---

``` bash
### wsl内
cd flink-processor

# 编译flink
mvn clean package

# 创建docker-compose
docker-compose up -d

# 清除docker-compose
docker-compose down -v
```
---
## requirements
mvn > 3.2.5
java = 11
flink = 1.14.6
scala = 2.12


## ports
Kafka: http://localhost:9092
PostgreSQL: http://localhost:5432
Flink Dashboard: http://localhost:8081
Grafana: http://localhost:3000

## how to check kafka info
``` bash
# kafka topics and consumer
docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --list 
docker exec -it kafka kafka-consumer-groups --bootstrap-server kafka:9092 --list

# to manually key in dummy input
docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic stock-raw-data --from-beginning   
docker exec -it kafka kafka-console-producer --bootstrap-server kafka:9092 --topic stock-raw-data
```
---

## how to check postgres info
``` bash
docker exec -it postgres psql -U postgres -c "SELECT * FROM stock_raw_data LIMIT 10;"
```
---

## thoughts
1. 先确保流程能跑通 最后再包装docker file
2. 添加更多转换数据种类如price change, percent change
3. why create taskmanager and jobmanager seperately
- JobManager is the master node that coordinates jobs — it schedules tasks, manages checkpoints, and handles failures.
- TaskManager is the worker node that actually runs the computation — it 
executes tasks in parallel using task slots.
4. timestamp as string
5. analytics采用的timestamp是now而不是raw_data的数据
6. grafana for real-time monitoring and observability
7. the intraday data (including 20+ years of historical data) is updated at the end of each trading day for all users by default.
- use real-time bulk quotes REST API call
8. build indicators sink and flink process pipeline