package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
//Vem da dependencia do lombok, para tornar os valores do id unicos, vai gerar o equal() e o hashCode() apenas para o id
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//construtor vazio no package fica visivel apenas para classes do mesmo pacote
@NoArgsConstructor(access = AccessLevel.PACKAGE )
@Setter(AccessLevel.PRIVATE)//Apenas a nivel privado, dentro da classe
@Getter
public class Delivery {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;


    private UUID courierId;

    private DeliveryStatus status;

    private OffsetDateTime placedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime expectedDeliveryAt;
    private OffsetDateTime fulfilledAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItems;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "sender_zip_code")),
            @AttributeOverride(name = "street", column = @Column(name = "sender_street")),
            @AttributeOverride(name = "number", column = @Column(name = "sender_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "sender_complement")),
            @AttributeOverride(name = "name", column = @Column(name = "sender_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "sender_phone"))
    })
    private ContactPoint sender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "recipient_zip_code")),
            @AttributeOverride(name = "street", column = @Column(name = "recipient_street")),
            @AttributeOverride(name = "number", column = @Column(name = "recipient_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "recipient_complement")),
            @AttributeOverride(name = "name", column = @Column(name = "recipient_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_phone"))
    })
    private ContactPoint recipient;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true, mappedBy = "delivery")
    private List<Item> items = new ArrayList<>();

    //construtor via metodo/padrão de factory, aqui um static factory
    //criando uma delivery em um status de rascunho/inicial
    //instanciar uma delivery atraves de um static factory com status de draft
    public static Delivery draft(){
       Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());//UUID vai gerar um id para não depender do banco de dados
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);
       return delivery;
    }

    public void changeItemQuantity(UUID itemId, int quantity){
        Item item = getItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow();
        item.setQuantity(quantity);
        calculateTotalItems();
    }

    public UUID addItem(String name, Integer quantity){
        Item item = Item.brandNew(name, quantity, this);
        items.add(item);
        calculateTotalItems();
        return item.getId();
    }

    public void removeItem(UUID itemId){
        items.removeIf(item -> item.getId().equals(itemId));
        calculateTotalItems();
    }

    public void removeItems(){
        items.clear();
        calculateTotalItems();
    }

    public void editPreparationDetails(PreparationDetails details){
        verifyIfCanBeEdited();
        setSender(details.getSender());
        setRecipient(details.getRecipient());
        setDistanceFee(details.getDistanceFee());
        setCourierPayout(details.getCourierPayout());

        setExpectedDeliveryAt(OffsetDateTime.now().plus(details.getExpectedDeliveryTime()));
        setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }

    public void place(){
        verifyIfCanBePlaced();
        this.changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
    }

    public void pickUp(UUID courierId){
        this.setCourierId(courierId);
        this.changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
    }

    public void markAsDelivered(){
        this.changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetDateTime.now());
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    private void calculateTotalItems(){
        int totalItems = getItems().stream().mapToInt(Item::getQuantity).sum();
        setTotalItems(totalItems);
    }

    private void verifyIfCanBePlaced(){
        if(!isFilled()){
            throw new DomainException();
        }
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException();
        }
    }

    private void verifyIfCanBeEdited(){
        if(!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
    }

    private boolean isFilled(){
        return this.getSender() != null
                && this.getRecipient() != null
                && this.getTotalCost() != null;
    }

    private void changeStatusTo(DeliveryStatus newStatus){
        if(newStatus != null && this.getStatus().canNotChangeTo(newStatus)){
            throw  new DomainException(
                    "Invalid Status transition from " +this.getStatus() +
                            " to "+newStatus
            );
        }
        this.setStatus(newStatus);
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PreparationDetails{
        private ContactPoint sender;
        private  ContactPoint recipient;
        private BigDecimal distanceFee;
        private BigDecimal courierPayout;
        private Duration expectedDeliveryTime;
    }
}
