import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import dto.StockData;
import sink.PostgreSQLSink;
import utils.AveragePriceAggregator;
import utils.VolumeAggregator;

public class StockProcessor {
    
    public static void main(String[] args) throws Exception {
  
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        final ObjectMapper objectMapper = new ObjectMapper();
        
        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers("kafka:9092")
            .setTopics("stock-raw-data")
            .setGroupId("stock-analytics-group")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();
            
        // get kafka data stream and convert JSON to StockData
        DataStream<StockData> stockDataStream = env
            .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
            .map(jsonString -> objectMapper.readValue(jsonString, StockData.class))
            .name("JSON to StockData");
            
        // debugging: print the received stock data
        stockDataStream.print().name("Received Stock Data");
        
        // create PostgreSQL tables
        stockDataStream
            .filter(data -> true) // 简单过滤器以生成新的流
            .addSink(PostgreSQLSink.createTableSink())
            .name("Create Stock Analytics Table");
            
        stockDataStream
            .filter(data -> true)
            .addSink(PostgreSQLSink.createRawDataTableSink())
            .name("Create Raw Stock Data Table");
        
        // store raw data into PostgreSQL
        stockDataStream
            .addSink(PostgreSQLSink.createRawDataSink())
            .name("Raw Stock Data PostgreSQL Sink");
        
        // compute average price in 10-minute windows
        DataStream<StockData> avgPriceStream = stockDataStream
            .keyBy(StockData::getSymbol)
            .window(TumblingProcessingTimeWindows.of(Time.minutes(10)))
            .aggregate(new AveragePriceAggregator())
            .name("Average Price Window");
        
        // compute total volume in 10-minute windows
        DataStream<StockData> volumeStream = stockDataStream
            .keyBy(StockData::getSymbol)
            .window(TumblingProcessingTimeWindows.of(Time.minutes(10)))
            .aggregate(new VolumeAggregator())
            .name("Volume Window");
        
        // add sinks for average price and volume
        avgPriceStream
            .addSink(PostgreSQLSink.createAvgPriceSink())
            .name("Average Price PostgreSQL Sink");
        
        volumeStream
            .addSink(PostgreSQLSink.createVolumeSink())
            .name("Volume PostgreSQL Sink");
        
        // execute the Flink job
        env.execute("Stock Analytics Job");
    }
}