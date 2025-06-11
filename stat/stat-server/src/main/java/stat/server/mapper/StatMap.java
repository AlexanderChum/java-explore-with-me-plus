package stat.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import stat.dto.EndpointHitDto;
import stat.dto.StatDto;
import stat.server.model.EndpointHit;

@Mapper(componentModel = "spring")
public interface StatMap {

    @Mapping(target = "id", ignore = true)
    EndpointHit toEndpointHit(StatDto statDto);

    @Mapping(target = "hits", ignore = true)
    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);
}
