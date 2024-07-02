package io.temporal.dto;

import java.util.Date;

public record ShipmentStatusUpdatedSignal(String shipmentID, Status status, Date updatedAt) {
}
