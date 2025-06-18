package stat.server.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import stat.dto.EndpointHitDto;
import stat.server.mapper.StatMap;
import stat.server.repository.StatRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatServiceImplTest {

    @Autowired
    StatServiceImpl statService;
    @Autowired
    StatRepository statRepository;
    @Autowired
    StatMap statMap;

    @Test
    void testSaveHit() {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.parse("2022-09-06T11:00:23"))
                .build();

        EndpointHitDto result = statRepository.save(statMap.toEndpointHit(endpointHitDto));

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("ewm-main-service", result.getApp()),
                () -> assertEquals("/events/1", result.getUri()),
                () -> assertEquals("192.163.0.1", result.getIp()),
                () -> assertEquals(LocalDateTime.parse("2022-09-06T11:00:23"), result.getTimestamp())
        );
    }

    @Test
    void getStats() {
    }
}