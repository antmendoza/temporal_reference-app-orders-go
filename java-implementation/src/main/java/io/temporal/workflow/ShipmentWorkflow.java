package io.temporal.workflow;

import io.temporal.dto.ShipmentCarrierUpdateSignal;
import io.temporal.dto.ShipmentInput;
import io.temporal.dto.ShipmentResult;
import io.temporal.dto.ShipmentStatus;

@WorkflowInterface
interface ShipmentWorkflow {

    @WorkflowMethod
    ShipmentResult run(final ShipmentInput input);

    @QueryMethod
    ShipmentStatus statusQuery();

    @SignalMethod
    void handleCarrierUpdate(ShipmentCarrierUpdateSignal signal);
}
