package utils;

import dto.StockData;
import org.apache.flink.api.common.functions.AggregateFunction;

public class AveragePriceAggregator implements AggregateFunction<StockData, AveragePriceAggregator.AveragePriceAccumulator, StockData> {

    @Override
    public AveragePriceAccumulator createAccumulator() {
        return new AveragePriceAccumulator();
    }

    @Override
    public AveragePriceAccumulator add(StockData value, AveragePriceAccumulator accumulator) {
        accumulator.sum += value.getClose();
        accumulator.count++;
        
        // using the first record's symbol and timestamp
        if (accumulator.count == 1) {
            accumulator.symbol = value.getSymbol();
            accumulator.timestamp = value.getTimestamp();
        }
        
        return accumulator;
    }

    @Override
    public StockData getResult(AveragePriceAccumulator accumulator) {
        StockData result = new StockData();
        result.setSymbol(accumulator.symbol);
        result.setTimestamp(accumulator.timestamp);
        result.setAvgPrice(accumulator.sum / accumulator.count);
        return result;
    }

    @Override
    public AveragePriceAccumulator merge(AveragePriceAccumulator a, AveragePriceAccumulator b) {
        AveragePriceAccumulator merged = new AveragePriceAccumulator();
        merged.symbol = a.symbol;
        merged.sum = a.sum + b.sum;
        merged.count = a.count + b.count;
        return merged;
    }
    
    public static class AveragePriceAccumulator {
        public String symbol = "";
        public double sum = 0;
        public int count = 0;
        public String timestamp = "";
    }
}