package io.edifact.routing.spi.rcv.ksc.ext;

import io.edifact.routing.spi.message.EdifactMessage;

import java.util.*;
import java.util.regex.*;

public final class SegmentFieldExtractor implements RoutingFieldExtractor<List<String>> {
	
	private static final Pattern PATH = Pattern.compile("[A-Z0-9]{3}\\.[0-9]+\\.[0-9]+");
	
	@Override
	public boolean supports(String field) {
		return PATH.matcher(field).matches();
	}
	
	@Override
	public List<String> extract(EdifactMessage message, String field) {
		
		if (!supports(field)){
			throw new IllegalArgumentException("Invalid field: " + field);
		}
		
		String[] parts = field.split("\\.");
		int element = Integer.parseInt(parts[1]), component = Integer.parseInt(parts[2]);
		List<String> values = new ArrayList<String>();
		
		for (EdifactMessage.Segment s : message.segments(parts[0])) {
			String value = s.value(element, component);
			if (value != null){
				values.add(value);
			}
		}
		
		return values;
	}
	
}