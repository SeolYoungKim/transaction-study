package hello.springtx.order;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {  // DB는 orderBy 라는 문법이 있고, 예약어기 떄문에 사용못하는 경우가 많다.

    @Id
    @GeneratedValue
    private Long id;

    private String username;  // 정상, 예외, 잔고부족
    private String payStatus; // 대기, 완료
}
