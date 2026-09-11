/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;

import com.events.planner.dto.ReservationDto;
import com.events.planner.entity.Event;
import com.events.planner.entity.Hall;
import com.events.planner.entity.Reservation;
import com.events.planner.entity.ReservationStatus;
import com.events.planner.entity.User;
import com.events.planner.mapper.impl.ReservationDtoEntityMapper;
import com.events.planner.repository.EventRepository;
import com.events.planner.repository.HallRepository;
import com.events.planner.repository.ReservationRepository;
import com.events.planner.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author xenia
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationDtoEntityMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void getById_shouldReturnReservationWhenReservationExists() throws Exception {

        Reservation reservation = mock(Reservation.class);
        ReservationDto dto = mock(ReservationDto.class);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationMapper.toDto(reservation))
                .thenReturn(dto);

        ReservationDto result = reservationService.getById(1L);

        assertSame(dto, result);

        verify(reservationRepository).findById(1L);
        verify(reservationMapper).toDto(reservation);
    }

    @Test
    void getById_shouldThrowExceptionWhenReservationDoesNotExist() {

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.getById(10L)
        );

        assertEquals("Reservation not found.", exception.getMessage());

        verify(reservationRepository).findById(10L);
        verifyNoInteractions(reservationMapper);
    }

    @Test
    void delete_shouldDeleteReservationWhenReservationExists() throws Exception {

        when(reservationRepository.existsById(1L))
                .thenReturn(true);

        reservationService.delete(1L);

        verify(reservationRepository).existsById(1L);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowExceptionWhenReservationDoesNotExist() {

        when(reservationRepository.existsById(999L))
                .thenReturn(false);

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.delete(999L)
        );

        assertEquals(
                "Reservation not found.",
                exception.getMessage()
        );

        verify(reservationRepository).existsById(999L);

        verify(reservationRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void create_shouldCreateReservationSuccessfully()
            throws Exception {

        ReservationDto dto = mock(ReservationDto.class);

        User user = mock(User.class);
        Hall hall = mock(Hall.class);
        Event event = mock(Event.class);
        Reservation reservation = mock(Reservation.class);
        ReservationDto resultDto = mock(ReservationDto.class);

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 12, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 12, 12, 0);

        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);
        when(dto.getHallId()).thenReturn(1L);
        when(dto.getEventId()).thenReturn(2L);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(hallRepository.findById(1L))
                .thenReturn(Optional.of(hall));

        when(eventRepository.findById(2L))
                .thenReturn(Optional.of(event));

        when(hall.getCapacity()).thenReturn(100);
        when(event.getCapacity()).thenReturn(50);

        when(reservationMapper.toEntity(dto))
                .thenReturn(reservation);

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        when(reservationMapper.toDto(reservation))
                .thenReturn(resultDto);

        ReservationDto result
                = reservationService.create(
                        dto,
                        "user@test.com"
                );

        assertSame(resultDto, result);

        verify(reservation).setUser(user);
        verify(reservation).setHall(hall);
        verify(reservation).setEvent(event);

        verify(reservation)
                .setStatus(ReservationStatus.PENDING);

        verify(reservationRepository)
                .save(reservation);
    }

    @Test
    void create_shouldThrowExceptionWhenHallCapacityIsTooSmall() {

        ReservationDto dto = mock(ReservationDto.class);

        User user = mock(User.class);
        Hall hall = mock(Hall.class);
        Event event = mock(Event.class);

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 12, 10, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 12, 12, 0);

        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);
        when(dto.getHallId()).thenReturn(1L);
        when(dto.getEventId()).thenReturn(2L);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(hallRepository.findById(1L))
                .thenReturn(Optional.of(hall));

        when(eventRepository.findById(2L))
                .thenReturn(Optional.of(event));

        when(hall.getCapacity()).thenReturn(30);
        when(event.getCapacity()).thenReturn(100);

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.create(
                        dto,
                        "user@test.com"
                )
        );

        assertEquals(
                "Selected event requires more capacity than the chosen hall.",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void create_shouldRejectReservationWhenEndIsBeforeStart() {

        ReservationDto dto = mock(ReservationDto.class);

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 12, 14, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 12, 12, 0);

        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.create(
                        dto,
                        "user@test.com"
                )
        );

        assertEquals(
                "End time must be after start time.",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                hallRepository,
                eventRepository
        );
    }

    @Test
    void create_shouldRejectReservationBeforeOpeningTime() {

        ReservationDto dto = mock(ReservationDto.class);

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 12, 7, 30);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 12, 10, 0);

        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.create(
                        dto,
                        "user@test.com"
                )
        );

        assertEquals(
                "Reservations cannot start before 08:00.",
                exception.getMessage()
        );
    }

    @Test
    void create_shouldRejectReservationAfterClosingTime() {

        ReservationDto dto = mock(ReservationDto.class);

        LocalDateTime start
                = LocalDateTime.of(2026, 9, 12, 18, 0);

        LocalDateTime end
                = LocalDateTime.of(2026, 9, 12, 21, 0);

        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.create(
                        dto,
                        "user@test.com"
                )
        );

        assertEquals(
                "Reservations must end by 20:00.",
                exception.getMessage()
        );
    }

    @Test
    void updateStatus_adminShouldApproveReservation()
            throws Exception {

        Reservation reservation = mock(Reservation.class);
        ReservationDto dto = mock(ReservationDto.class);
        Hall hall = mock(Hall.class);

        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getHall())
                .thenReturn(hall);

        when(hall.getId())
                .thenReturn(2L);

        when(reservation.getId())
                .thenReturn(1L);

        when(reservation.getStart())
                .thenReturn(
                        LocalDateTime.of(
                                2026, 9, 12, 10, 0
                        )
                );

        when(reservation.getEnd())
                .thenReturn(
                        LocalDateTime.of(
                                2026, 9, 12, 12, 0
                        )
                );

        when(reservationRepository
                .existsHallReservationConflict(
                        anyLong(),
                        any(),
                        any(),
                        eq(ReservationStatus.APPROVED),
                        anyLong()
                ))
                .thenReturn(false);

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        when(reservationMapper.toDto(reservation))
                .thenReturn(dto);

        ReservationDto result
                = reservationService.updateStatus(
                        1L,
                        "APPROVED",
                        authentication
                );

        assertSame(dto, result);

        verify(reservation)
                .setStatus(ReservationStatus.APPROVED);

        verify(reservationRepository)
                .save(reservation);
    }

    @Test
    void updateStatus_adminShouldNotApproveConflictingReservation() {

        Reservation reservation = mock(Reservation.class);
        Hall hall = mock(Hall.class);

        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getHall())
                .thenReturn(hall);

        when(hall.getId())
                .thenReturn(2L);

        when(reservation.getId())
                .thenReturn(1L);

        when(reservation.getStart())
                .thenReturn(
                        LocalDateTime.of(
                                2026, 9, 12, 10, 0
                        )
                );

        when(reservation.getEnd())
                .thenReturn(
                        LocalDateTime.of(
                                2026, 9, 12, 12, 0
                        )
                );

        when(reservationRepository
                .existsHallReservationConflict(
                        anyLong(),
                        any(),
                        any(),
                        eq(ReservationStatus.APPROVED),
                        anyLong()
                ))
                .thenReturn(true);

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.updateStatus(
                        1L,
                        "APPROVED",
                        authentication
                )
        );

        assertEquals(
                "Cannot approve reservation. Hall is already reserved in that time period.",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void updateStatus_userShouldCancelOwnReservation()
            throws Exception {

        Reservation reservation = mock(Reservation.class);
        ReservationDto dto = mock(ReservationDto.class);
        User user = mock(User.class);

        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getUser())
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn("user@test.com");

        when(reservation.getStatus())
                .thenReturn(ReservationStatus.PENDING);

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        when(reservationMapper.toDto(reservation))
                .thenReturn(dto);

        ReservationDto result
                = reservationService.updateStatus(
                        1L,
                        "CANCELLED",
                        authentication
                );

        assertSame(dto, result);

        verify(reservation)
                .setStatus(ReservationStatus.CANCELLED);

        verify(reservationRepository)
                .save(reservation);
    }

    @Test
    void updateStatus_userShouldNotApproveReservation() {

        Reservation reservation = mock(Reservation.class);
        User user = mock(User.class);

        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getUser())
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn("user@test.com");

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.updateStatus(
                        1L,
                        "APPROVED",
                        authentication
                )
        );

        assertEquals(
                "Users can only cancel their own reservations.",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void updateStatus_userShouldNotChangeAnotherUsersReservation() {

        Reservation reservation = mock(Reservation.class);
        User owner = mock(User.class);

        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservation.getUser())
                .thenReturn(owner);

        when(owner.getEmail())
                .thenReturn("other@test.com");

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.updateStatus(
                        1L,
                        "CANCELLED",
                        authentication
                )
        );

        assertEquals(
                "You can only change status of your own reservations.",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void updateStatus_shouldRejectInvalidStatus() {

        Reservation reservation = mock(Reservation.class);

        Authentication authentication
                = mock(Authentication.class);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(
                Exception.class,
                () -> reservationService.updateStatus(
                        1L,
                        "SOMETHING_RANDOM",
                        authentication
                )
        );

        assertEquals(
                "Invalid reservation status.",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any());
    }
}
