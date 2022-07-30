package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username) {
        Member member = Member.builder()
                .username(username)
                .build();

        Log logMessage = Log.builder()
                .message(username)
                .build();

        log.info("=== memberRepository 호출 시작 ===");
        memberRepository.save(member);
        log.info("=== memberRepository 호출 종료 ===");

        log.info("=== logRepository 호출 시작 ===");
        logRepository.save(logMessage);
        log.info("=== logRepository 호출 종료 ===");
    }

    @Transactional  // 예외를 서비스 로직에서 처리
    public void joinV2(String username) {
        Member member = Member.builder()
                .username(username)
                .build();

        Log logMessage = Log.builder()
                .message(username)
                .build();

        log.info("=== memberRepository 호출 시작 ===");
        memberRepository.save(member);
        log.info("=== memberRepository 호출 종료 ===");

        log.info("=== logRepository 호출 시작 ===");
        try {  // 예외 발생시, 예외 흐름 복구.
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("log 저장에 실패했습니다. logMessage={}", logMessage.getMessage());
            log.info("정상 흐름 반환");
        }
        log.info("=== logRepository 호출 종료 ===");
    }
}
