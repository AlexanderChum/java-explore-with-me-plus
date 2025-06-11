package stat.server.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import stat.dto.EndpointHitDto;
import stat.dto.StatDto;
import stat.server.model.EndpointHit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatMapper {

    public static EndpointHit toEndpointHit(StatDto statDto) {
        return EndpointHit.builder()
                .app(statDto.getApp())
                .uri(statDto.getUri())
                .ip(statDto.getIp())
                .timestamp(statDto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .build();
    }
}