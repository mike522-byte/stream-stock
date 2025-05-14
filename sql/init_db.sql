-- raw data table
CREATE TABLE stock_raw_data (
   symbol VARCHAR(10) NOT NULL, 
   timestamp TEXT NOT NULL, 
   open_price DOUBLE PRECISION NOT NULL, 
   high_price DOUBLE PRECISION NOT NULL, 
   low_price DOUBLE PRECISION NOT NULL, 
   close_price DOUBLE PRECISION NOT NULL, 
   volume INTEGER NOT NULL, 
   data_type VARCHAR(20) NOT NULL, 
   PRIMARY KEY (symbol, timestamp)
);

-- analytics table
CREATE TABLE stock_analytics (
   symbol VARCHAR(10) NOT NULL, 
   timestamp TEXT NOT NULL, 
   window_size VARCHAR(10) NOT NULL, 
   avg_price DOUBLE PRECISION NOT NULL DEFAULT 0, 
   total_volume INTEGER NOT NULL DEFAULT 0, 
   PRIMARY KEY (symbol, timestamp, window_size)
);

-- indicators table
CREATE TABLE IF NOT EXISTS stock_indicators (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    timestamp TEXT NOT NULL,
    indicator_type VARCHAR(20) NOT NULL,  -- 'RSI', 'MACD', etc.
    value DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(symbol, timestamp, indicator_type)
);

-- indexes for query performance
CREATE INDEX idx_stock_data_symbol_time ON stock_raw_data (symbol, timestamp);
CREATE INDEX idx_stock_analytics_symbol_time ON stock_analytics (symbol, timestamp);
CREATE INDEX idx_stock_indicators_symbol ON stock_indicators (symbol, indicator_type);