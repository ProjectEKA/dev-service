package in.projecteka.devservice.bridge;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import in.projecteka.devservice.bridge.model.ServiceType;

import java.io.IOException;

public class ServiceTypeDeserializer extends JsonDeserializer<ServiceType> {
    @Override
    public ServiceType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        if (node == null) {
            return null;
        }

        String text = node.textValue();

        if (text == null) {
            return null;
        }

        return ServiceType.fromText(text);
    }
}
