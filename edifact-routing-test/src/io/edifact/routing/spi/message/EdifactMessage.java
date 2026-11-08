package io.edifact.routing.spi.message;

import java.util.*;

public final class EdifactMessage {
	
	private final List<Segment> segments;

	public EdifactMessage(List<Segment> segments) {
		this.segments = Collections.unmodifiableList(new ArrayList<Segment>(
				segments));
	}

	public List<Segment> segments(String tag) {
		List<Segment> result = new ArrayList<Segment>();
		for (Segment s : segments)
			if (tag.equals(s.tag()))
				result.add(s);
		return Collections.unmodifiableList(result);
	}

	public static final class Segment {
		
		private final List<List<String>> elements;

		public Segment(List<List<String>> input) {
			
			List<List<String>> copy = new ArrayList<List<String>>();
			
			for (List<String> item : input){
				copy.add(Collections.unmodifiableList(new ArrayList<String>(item)));
			}
			
			this.elements = Collections.unmodifiableList(copy);
		}

		public String tag() {
			return value(0, 0);
		}

		public String value(int element, int component) {
			if (element < 0 || component < 0 || element >= elements.size())
				return null;
			List<String> c = elements.get(element);
			if (component >= c.size())
				return null;
			String value = c.get(component).trim();
			return value.isEmpty() ? null : value;
		}
		
	}
	
}
