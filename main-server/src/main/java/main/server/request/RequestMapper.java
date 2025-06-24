package main.server.request;

import main.server.request.dto.ParticipationRequestDto;
import main.server.request.model.ParticipationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "requester.id", target = "requesterId")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);
}
