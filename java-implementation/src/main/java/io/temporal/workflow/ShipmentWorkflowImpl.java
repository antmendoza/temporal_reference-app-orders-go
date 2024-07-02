package io.temporal.workflow;

import io.temporal.activity.Activities;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.dto.*;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.TemporalFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ShipmentWorkflowImpl implements ShipmentWorkflow {

    private static final Logger log = LoggerFactory.getLogger(ShipmentWorkflowImpl.class);
    // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L101
    private final Activities activityStub =
            Workflow.newActivityStub(
                    Activities.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(5)).build());

    // You can have more than one Stub or the same interface (Activities.class)
    // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L158
    private final Activities localActivityStub =
            Workflow.newLocalActivityStub(
                    Activities.class,
                    LocalActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(5))
                            // TODO for local activities I think we should limit the number of retries
                            .build());
    private Shipment shipment;

    @Override
    public ShipmentResult run(final ShipmentInput input) {
        this.shipment = new Shipment(input, Status.Pending);

        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L109
        BookShipmentResult bookShipmentResult =
                activityStub.bookShipment(
                        new BookShipmentInput(this.shipment.getId(), this.shipment.getItems()));

        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L120
        shipment.updateStatus(Status.Booked);

        try {
            // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L149
            notifyRequestorOfStatus();
        } catch (TemporalFailure e) {
            // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L150
            throw ApplicationFailure.newFailureWithCause(
                    "Failed to notify requestor of status", "NotifyRequestorFailure", e);
        }

        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L161
        // Rob I think this can never fail, can you confirm it
        localActivityStub.updateShipmentStatus(
                new ShipmentStatusUpdate(this.shipment.getId(), this.shipment.getStatus()));

        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L122
        // Rob this is just a signal handler right?
        // See method shipmentStatusDelivered

        return new ShipmentResult(bookShipmentResult.courierReference());
    }

    private void notifyRequestorOfStatus() {

        //// ShipmentStatusUpdatedSignalName is the name for a signal to notify of an update to a
        // shipment's status.
        final String ShipmentStatusUpdatedSignalName = "ShipmentStatusUpdated";

        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L165
        Workflow.newUntypedChildWorkflowStub(this.shipment.getRequestorWID())
                .signal(
                        ShipmentStatusUpdatedSignalName,
                        new ShipmentStatusUpdatedSignal(
                                this.shipment.getId(), this.shipment.getStatus(), this.shipment.getUpdatedAt()));
    }

    // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L90
    @Override
    public ShipmentStatus statusQuery() {
        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L91
        return new ShipmentStatus(this.shipment);

        // Rob Why this struct does not have items
        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L60
        // and here you have to get them from the input
        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L95

    }

    @Override
    public void shipmentStatusDelivered(ShipmentCarrierUpdateSignal signal) {

        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L139
        this.shipment.updateStatus(signal.status());
    }
}
