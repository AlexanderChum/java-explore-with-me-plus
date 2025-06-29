package main.server.compilation.service;


import main.server.compilation.dto.NewCompilationDto;
import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.CompilationUpdateDto;
import main.server.compilation.dto.CompilationsRequest;
import main.server.compilation.pagination.PaginationOffset;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, CompilationUpdateDto updateDto);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilations(CompilationsRequest compilationsRequest, PaginationOffset paginationOffset);
}
