package sink;

import dto.StockData;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class PostgreSQLSink {
    
    private static final String JDBC_URL = "jdbc:postgresql://postgres:5432/postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    public static SinkFunction<StockData> createRawDataSink() {
        return JdbcSink.sink(
            "INSERT INTO stock_raw_data (" +
            "   symbol, timestamp, open_price, high_price, low_price, close_price, volume, data_type" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (symbol, timestamp) DO UPDATE SET " +
            "   open_price = EXCLUDED.open_price, " +
            "   high_price = EXCLUDED.high_price, " +
            "   low_price = EXCLUDED.low_price, " +
            "   close_price = EXCLUDED.close_price, " +
            "   volume = EXCLUDED.volume, " +
            "   data_type = EXCLUDED.data_type",
            (JdbcStatementBuilder<StockData>) (preparedStatement, data) -> {
                System.out.println("Storing raw data: " + data.getSymbol());
                preparedStatement.setString(1, data.getSymbol());
                preparedStatement.setString(2, data.getTimestamp());
                preparedStatement.setDouble(3, data.getOpen());
                preparedStatement.setDouble(4, data.getHigh());
                preparedStatement.setDouble(5, data.getLow());
                preparedStatement.setDouble(6, data.getClose());
                preparedStatement.setInt(7, data.getVolume());
                preparedStatement.setString(8, data.getDataType());
            },
            createJdbcExecutionOptions(),
            createJdbcConnectionOptions()
        );
    }
    
    public static SinkFunction<StockData> createRawDataTableSink() {
        return JdbcSink.sink(
            "CREATE TABLE IF NOT EXISTS stock_raw_data (" +
            "   symbol VARCHAR(10) NOT NULL, " +
            "   timestamp TEXT NOT NULL, " +
            "   open_price DOUBLE PRECISION NOT NULL, " +
            "   high_price DOUBLE PRECISION NOT NULL, " +
            "   low_price DOUBLE PRECISION NOT NULL, " +
            "   close_price DOUBLE PRECISION NOT NULL, " +
            "   volume INTEGER NOT NULL, " +
            "   data_type VARCHAR(20) NOT NULL, " +
            "   PRIMARY KEY (symbol, timestamp)" +
            ")",
            (JdbcStatementBuilder<StockData>) (preparedStatement, data) -> { },
            createJdbcExecutionOptions(),
            createJdbcConnectionOptions()
        );
    }

    public static SinkFunction<StockData> createAvgPriceSink() {
        return JdbcSink.sink(
            "INSERT INTO stock_analytics (symbol, timestamp, window_size, avg_price, total_volume) " +
            "VALUES (?, ?, ?, ?, 0) " +
            "ON CONFLICT (symbol, timestamp, window_size) DO UPDATE SET avg_price = EXCLUDED.avg_price",
            (JdbcStatementBuilder<StockData>) (preparedStatement, data) -> {
                System.out.println("Writing average price of " + data.getSymbol() + ", average price:" + data.getAvgPrice());
                preparedStatement.setString(1, data.getSymbol());
                preparedStatement.setString(2, data.getTimestamp());
                preparedStatement.setString(3, "10m");  // window size of 10 minutes
                preparedStatement.setDouble(4, data.getAvgPrice());
            },
            createJdbcExecutionOptions(),
            createJdbcConnectionOptions()
        );
    }
    
    public static SinkFunction<StockData> createVolumeSink() {
        return JdbcSink.sink(
            "INSERT INTO stock_analytics (symbol, timestamp, window_size, avg_price, total_volume) " +
            "VALUES (?, ?, ?, 0, ?) " +
            "ON CONFLICT (symbol, timestamp, window_size) DO UPDATE SET total_volume = EXCLUDED.total_volume",
            (JdbcStatementBuilder<StockData>) (preparedStatement, data) -> {
                System.out.println("Writing total volume of " + data.getSymbol() + ", total volume: " + data.getTotalVolume());
                preparedStatement.setString(1, data.getSymbol());
                preparedStatement.setString(2, data.getTimestamp());
                preparedStatement.setString(3, "10m");  // window size of 10 minutes
                preparedStatement.setInt(4, data.getTotalVolume());
            },
            createJdbcExecutionOptions(),
            createJdbcConnectionOptions()
        );
    }
    
    public static SinkFunction<StockData> createTableSink() {
        return JdbcSink.sink(
            "CREATE TABLE IF NOT EXISTS stock_analytics (" +
            "   symbol VARCHAR(10) NOT NULL, " +
            "   timestamp TEXT NOT NULL, " +
            "   window_size VARCHAR(10) NOT NULL, " +
            "   avg_price DOUBLE PRECISION NOT NULL DEFAULT 0, " +
            "   total_volume INTEGER NOT NULL DEFAULT 0, " +
            "   PRIMARY KEY (symbol, timestamp, window_size)" +
            ")",
            (JdbcStatementBuilder<StockData>) (preparedStatement, data) -> { },
            createJdbcExecutionOptions(),
            createJdbcConnectionOptions()
        );
    }
    
    // jdbc execution options
    private static JdbcExecutionOptions createJdbcExecutionOptions() {
        return JdbcExecutionOptions.builder()
            .withBatchSize(1000)       // 批量处理1000条记录
            .withBatchIntervalMs(200)  // 每200毫秒执行一次批处理
            .withMaxRetries(5)         // 最多重试5次
            .build();
    }
    
   // jdbc connection options
    private static JdbcConnectionOptions createJdbcConnectionOptions() {
        return new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
            .withUrl(JDBC_URL)
            .withDriverName("org.postgresql.Driver")
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .build();
    }
}