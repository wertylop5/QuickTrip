package io.github.kkysen.quicktrip.json;

import io.github.kkysen.quicktrip.io.IOFunction;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * 
 * 
 * @author Khyber Sen
 */
public abstract class TypeReaderAdapter<T> extends TypeAdapterReaderHelper<T> {
    
    private final Map<String, IOFunction<JsonReader, Object>> propertyReaders = new HashMap<>();
    
    protected final void addPropertyReader(final String name,
            final IOFunction<JsonReader, Object> propertyReader) {
        propertyReaders.put(name, propertyReader);
    }
    
    protected abstract void addPropertyReaders();
    
    public abstract void read() throws IOException;
    
    public abstract T get();
    
    protected final Map<String, Object> readObj() throws IOException, MissingInformationException {
        final Set<String> properties = new HashSet<>(propertyReaders.keySet());
        final Map<String, Object> readObj = new HashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            final String property = in.nextName();
            final IOFunction<JsonReader, Object> propertyReader = propertyReaders.get(property);
            if (propertyReader == null) {
                in.skipValue();
            } else {
                properties.remove(property);
                final Object readProperty = propertyReader.apply(in);
                readObj.put(property, readProperty);
            }
        }
        in.endObject();
        if (properties.size() != 0) {
            throw new MissingInformationException(properties);
        }
        return readObj;
    }
    
    @Override
    public T read(final JsonReader in) throws IOException {
        this.in = in;
        addPropertyReaders();
        read();
        return get();
    }
    
    @Override
    public final void write(final JsonWriter out, final T value) throws IOException {
        // not used
    }
    
}
