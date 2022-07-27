package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @Test
    void externalCall() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {
        public void external() {
            log.info("call external");
            printTxInfo();
            internal();  // 내부 로직에서 트랜잭션이 걸린 메서드 수행. -> 트랜잭션이 안걸린다..?
            // 얘는 this.internal()입니다. -> 나 자신의 인스턴스를 가리킵니다.
        }

        @Transactional  // 메서드에 @Transactional이 걸려있어도, 스프링은 클래스 자체를 프록시로 만든다.
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        public void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
//            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
//            log.info("tx readOnly={}", readOnly);
        }
    }




}
