import requests
import json
import time
import schedule
import logging
from kafka import KafkaProducer
from datetime import datetime

# Configuration
API_KEY = "20HXQRUT52LL145K"
KAFKA_BROKER = "kafka:9092"
SYMBOLS = ["META"]  # stock code of interest

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('stock_data_collector.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Kafka producer
producer = KafkaProducer(
    bootstrap_servers=[KAFKA_BROKER],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

def fetch_realtime_bulk_quotes(symbols):
    """Fetch real-time bulk quotes data"""
    try:
        # Convert list to comma-separated string if symbols is a list
        symbol_string = ",".join(symbols) if isinstance(symbols, list) else symbols
        
        url = f"https://www.alphavantage.co/query?function=REALTIME_BULK_QUOTES&symbol={symbol_string}&apikey={API_KEY}"
        logger.debug(f"Fetching bulk quotes from URL: {url}")

        response = requests.get(url)
        response.raise_for_status()  # Raises an HTTPError for bad responses
        data = response.json()

        # Check if we have data in the response
        if "data" not in data:
            error_msg = data.get('message', 'Unknown error in API response')
            logger.error(f"Error in bulk quotes response: {error_msg}")
            return

        stock_data_topic = "stock-raw-data"
        quotes = data["data"]

        for quote in quotes:
            try:
                message = {
                    "symbol": quote["symbol"],
                    "timestamp": quote["timestamp"],
                    "open": float(quote["open"]),
                    "high": float(quote["high"]),
                    "low": float(quote["low"]),
                    "close": float(quote["close"]),
                    "volume": int(quote["volume"]),
                    "dataType": "realtime"
                }

                producer.send(stock_data_topic, message)
                logger.info(f"Sent {quote['symbol']} data to {stock_data_topic} topic")
                
            except (KeyError, ValueError) as e:
                logger.error(f"Error processing quote for {quote.get('symbol', 'unknown')}: {str(e)}")
                continue

    except requests.exceptions.RequestException as e:
        logger.error(f"Request failed for bulk quotes: {str(e)}")
    except json.JSONDecodeError as e:
        logger.error(f"Failed to decode JSON response: {str(e)}")
    except Exception as e:
        logger.error(f"Unexpected error in fetch_realtime_bulk_quotes: {str(e)}", exc_info=True)

def fetch_indicators(symbol):
    """Get RSI indicator for a symbol"""
    try:
        url = f"https://www.alphavantage.co/query?function=RSI&symbol={symbol}&interval=daily&time_period=14&series_type=close&apikey={API_KEY}"
        logger.debug(f"Fetching RSI for {symbol} from URL: {url}")

        response = requests.get(url)
        response.raise_for_status()
        data = response.json()

        if "Technical Analysis: RSI" not in data:
            error_msg = data.get('Note', 'Unknown error in API response')
            logger.warning(f"RSI data not available for {symbol}: {error_msg}")
            return

        indicators_topic = "stock-indicators"
        rsi_data = data["Technical Analysis: RSI"]
        latest_date = list(rsi_data.keys())[0]
        latest_rsi = float(rsi_data[latest_date]["RSI"])

        message = {
            "symbol": symbol,
            "timestamp": latest_date,
            "indicator": "RSI",
            "value": latest_rsi,
            "dataType": "indicator"
        }

        producer.send(indicators_topic, message)
        logger.info(f"Sent {symbol} RSI data to {indicators_topic} topic")

    except requests.exceptions.RequestException as e:
        logger.error(f"Request failed for RSI ({symbol}): {str(e)}")
    except (KeyError, IndexError, ValueError) as e:
        logger.error(f"Error processing RSI data for {symbol}: {str(e)}")
    except Exception as e:
        logger.error(f"Unexpected error in fetch_indicators for {symbol}: {str(e)}", exc_info=True)

def job():
    """Scheduled job to collect stock data"""
    start_time = datetime.now()
    logger.info(f"Starting stock data collection at {start_time.strftime('%Y-%m-%d %H:%M:%S')}")
    
    try:
        fetch_realtime_bulk_quotes(SYMBOLS)

        for symbol in SYMBOLS:
            fetch_indicators(symbol)

        producer.flush()
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        
        logger.info(
            f"Completed collection at {end_time.strftime('%Y-%m-%d %H:%M:%S')} "
            f"(duration: {duration:.2f} seconds)"
        )
        
    except Exception as e:
        logger.error(f"Job failed: {str(e)}", exc_info=True)

# Configure job frequency based on Alpha Vantage API limit
schedule.every(1).minutes.do(job)

# Initialize first job run
logger.info("Starting stock data collector service")
job()

# Keep the script running
try:
    while True:
        schedule.run_pending()
        time.sleep(1)  # for more efficient CPU usage
except KeyboardInterrupt:
    logger.info("Received keyboard interrupt, shutting down...")
except Exception as e:
    logger.error(f"Unexpected error in main loop: {str(e)}", exc_info=True)
finally:
    producer.close()
    logger.info("Kafka producer closed. Service stopped.")