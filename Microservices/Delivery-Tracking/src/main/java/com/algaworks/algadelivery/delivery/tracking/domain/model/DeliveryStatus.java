package com.algaworks.algadelivery.delivery.tracking.domain.model;

import java.util.Arrays;
import java.util.List;

public enum DeliveryStatus {
    DRAFT,
    WAITING_FOR_COURIER(DRAFT),
    IN_TRANSIT(WAITING_FOR_COURIER),
    DELIVERED(IN_TRANSIT);

    private final List<DeliveryStatus> previousStatususes;

    DeliveryStatus(DeliveryStatus... previousStatususes) {
        this.previousStatususes = Arrays.asList(previousStatususes);
    }

    public boolean canNotChangeTo(DeliveryStatus newStatus){
        DeliveryStatus current = this;
        return !newStatus.previousStatususes.contains(current);
    }

    public boolean canChangeTo(DeliveryStatus newStatus){
        return !canNotChangeTo(newStatus);
    }
}
