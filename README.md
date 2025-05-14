# Real-time Stock Data Analytics System
![stream-stock drawio](https://github.com/user-attachments/assets/b5ed1996-0e5c-4360-9c0e-12b9dca612bc)

A real-time stock data analytics system built with Apache Flink, Kafka, PostgreSQL, and Grafana. This system fetches real-time stock data from Alpha Vantage API, transfers data through Kafka, processes streams with Flink, stores results in PostgreSQL, and visualizes them using Grafana.



---
## System Architecture


The system consists of the following main components:

1. **Data Collection Layer**: Python scripts fetch real-time stock data from Alpha Vantage API and send it to Kafka
2. **Data Transport Layer**: Kafka message queue with three topics
   - `stock-raw-data`: Raw stock data
   - `stock-indicators`: Technical indicators data
   - `stock-analytics`: Analysis results data
3. **Data Processing Layer**: Apache Flink stream processing engine for real-time data analysis
4. **Storage Layer**: PostgreSQL database to store processed results
5. **Visualization Layer**: Grafana dashboards for real-time visualization
---
## Prerequisites

* [Docker](https://www.docker.com/get-started) and [Docker Compose](https://docs.docker.com/compose/install/)
* [Java 11](https://adoptium.net/)
* [Maven](https://maven.apache.org/download.cgi)
* [Alpha Vantage API Key](https://www.alphavantage.co/support/#api-key)
---
## Quick Start

1. Clone the Repository

```bash
git clone https://github.com/your-username/stream-stock.git
cd stream-stock
```

2. Configure API Key

In `kafka-producer/collector.py`, replace `API_KEY` with your Alpha Vantage API key:

```python
API_KEY = "YOUR_API_KEY"  # Replace with your Alpha Vantage API key
```

3. Compile Flink Processor

```bash
cd flink-processor
mvn clean package
cd ..
```

 4. Start the System

```bash
docker-compose up -d
```

5. Access Monitoring Interfaces

* **Flink Dashboard**: http://localhost:8081
* **Grafana**: http://localhost:3000 (username: admin, password: admin)

---
## Modifications

### Adding New Stock Symbols

1. Modify the `SYMBOLS` list in `kafka-producer/collector.py`:

```python
SYMBOLS = ["MSFT", "AAPL", "META", "GOOG"]  # Add new stock symbols
```

2. Restart the Kafka producer container:

```bash
docker-compose restart kafka-producer
```

### Configuring Grafana Dashboards

1. Log in to Grafana (http://localhost:3000) using default credentials (admin/admin)
2. Add PostgreSQL data source
   - Host: `postgres:5432`
   - Database: `postgres`
   - User: `postgres`
   - Password: `postgres`
3. Import dashboard templates (located in the `grafana/dashboards/` directory)

### Flink Parameter Adjustments

Modify Flink's TaskManager configuration in `docker-compose.yml`:

```yaml
taskmanager:
  environment:
    - |
      FLINK_PROPERTIES=
      taskmanager.numberOfTaskSlots: 4  # Increase the number of task slots
      taskmanager.memory.process.size: 2048m  # Increase memory size
```

### Kafka Parameter Adjustments

Increase the number of partitions for Kafka topics:
```bash
docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --alter --topic stock-raw-data --partitions 8
```
---

## Technology Stack

* **Data Source**: [Alpha Vantage API](https://www.alphavantage.co/)
* **Message Queue**: [Apache Kafka](https://kafka.apache.org/)
* **Stream Processing**: [Apache Flink](https://flink.apache.org/)
* **Data Storage**: [PostgreSQL](https://www.postgresql.org/)
* **Visualization**: [Grafana](https://grafana.com/)
* **Containerization**: [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)

---

## Contact

For any questions or suggestions, please contact [limm1618@gmail.com].
