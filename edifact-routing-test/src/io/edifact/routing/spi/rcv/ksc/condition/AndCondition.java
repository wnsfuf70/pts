package io.edifact.routing.spi.rcv.ksc.condition;

import io.edifact.routing.spi.rcv.context.RoutingContext;

import java.util.*;

public final class AndCondition implements RoutingCondition {
	
	private final List<RoutingCondition> conditions;

	public AndCondition(Collection<? extends RoutingCondition> conditions) {
		
		if (conditions == null || conditions.contains(null)){
			throw new IllegalArgumentException("conditions required");
		}
		
		this.conditions = Collections.unmodifiableList(new ArrayList<RoutingCondition>(conditions));
	}
	
	@Override
	public boolean matches(RoutingContext context) {
		
		for (RoutingCondition c : conditions){
			if (!c.matches(context)){
				return false;
			}
		}
		
		return true;
	}
	
}
