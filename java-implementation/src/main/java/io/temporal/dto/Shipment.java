package io.temporal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public final class Shipment {
    private ShipmentInput input;
    private Status status;
    private Date updatedAt;

    public Shipment() {
    }

    public Shipment(ShipmentInput input, Status status) {
        this.input = input;
        this.status = status;
        this.updatedAt = new Date();
    }

    public ShipmentInput getInput() {
        return input;
    }

    public void setInput(final ShipmentInput input) {
        this.input = input;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void updateStatus(final Status status) {
        // https://github.com/temporalio/reference-app-orders-go/blob/3fa995740d2f9ad31890c0ca093bc40524250a19/app/shipment/workflows.go#L145-L148
        this.updatedAt = new Date();
        this.status = status;
    }

    @JsonIgnore
    public String getRequestorWID() {
        return this.input.requestorWID();
    }

    @JsonIgnore
    public List<ShipmentItem> getItems() {
        return this.input.items();
    }

    @JsonIgnore
    public String getId() {
        return this.input.id();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Shipment shipment = (Shipment) o;
        return Objects.equals(input, shipment.input)
                && status == shipment.status
                && Objects.equals(updatedAt, shipment.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, status, updatedAt);
    }
}
