package io.temporal.workflow;

import java.util.Objects;

public class Fulfillment {
    private int f;
    public Fulfillment() {
    }

    public Fulfillment(final int f) {
        this.f = f;
    }

    public int getF() {
        return f;
    }

    public void setF(final int f) {
        this.f = f;
    }

    @Override
    public String toString() {
        return "Fulfillment{" +
                "f=" + f +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Fulfillment that = (Fulfillment) o;
        return f == that.f;
    }

    @Override
    public int hashCode() {
        return Objects.hash(f);
    }
}
