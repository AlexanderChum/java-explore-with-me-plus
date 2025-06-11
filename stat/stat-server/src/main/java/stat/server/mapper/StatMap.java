package stat.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import stat.dto.EndpointHitDto;
import stat.server.model.EndpointHit;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring")
public interface StatMap {

    @Mapping(target = "id", ignore = true)
    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

    @Mapping(target = "hits", ignore = true)
    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);
}
