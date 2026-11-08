package io.edifact.routing.spi.rcv.ksc.condition;

import io.edifact.routing.spi.rcv.context.RoutingContext;

import java.util.*;
import java.util.regex.Pattern;

/** All operators are existential over repeated segment values. */
public final class FieldCondition implements RoutingCondition {
	
	public enum Operator {
		EQ, NE, CONTAINS, REGEX, EXISTS
	}

	private final String field;
	private final Operator operator;
	private final String expected;
	private final Pattern pattern;

	public FieldCondition(String field, Operator operator, String expected) {
		
		if (field == null || field.trim().isEmpty() || operator == null){
			throw new IllegalArgumentException("field/operator required");
		}
			
		if (operator != Operator.EXISTS && expected == null){
			throw new IllegalArgumentException("expected required");
		}
		
		this.field = field.trim();
		this.operator = operator;
		this.expected = expected;
		this.pattern = operator == Operator.REGEX ? Pattern.compile(expected): null;
	}
	
	@Override
	public boolean matches(RoutingContext context) {
		
		List<String> values = context.values(field);
		
		if (operator == Operator.EXISTS){
			return !values.isEmpty();
		}
		
		if (operator == Operator.NE) {
			
			if (values.isEmpty()){
				return false;
			}
			
			for (String v : values){
				if (expected.equals(v)){
					return false;
				}
			}
			
			return true;
		}
		
		for (String value : values) {
			
			if (operator == Operator.EQ && expected.equals(value)){
				return true;
			}
			
			if (operator == Operator.CONTAINS && value.contains(expected)){
				return true;
			}
			
			if (operator == Operator.REGEX && pattern.matcher(value).matches()){
				return true;
			}
			
		}
		
		return false;
	}
	
}