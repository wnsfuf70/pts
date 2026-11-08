package io.edifact.routing.spi.rcv.ksc.ext;

import io.edifact.routing.spi.message.EdifactMessage;

import java.util.*;

public final class StandardFieldExtractor implements RoutingFieldExtractor<List<String>> {
	
	public boolean supports(String field) {
		return "UNH.TYPE".equals(field) || "BGM.CODE".equals(field) || "DOC.CODE".equals(field);
	}

	public List<String> extract(EdifactMessage message, String field) {
		List<String> values = new ArrayList<String>();
		if ("UNH.TYPE".equals(field)) {
			EdifactMessage.Segment unh = message.segments("UNH").get(0);
			String name = unh.value(2, 0), release = unh.value(2, 2);
			if (name == null || release == null) {
				throw new IllegalArgumentException("UNH missing message name or release");
			}
			values.add(name + release);
		} 
		else {
			String tag = "BGM.CODE".equals(field) ? "BGM" : "DOC";
			for (EdifactMessage.Segment s : message.segments(tag)) {
				String value = s.value(1, 0);
				if (value != null)
					values.add(value);
			}
			if ("BGM.CODE".equals(field) && message.segments("BGM").size() > 1)
				throw new IllegalArgumentException("multiple BGM segments in single message");
		}
		return values;
	}
}
