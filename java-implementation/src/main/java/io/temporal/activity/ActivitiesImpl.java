package io.temporal.activity;

import io.temporal.dto.BookShipmentInput;
import io.temporal.dto.BookShipmentResult;
import io.temporal.dto.ShipmentStatusUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivitiesImpl implements Activities {
    private static final Logger log = LoggerFactory.getLogger(ActivitiesImpl.class);

    @Override
    public BookShipmentResult bookShipment(final BookShipmentInput bookShipmentInput) {
        // TODO

        log.debug("Executing bookShipment activity");
        return new BookShipmentResult(bookShipmentInput.reference() + ":1244");
    }

    @Override
    public void updateShipmentStatus(final ShipmentStatusUpdate shipmentStatusUpdate) {
        // TODO
        log.debug("Executing updateShipmentStatus activity");
    }
}
