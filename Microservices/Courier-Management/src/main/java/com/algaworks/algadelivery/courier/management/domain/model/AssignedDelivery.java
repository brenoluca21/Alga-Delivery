package com.algaworks.algadelivery.courier.management.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
@Entity
public class AssignedDelivery {

    @EqualsAndHashCode.Include
    @Id
    private UUID id;

     private OffsetDateTime assignedAt;

     @ManyToOne(optional = false)
     @Getter(AccessLevel.PRIVATE)
     private Courier courier;

     static AssignedDelivery pending(UUID id, Courier courier){
         AssignedDelivery delivery = new AssignedDelivery();
         delivery.setId(id);
         delivery.setAssignedAt(OffsetDateTime.now());
         delivery.setCourier(courier);
         return delivery;
     }
}
