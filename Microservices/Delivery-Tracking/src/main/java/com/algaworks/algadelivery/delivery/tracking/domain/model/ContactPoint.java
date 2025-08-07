package com.algaworks.algadelivery.delivery.tracking.domain.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode //padrao é false, vai gerar o equal() e o hashCode() para todos os atributos
@AllArgsConstructor //como ContactPoint é um ValueObject (dentro do contexto
// de Domain Design e da arquitetura) ele precisa de um construtor com valores já informados, ou seja,
// para instanciar, pois valueobject nao pode ter seu status alterado atraves de sets por exemplo
@Builder
@Getter
public class ContactPoint {
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String name;
    private String phone;
}
