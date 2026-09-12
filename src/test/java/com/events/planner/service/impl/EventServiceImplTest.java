/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;

import com.events.planner.dto.EventDto;
import com.events.planner.entity.Event;
import com.events.planner.entity.EventType;
import com.events.planner.entity.Subject;
import com.events.planner.mapper.impl.EventDtoEntityMapper;
import com.events.planner.repository.EventRepository;
import com.events.planner.repository.SubjectRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author MAU
 */
@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {
    
    @Mock
    private EventRepository eventRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EventDtoEntityMapper eventMapper;

    private EventServiceImpl eventService;

    private EventType validType;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(
                eventRepository,
                subjectRepository,
                eventMapper
        );

        validType = EventType.values()[0];
    }

    @Test
    void create_shouldCreateEvent() {
        EventDto dto = createDto();
        Event entity = createEntity();
        Event saved = createEntity();
        EventDto resultDto = createDto();

        Subject subject = new Subject();
        subject.setId(1L);

        when(eventMapper.toEntity(dto))
                .thenReturn(entity);

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.of(subject));

        when(eventRepository.save(entity))
                .thenReturn(saved);

        when(eventMapper.toDto(saved))
                .thenReturn(resultDto);

        EventDto result = eventService.create(dto);

        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        assertEquals(50, result.getCapacity());

        verify(eventRepository).save(entity);
    }

    @Test
    void create_shouldThrowWhenNameMissing() {
        EventDto dto = createDto();
        dto.setName(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> eventService.create(dto)
                );

        assertEquals(
                "Event name is required.",
                exception.getMessage()
        );

        verify(eventRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenTypeMissing() {
        EventDto dto = createDto();
        dto.setType(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> eventService.create(dto)
                );

        assertEquals(
                "Event type is required.",
                exception.getMessage()
        );
    }

    @Test
    void create_shouldThrowWhenCapacityNegative() {
        EventDto dto = createDto();
        dto.setCapacity(-1);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> eventService.create(dto)
                );

        assertEquals(
                "Capacity cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    void create_shouldThrowWhenSubjectNotFound() {
        EventDto dto = createDto();
        Event entity = createEntity();

        when(eventMapper.toEntity(dto))
                .thenReturn(entity);

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> eventService.create(dto)
                );

        assertEquals(
                "Subject not found.",
                exception.getMessage()
        );
    }

    @Test
    void getById_shouldReturnEvent() {
        Event event = createEntity();
        EventDto dto = createDto();

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(eventMapper.toDto(event))
                .thenReturn(dto);

        EventDto result = eventService.getById(1L);

        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        assertEquals(50, result.getCapacity());
    }

    @Test
    void getById_shouldThrowWhenEventNotFound() {
        when(eventRepository.findById(99L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> eventService.getById(99L)
                );

        assertEquals(
                "Event not found.",
                exception.getMessage()
        );
    }

    @Test
    void getByType_shouldThrowWhenTypeInvalid() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> eventService.getByType(
                                "INVALID_TYPE",
                                0,
                                10
                        )
                );

        assertTrue(
                exception.getMessage().contains("Invalid event type")
        );
    }

    @Test
    void update_shouldThrowWhenSubjectNotFound() {
        Event existing = createEntity();
        EventDto dto = createDto();

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> eventService.update(1L, dto)
                );

        assertEquals(
                "Subject not found.",
                exception.getMessage()
        );
    }

    @Test
    void delete_shouldThrowWhenEventNotFound() {
        when(eventRepository.existsById(99L))
                .thenReturn(false);

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> eventService.delete(99L)
                );

        assertEquals(
                "Event not found.",
                exception.getMessage()
        );

        verify(eventRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void delete_shouldDeleteEvent() {
        when(eventRepository.existsById(1L))
                .thenReturn(true);

        eventService.delete(1L);

        verify(eventRepository).deleteById(1L);
    }

    private EventDto createDto() {
        EventDto dto = new EventDto();

        dto.setId(1L);
        dto.setName("Test Event");
        dto.setType(validType.name());
        dto.setDescription("Test description");
        dto.setCapacity(50);
        dto.setSubjectId(1L);

        return dto;
    }

    private Event createEntity() {
        Event event = new Event();

        event.setId(1L);
        event.setName("Test Event");
        event.setType(validType);
        event.setDescription("Test description");
        event.setCapacity(50);

        return event;
    }
}
