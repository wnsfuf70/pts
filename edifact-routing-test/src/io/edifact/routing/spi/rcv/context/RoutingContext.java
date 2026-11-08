package io.edifact.routing.spi.rcv.context;

import io.edifact.routing.spi.message.EdifactMessage;
import io.edifact.routing.spi.rcv.ksc.ext.RoutingFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;

import java.util.*;

public final class RoutingContext {
	
    private final EdifactMessage message;
    private final List<RoutingFieldExtractor<List<String>>> extractors;
    private final Map<String, List<String>> cache = new HashMap<String, List<String>>();
    
    public RoutingContext(EdifactMessage message, List<RoutingFieldExtractor<List<String>>> extractors) {
    	
        if (message == null || extractors == null){ 
        	throw new IllegalArgumentException("message/extractors required");
    	}
        
        this.message = message;
        this.extractors = Collections.unmodifiableList(new ArrayList<RoutingFieldExtractor<List<String>>>(extractors));
    }
    
    public List<String> values(String field) {
    	
        if (cache.containsKey(field)) {
        	return cache.get(field);
        }
        
        RoutingFieldExtractor<List<String>> selected = null;
        for (RoutingFieldExtractor<List<String>> extractor : extractors) {
            if (extractor.supports(field)) {
                if (selected != null) throw new IllegalStateException("Multiple extractors for " + field);
                selected = extractor;
            }
        }
        
        if (selected == null) {
        	throw new IllegalArgumentException("Unknown routing field: " + field);
        }
        List<String> values = selected.extract(message, field);
        if (values == null) {
        	throw new IllegalStateException("Extractor returned null: " + field);
        }
        
        List<String> frozen = Collections.unmodifiableList(new ArrayList<String>(values));
        cache.put(field, frozen);
        
        return frozen;
        
    }
    
    public EdifactRoutingKey legacyKey() {
    	
        List<String> type = values("UNH.TYPE"), bgm = values("BGM.CODE");
        
        if (type.size() != 1) {
        	throw new IllegalArgumentException("invalid UNH type");
        }
        
        return new EdifactRoutingKey(type.get(0), bgm.isEmpty() ? null : bgm.get(0), values("DOC.CODE"));
        
    }
    
}
