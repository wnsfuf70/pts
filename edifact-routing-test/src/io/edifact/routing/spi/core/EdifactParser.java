package io.edifact.routing.spi.core;

import java.util.ArrayList;
import java.util.List;

import io.edifact.routing.spi.message.EdifactMessage;

public final class EdifactParser {
	
	private EdifactParser() { }

	public static EdifactMessage parse(String message) {
		
		if (message == null || message.trim().isEmpty()){
			throw new IllegalArgumentException("message is empty");
		}
		
		int offset = 0;
		while (offset < message.length() && (Character.isWhitespace(message.charAt(offset)) || message.charAt(offset) == '\ufeff')){
			offset++;
		}
		
		char component = ':', element = '+', release = '?', segment = '\'';
		if (message.startsWith("UNA", offset)) {
			
			if (offset + 9 > message.length()){
				throw new IllegalArgumentException("truncated UNA header");
			}
			
			component = message.charAt(offset + 3);
			element = message.charAt(offset + 4);
			release = message.charAt(offset + 6);
			segment = message.charAt(offset + 8);
			offset += 9;
			
		}
		
		List<EdifactMessage.Segment> segments = new ArrayList<EdifactMessage.Segment>();
		List<List<String>> elements = new ArrayList<List<String>>();
		List<String> comps = new ArrayList<String>();
		StringBuilder value = new StringBuilder();
		
		boolean pending = false;
		
		for (int i = offset; i < message.length(); i++) {
			
			char ch = message.charAt(i);
			
			if (ch == release) {
				if (++i >= message.length()){
					throw new IllegalArgumentException( "dangling EDIFACT release character");
				}
				value.append(message.charAt(i));
				pending = true;
			} 
			else if (ch == component) {
				comps.add(value.toString());
				value.setLength(0);
				pending = true;
			} 
			else if (ch == element) {
				comps.add(value.toString());
				value.setLength(0);
				elements.add(comps);
				comps = new ArrayList<String>();
				pending = true;
			} 
			else if (ch == segment) {
				comps.add(value.toString());
				value.setLength(0);
				elements.add(comps);
				comps = new ArrayList<String>();
				EdifactMessage.Segment parsed = new EdifactMessage.Segment(elements);
				if (parsed.tag() != null){
					segments.add(parsed);
				}
				elements = new ArrayList<List<String>>();
				pending = false;
			} 
			else {
				value.append(ch);
				if (!Character.isWhitespace(ch)){
					pending = true;
				}
			}
			
		}
		
		if (pending || !elements.isEmpty()){
			throw new IllegalArgumentException("EDIFACT message ends without segment terminator");
		}
		
		EdifactMessage result = new EdifactMessage(segments);
		
		if (result.segments("UNH").size() != 1) {
			throw new IllegalArgumentException("expected 1 UNH, actual " + result.segments("UNH").size());
		}
		
		return result;
	}
	
}