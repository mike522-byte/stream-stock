package utils;

import dto.StockData;
import org.apache.flink.api.common.functions.AggregateFunction;

public class VolumeAggregator implements AggregateFunction<StockData, VolumeAggregator.VolumeAccumulator, StockData> {

    @Override
    public VolumeAccumulator createAccumulator() {
        return new VolumeAccumulator();
    }

    @Override
    public VolumeAccumulator add(StockData value, VolumeAccumulator accumulator) {
        accumulator.totalVolume += value.getVolume();
        
        // using the first record's symbol and timestamp
        if (accumulator.symbol.isEmpty()) {
            accumulator.symbol = value.getSymbol();
            accumulator.timestamp = value.getTimestamp();
        }
        
        return accumulator;
    }

    @Override
    public StockData getResult(VolumeAccumulator accumulator) {
        StockData result = new StockData();
        result.setSymbol(accumulator.symbol);
        result.setTimestamp(accumulator.timestamp);
        result.setTotalVolume(accumulator.totalVolume);
        return result;
    }

    @Override
    public VolumeAccumulator merge(VolumeAccumulator a, VolumeAccumulator b) {
        VolumeAccumulator merged = new VolumeAccumulator();
        merged.symbol = a.symbol; // using a's symbol
        merged.totalVolume = a.totalVolume + b.totalVolume;
        return merged;
    }
    
    public static class VolumeAccumulator {
        public String symbol = "";
        public int totalVolume = 0;
        public String timestamp = "";
    }
}