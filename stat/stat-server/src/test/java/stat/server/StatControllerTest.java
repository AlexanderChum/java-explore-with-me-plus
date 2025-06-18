package stat.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;
import stat.server.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class StatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    StatService statService;

    @Test
    void testSaveHit() throws Exception {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.parse("2022-09-06T11:00:23"))
                .build();

        when(statService.saveHit(any(EndpointHitDto.class))).thenReturn(endpointHitDto);

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(endpointHitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.app").value("ewm-main-service"))
                .andExpect(jsonPath("$.uri").value("/events/1"))
                .andExpect(jsonPath("$.ip").value("192.163.0.1"))
                .andExpect(jsonPath("$.timestamp").value("2022-09-06 11:00:23"));
    }

    @Test
    public void testGetStats() throws Exception {
        ViewStatsDto viewStatsDto = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(1L)
                .build();
        List<ViewStatsDto> statsList = List.of(viewStatsDto);

        when(statService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), anyBoolean())).thenReturn(statsList);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        mockMvc.perform(get("/stats")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("uris", "/events/1", "/events/2")
                        .param("unique", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$.[0].uri").value("/events/1"))
                .andExpect(jsonPath("$.[0].hits").value(1));
    }
}