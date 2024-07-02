package io.temporal.dto;

import java.util.List;

public record BookShipmentInput(String reference, List<ShipmentItem> items) {

}
