package io.temporal.activity;

import io.temporal.dto.BookShipmentInput;
import io.temporal.dto.BookShipmentResult;
import io.temporal.dto.ShipmentStatusUpdate;

@ActivityInterface
public interface Activities {

    /**
     * BookShipment engages a courier who can deliver the shipment to the customer
     *
     * @param bookShipmentInput
     */
    @ActivityMethod
    BookShipmentResult bookShipment(BookShipmentInput bookShipmentInput);

    /**
     * UpdateShipmentStatus stores the Order status to the database.
     *
     * @param shipmentStatusUpdate
     */
    @ActivityMethod
    void updateShipmentStatus(ShipmentStatusUpdate shipmentStatusUpdate);
}
