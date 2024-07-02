package io.temporal.dto;

import java.util.List;

public record ShipmentInput(String requestorWID,
                            String id,
                            List<ShipmentItem> items) {
}
