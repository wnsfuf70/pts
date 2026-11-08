package io.edifact.routing.spi.rcv.ksc.ext;

import io.edifact.routing.spi.rcv.context.RoutingContext;
import io.edifact.routing.spi.core.EdifactParser;
import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;

import java.util.Arrays;
import java.util.List;

public final class EdifactRoutingKeyExtractor {
	
	private EdifactRoutingKeyExtractor() { }

	public static EdifactRoutingKey extract(String message) {
		
		return new RoutingContext(EdifactParser.parse(message),
				Arrays.<RoutingFieldExtractor<List<String>>> asList(
						new StandardFieldExtractor(),
						new SegmentFieldExtractor())).legacyKey();
	}
	
}