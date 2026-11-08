package io.edifact.routing.spi.rcv.exception;

import io.edifact.routing.spi.rcv.ksc.rules.EdifactRoutingKey;

public class RoutingRuleNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final EdifactRoutingKey routingKey;

    public RoutingRuleNotFoundException(EdifactRoutingKey routingKey) {
        super("No EDIFACT routing rule for " + routingKey);
        this.routingKey = routingKey;
    }

    public EdifactRoutingKey getRoutingKey() {
        return routingKey;
    }
    
}