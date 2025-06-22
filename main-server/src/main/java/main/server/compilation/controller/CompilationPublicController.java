package main.server.compilation.controller;

import lombok.RequiredArgsConstructor;
import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.CompilationsRequest;
import main.server.compilation.pagination.PaginationOffset;
import main.server.compilation.service.CompilationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable long compId) {
        return compilationService.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilations(@ModelAttribute @Validated CompilationsRequest compilationsRequest,
                                                @ModelAttribute @Validated PaginationOffset paginationOffset) {
        return compilationService.getCompilations(compilationsRequest, paginationOffset);
    }
}
