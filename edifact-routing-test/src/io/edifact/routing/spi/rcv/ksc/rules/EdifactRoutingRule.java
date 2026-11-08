package io.edifact.routing.spi.rcv.ksc.rules;

import io.edifact.routing.spi.rcv.context.RoutingContext;
import io.edifact.routing.spi.rcv.ksc.condition.RoutingCondition;

import java.util.*;

public final class EdifactRoutingRule {
	
	private final String interfaceItemId;
	private final EdifactRoutingKey routingKey;
	private final RoutingCondition condition;

	public EdifactRoutingRule(String id, EdifactRoutingKey key) {
		this(id, key, null);
	}

	public EdifactRoutingRule(String id, EdifactRoutingKey key, RoutingCondition condition) {
		
		if (id == null || id.trim().isEmpty() || key == null){
			throw new IllegalArgumentException("id/key required");
		}
		
		this.interfaceItemId = id.trim();
		this.routingKey = key;
		this.condition = condition;
	}

	public String getInterfaceItemId() {
		return interfaceItemId;
	}

	public EdifactRoutingKey getRoutingKey() {
		return routingKey;
	}

	public boolean requiresContext() {
		return condition != null;
	}

	public boolean matches(EdifactRoutingKey incoming) {
		
		if (incoming == null){ 
			return false;
		}
		
		if (condition != null){
			throw new IllegalStateException("Rule requires full EDIFACT routing context: " + interfaceItemId);
		}
		
		return matchesLegacy(incoming);
	}

	public boolean matches(RoutingContext context, EdifactRoutingKey incoming) {
		return matchesLegacy(incoming) && (condition == null || condition.matches(context));
	}

	private boolean matchesLegacy(EdifactRoutingKey incoming) {
		
		if (!routingKey.getEdifactType().equals(incoming.getEdifactType())){
			return false;
		}
		
		String bgm = routingKey.getBgmCode();
		
		if (bgm != null && !bgm.equals(incoming.getBgmCode())){
			return false;
		}
		
		Set<String> expected = routingKey.getDocCodes();
		
		if (expected.isEmpty()){
			return true;
		}
		
		for (String doc : incoming.getDocCodes()){
			if (expected.contains(doc)){
				return true;
			}
		}
		
		return false;
	}

}