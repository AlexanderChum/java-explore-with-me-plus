package main.server.compilation.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import main.server.compilation.CompilationMapper;
import main.server.compilation.CompilationRepository;
import main.server.compilation.dto.NewCompilationDto;
import main.server.compilation.dto.CompilationDto;
import main.server.compilation.dto.CompilationUpdateDto;
import main.server.compilation.dto.CompilationsRequest;
import main.server.compilation.model.Compilation;
import main.server.compilation.pagination.PaginationOffset;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import main.server.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<EventModel> events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toDto(savedCompilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Событие с id " + compId + " не найдено");
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, CompilationUpdateDto updateDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + compId + " не найдено"));

        if (updateDto.getTitle() != null) {
            compilation.setTitle(updateDto.getTitle());
        }
        if (updateDto.getPinned() != null) {
            compilation.setPinned(updateDto.getPinned());
        }
        if (updateDto.getEvents() != null) {
            Set<EventModel> events = new HashSet<>(eventRepository.findAllById(updateDto.getEvents()));
            compilation.setEvents(events);
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toDto(savedCompilation);
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + compId + " не найдено"));
        return compilationMapper.toDto(compilation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilations(CompilationsRequest request, PaginationOffset pagination) {
        Predicate predicate = null;
        if (request.getPinned() != null) {
            predicate = qCompilation.pinned.eq(request.getPinned());
        }
        int from = (pagination.getFrom() != null) ? pagination.getFrom() : 0;
        int size = (pagination.getSize() != null) ? pagination.getSize() : 10;
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> page = (predicate != null)
                ? compilationRepository.findAll(predicate, pageable)
                : compilationRepository.findAll(pageable);
        return compilationMapper.toDtoList(page.getContent());
    }
}
