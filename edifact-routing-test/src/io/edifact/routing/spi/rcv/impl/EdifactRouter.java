package io.edifact.routing.spi.rcv.impl;

import io.edifact.routing.spi.core.EdifactParser;
import io.edifact.routing.spi.rcv.context.RoutingContext;
import io.edifact.routing.spi.rcv.exception.RoutingRuleNotFoundException;
import io.edifact.routing.spi.rcv.ksc.ext.RoutingFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.ext.SegmentFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.ext.StandardFieldExtractor;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingRule;

import java.util.*;

public final class EdifactRouter {
	
	private final Map<String, List<EdifactRoutingRule>> index;
	private final List<RoutingFieldExtractor<List<String>>> extractors;

	public EdifactRouter(Collection<EdifactRoutingRule> rules) {
		this(rules, Arrays.<RoutingFieldExtractor<List<String>>> asList(new StandardFieldExtractor(), new SegmentFieldExtractor()));
	}

	public EdifactRouter(Collection<EdifactRoutingRule> rules, Collection<RoutingFieldExtractor<List<String>>> extractors) {
		
		if (rules == null || extractors == null){
			throw new IllegalArgumentException("rules/extractors required");
		}
		
		Map<String, List<EdifactRoutingRule>> mutable = new HashMap<String, List<EdifactRoutingRule>>();
		
		for (EdifactRoutingRule rule : rules) {
			
			if (rule == null){
				throw new IllegalArgumentException("null rule");
			}
			
			String type = rule.getRoutingKey().getEdifactType();
			if (!mutable.containsKey(type)){
				mutable.put(type, new ArrayList<EdifactRoutingRule>());
			}
			
			mutable.get(type).add(rule);
		}
		
		Map<String, List<EdifactRoutingRule>> frozen = new HashMap<String, List<EdifactRoutingRule>>();
		
		for (Map.Entry<String, List<EdifactRoutingRule>> e : mutable.entrySet()){
			frozen.put(e.getKey(), Collections.unmodifiableList(new ArrayList<EdifactRoutingRule>(e.getValue())));
		}
		
		this.index = Collections.unmodifiableMap(frozen);
		this.extractors = Collections.unmodifiableList(new ArrayList<RoutingFieldExtractor<List<String>>>(extractors));
		
	}

	public EdifactRoutingRule resolve(String ediMessage) {
		RoutingContext context = new RoutingContext(EdifactParser.parse(ediMessage), extractors);
		EdifactRoutingKey key = context.legacyKey();
		return resolveInternal(key, context);
	}

	public EdifactRoutingRule resolve(EdifactRoutingKey key) {
		return resolveInternal(key, null);
	}

	private EdifactRoutingRule resolveInternal(EdifactRoutingKey key, RoutingContext context) {
		
		if (key == null){
			throw new IllegalArgumentException("routing key is required");
		}
		
		EdifactRoutingRule selected = null;
		List<EdifactRoutingRule> candidates = index.get(key.getEdifactType());
		
		if (candidates != null){
			for (EdifactRoutingRule rule : candidates) {
				
				boolean match = context == null ? rule.matches(key) : rule.matches(context, key);
				if (match) {
					if (selected != null){
						throw new IllegalStateException(
								"Ambiguous EDIFACT routing: "
										+ selected.getInterfaceItemId()
										+ " and " + rule.getInterfaceItemId()
										+ " for " + key);
					}
					selected = rule;
				}
			}
		}
		
		if (selected == null) {
		    throw new RoutingRuleNotFoundException(key);
		}
		
		return selected;
	}
	
}