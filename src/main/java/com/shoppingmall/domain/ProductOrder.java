package com.shoppingmall.domain;

import com.shoppingmall.domain.enums.OrderStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(value = {AuditingEntityListener.class})
public class ProductOrder {

    @Id     // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String orderNumber;
    @Column
    private String orderName;
    @Column
    private String deliveryMessage;
    @Column
    private OrderStatus orderStatus;
    @Column
    private String address;
    @Column
    private Character refundState;
    @Column
    private Integer amount;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "normal_user_id", referencedColumnName = "id")
    private NormalUser normalUser;

    @OneToMany(mappedBy = "productOrder", cascade = CascadeType.ALL)
    private List<Cart> carts;
}
