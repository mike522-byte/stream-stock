package deserializer;

import dto.StockData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import java.io.IOException;

public class StockDataDeserializationSchema implements DeserializationSchema<StockData> {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public StockData deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, StockData.class);
    }

    @Override
    public boolean isEndOfStream(StockData nextElement) {
        return false;
    }

    @Override
    public TypeInformation<StockData> getProducedType() {
        return TypeInformation.of(StockData.class);
    }
}