package io.edifact.routing.spi.rcv.ksc.ext;

import io.edifact.routing.spi.message.EdifactMessage;

public interface RoutingFieldExtractor<T> {
    
	public boolean supports(String field);

	public T extract(EdifactMessage message, String field);

}