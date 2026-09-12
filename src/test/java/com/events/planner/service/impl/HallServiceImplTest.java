/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;

import com.events.planner.dto.HallDto;
import com.events.planner.entity.Event;
import com.events.planner.entity.Hall;
import com.events.planner.entity.ReservationStatus;
import com.events.planner.mapper.impl.HallDtoEntityMapper;
import com.events.planner.repository.EventRepository;
import com.events.planner.repository.HallRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author xenia
 */
@ExtendWith(MockitoExtension.class)
public class HallServiceImplTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private HallDtoEntityMapper hallMapper;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private HallServiceImpl hallService;

    @Test
    void getAvailableHalls_shouldReturnAvailableHalls() throws Exception {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 12, 0);

        Event event = mock(Event.class);

        Hall hall1 = mock(Hall.class);
        Hall hall2 = mock(Hall.class);

        HallDto dto1 = mock(HallDto.class);
        HallDto dto2 = mock(HallDto.class);

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(event.getCapacity())
                .thenReturn(40);

        when(hallRepository.findAvailableHalls(
                start,
                end,
                ReservationStatus.APPROVED,
                40
        )).thenReturn(List.of(hall1, hall2));

        when(hallMapper.toDto(hall1))
                .thenReturn(dto1);

        when(hallMapper.toDto(hall2))
                .thenReturn(dto2);

        List<HallDto> result
                = hallService.getAvailableHalls(
                        start,
                        end,
                        3L
                );

        assertEquals(2, result.size());

        assertSame(dto1, result.get(0));
        assertSame(dto2, result.get(1));

        verify(eventRepository).findById(3L);

        verify(hallRepository).findAvailableHalls(
                start,
                end,
                ReservationStatus.APPROVED,
                40
        );

        verify(hallMapper).toDto(hall1);
        verify(hallMapper).toDto(hall2);
    }

    @Test
    void getAvailableHalls_shouldUseEventCapacity() throws Exception {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 12, 0);

        Event event = mock(Event.class);

        when(eventRepository.findById(5L))
                .thenReturn(Optional.of(event));

        when(event.getCapacity())
                .thenReturn(150);

        when(hallRepository.findAvailableHalls(
                start,
                end,
                ReservationStatus.APPROVED,
                150
        )).thenReturn(List.of());

        List<HallDto> result
                = hallService.getAvailableHalls(
                        start,
                        end,
                        5L
                );

        assertTrue(result.isEmpty());

        verify(hallRepository).findAvailableHalls(
                start,
                end,
                ReservationStatus.APPROVED,
                150
        );
    }

    @Test
    void getAvailableHalls_shouldReturnEmptyListWhenNoHallIsAvailable()
            throws Exception {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 12, 0);

        Event event = mock(Event.class);

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(event.getCapacity())
                .thenReturn(40);

        when(hallRepository.findAvailableHalls(
                start,
                end,
                ReservationStatus.APPROVED,
                40
        )).thenReturn(List.of());

        List<HallDto> result
                = hallService.getAvailableHalls(
                        start,
                        end,
                        1L
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableHalls_shouldThrowExceptionWhenEventDoesNotExist() {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 12, 0);

        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.getAvailableHalls(
                        start,
                        end,
                        999L
                )
        );

        assertEquals(
                "Event not found.",
                exception.getMessage()
        );

        verify(hallRepository, never())
                .findAvailableHalls(
                        any(),
                        any(),
                        any(),
                        anyInt()
                );
    }

    @Test
    void getAvailableHalls_shouldThrowExceptionWhenEndIsBeforeStart() {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 14, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 12, 0);

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.getAvailableHalls(
                        start,
                        end,
                        1L
                )
        );

        assertEquals(
                "End time must be after start time.",
                exception.getMessage()
        );

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(hallRepository);
    }

    @Test
    void getAvailableHalls_shouldRejectTimeBeforeOpening() {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 7, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 10, 0);

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.getAvailableHalls(
                        start,
                        end,
                        1L
                )
        );

        assertEquals(
                "Reservations cannot start before 08:00.",
                exception.getMessage()
        );

        verifyNoInteractions(eventRepository);
    }

    @Test
    void getAvailableHalls_shouldRejectTimeAfterClosing() {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 18, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 21, 0);

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.getAvailableHalls(
                        start,
                        end,
                        1L
                )
        );

        assertEquals(
                "Reservations must end by 20:00.",
                exception.getMessage()
        );

        verifyNoInteractions(eventRepository);
    }

    @Test
    void getAvailableHalls_shouldThrowExceptionWhenEventIdIsMissing() {

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 15, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 15, 12, 0);

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.getAvailableHalls(
                        start,
                        end,
                        null
                )
        );

        assertEquals(
                "Event ID is required.",
                exception.getMessage()
        );

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(hallRepository);
    }

    @Test
    void getById_shouldReturnHallWhenHallExists()
            throws Exception {

        Hall hall = mock(Hall.class);
        HallDto dto = mock(HallDto.class);

        when(hallRepository.findById(1L))
                .thenReturn(Optional.of(hall));

        when(hallMapper.toDto(hall))
                .thenReturn(dto);

        HallDto result
                = hallService.getById(1L);

        assertSame(dto, result);

        verify(hallRepository).findById(1L);
        verify(hallMapper).toDto(hall);
    }

    @Test
    void getById_shouldThrowExceptionWhenHallDoesNotExist() {

        when(hallRepository.findById(999L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.getById(999L)
        );

        assertEquals(
                "Hall not found.",
                exception.getMessage()
        );

        verifyNoInteractions(hallMapper);
    }

    @Test
    void delete_shouldDeleteHallWhenHallExists()
            throws Exception {

        when(hallRepository.existsById(1L))
                .thenReturn(true);

        hallService.delete(1L);

        verify(hallRepository).existsById(1L);
        verify(hallRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowExceptionWhenHallDoesNotExist() {

        when(hallRepository.existsById(999L))
                .thenReturn(false);

        Exception exception = assertThrows(
                Exception.class,
                () -> hallService.delete(999L)
        );

        assertEquals(
                "Hall not found.",
                exception.getMessage()
        );

        verify(hallRepository, never())
                .deleteById(anyLong());
    }
}
