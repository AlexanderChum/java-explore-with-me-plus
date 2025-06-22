package main.server.compilation;

import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.NewCompilationDto;
import main.server.compilation.model.Compilation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto dto);

    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDtoList(List<Compilation> compilations);
}