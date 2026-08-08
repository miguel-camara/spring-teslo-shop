package com.teslo.shop.products.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class WholeNumberDoubleSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value == Math.floor(value) && !Double.isInfinite(value)) {
            gen.writeNumber(value.longValue());
        } else if (value != null) {
            gen.writeNumber(value);
        } else {
            gen.writeNull();
        }
    }
}
