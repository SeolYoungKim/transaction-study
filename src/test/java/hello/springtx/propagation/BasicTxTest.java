package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);

        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);

        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.commit(tx2);
    }

    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.rollback(tx2);
    }

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());  // 처음 트랜잭션 수행 -> 신규 트랜잭션. 물리 트랜잭션 시작!

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());  // 이미 외부 트랜잭션 수행 중. -> 내부 트랜잭션이 참여. 신규 트랜잭션이 아님.
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);  // 무시하고 넘어감.

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);  // 물리 트랜잭션 끝
        
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());  // 처음 트랜잭션 수행 -> 신규 트랜잭션. 물리 트랜잭션 시작!

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());  // 이미 외부 트랜잭션 수행 중. -> 내부 트랜잭션이 참여. 신규 트랜잭션이 아님.
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);  // 무시하고 넘어감.

        log.info("외부 트랜잭션 커밋");
        txManager.rollback(outer);  // 물리 트랜잭션 끝

    }

    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());log.info("outer.isNewTransaction()={}", outer.isNewTransaction());  // 처음 트랜잭션 수행 -> 신규 트랜잭션. 물리 트랜잭션 시작!

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);  // rollback-only 표시

        log.info("외부 트랜잭션 커밋");
        // 전체 트랜잭션이 rollback-only 라서 롤백됨. 예외로 내부에서 롤백이 발생했다고 알려줌.

        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());log.info("outer.isNewTransaction()={}", outer.isNewTransaction());  // 처음 트랜잭션 수행 -> 신규 트랜잭션. 물리 트랜잭션 시작!
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());  // true

        log.info("내부 트랜잭션 시작");  // 외부 트랜잭션은 잠깐 미뤄둔다.
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);  // 기존 트랜잭션에 참여하지 않고, 무시하고 새 트랜잭션 생성.

        TransactionStatus inner = txManager.getTransaction(definition);

        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());  // true

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);  // 롤백하고, 기존에 미뤄놨던 외부 트랜잭션을 재시작.

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
    }
}
