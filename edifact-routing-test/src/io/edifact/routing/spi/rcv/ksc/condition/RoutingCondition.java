package io.edifact.routing.spi.rcv.ksc.condition;

import io.edifact.routing.spi.rcv.context.RoutingContext;

public interface RoutingCondition {
    public boolean matches(RoutingContext context);
}
