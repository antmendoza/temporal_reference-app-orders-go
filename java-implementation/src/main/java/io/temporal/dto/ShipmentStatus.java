package io.temporal.dto;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public final class ShipmentStatus {

    private String id;
    private Status status;
    private Object updatedAt;
    private List<ShipmentItem> items;

    public ShipmentStatus() {
    }

    public ShipmentStatus(final Shipment shipment) {
        this(shipment.getId(), shipment.getStatus(), shipment.getUpdatedAt(), shipment.getItems());
    }

    public ShipmentStatus(
            final String id, final Status status, final Date updatedAt, final List<ShipmentItem> items) {
        this.id = id;
        this.status = status;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Object updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ShipmentItem> getItems() {
        return items;
    }

    public void setItems(final List<ShipmentItem> items) {
        this.items = items;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ShipmentStatus that = (ShipmentStatus) o;
        return Objects.equals(id, that.id)
                && status == that.status
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, updatedAt, items);
    }
}
