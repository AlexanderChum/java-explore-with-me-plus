package main.server.compilation.controller;

import lombok.RequiredArgsConstructor;
import main.server.compilation.dto.NewCompilationDto;
import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.CompilationUpdateDto;
import main.server.compilation.service.CompilationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return compilationService.addCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable long compId,
                                            @RequestBody @Valid CompilationUpdateDto compilationUpdateDto) {
        return compilationService.updateCompilation(compId, compilationUpdateDto);
    }
}
