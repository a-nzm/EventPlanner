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
import com.events.planner.service.ReservationService;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 *
 * @author MAU
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private static final String RESERVATION_NOT_FOUND = "Reservation not found.";

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final HallRepository hallRepository;
    private final EventRepository eventRepository;
    private final ReservationDtoEntityMapper reservationMapper;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
            UserRepository userRepository,
            HallRepository hallRepository,
            EventRepository eventRepository,
            ReservationDtoEntityMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.hallRepository = hallRepository;
        this.eventRepository = eventRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public ReservationDto create(ReservationDto dto, String email) {
        validateReservation(dto, false);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        Hall hall = hallRepository.findById(dto.getHallId())
                .orElseThrow(() -> new NoSuchElementException("Hall not found."));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NoSuchElementException("Event not found."));

        if (event.getCapacity() > hall.getCapacity()) {
            throw new IllegalArgumentException("Selected event requires more capacity than the chosen hall.");
        }

        Reservation reservation = reservationMapper.toEntity(dto);
        reservation.setUser(user);
        reservation.setHall(hall);
        reservation.setEvent(event);
        reservation.setTimestamp(LocalDateTime.now(ZoneId.of("Europe/Belgrade")));

        reservation.setStatus(ReservationStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toDto(saved);
    }

    @Override
    public ReservationDto getById(Long id){
        return reservationRepository.findById(id)
                .map(reservationMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException(RESERVATION_NOT_FOUND));
    }

    @Override
    public Page<ReservationDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return reservationRepository.findAll(pageable).map(reservationMapper::toDto);
    }

    @Override
    public Page<ReservationDto> getByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return reservationRepository.findByUserId(userId, pageable).map(reservationMapper::toDto);
    }

    @Override
    public Page<ReservationDto> getByHallId(Long hallId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return reservationRepository.findByHallId(hallId, pageable).map(reservationMapper::toDto);
    }

    @Override
    public Page<ReservationDto> getByEventId(Long eventId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return reservationRepository.findByEventId(eventId, pageable).map(reservationMapper::toDto);
    }

    @Override
    public Page<ReservationDto> getByStatus(String status, int page, int size)  {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        ReservationStatus reservationStatus = parseReservationStatus(status);
        return reservationRepository.findByStatus(reservationStatus, pageable)
                .map(reservationMapper::toDto);
    }

    @Override
    public ReservationDto update(Long id, ReservationDto dto, Authentication authentication)  {
        validateReservation(dto, true);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(RESERVATION_NOT_FOUND));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        Hall hall = hallRepository.findById(dto.getHallId())
                .orElseThrow(() -> new NoSuchElementException("Hall not found."));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NoSuchElementException("Event not found."));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String email = authentication.getName();

        if (!isAdmin) {

            if (!reservation.getUser().getEmail().equals(email)) {
                throw new AccessDeniedException("You can only edit your own reservations.");
            }

            if (reservation.getStatus() != ReservationStatus.PENDING) {
                throw new IllegalStateException("Only PENDING reservations can be edited.");
            }
        }

        if (event.getCapacity() > hall.getCapacity()) {
            throw new IllegalArgumentException("Selected event requires more capacity than the chosen hall.");
        }

        if (reservation.getStatus() == ReservationStatus.APPROVED) {
            boolean conflict = reservationRepository.existsHallReservationConflict(
                    hall.getId(),
                    dto.getStart(),
                    dto.getEnd(),
                    ReservationStatus.APPROVED,
                    id
            );

            if (conflict) {
                throw new IllegalStateException("Hall is already reserved in that time period by an approved reservation.");
            }
        }

        LocalDateTime oldTimestamp = reservation.getTimestamp();
        ReservationStatus oldStatus = reservation.getStatus();

        reservationMapper.updateEntity(reservation, dto);

        reservation.setTimestamp(oldTimestamp);
        reservation.setStatus(oldStatus);
        reservation.setUser(user);
        reservation.setHall(hall);
        reservation.setEvent(event);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toDto(saved);
    }

    @Override
    public ReservationDto updateStatus(Long id, String status, Authentication authentication){
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(RESERVATION_NOT_FOUND));

        ReservationStatus newStatus = parseReservationStatus(status);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String email = authentication.getName();

        if (!isAdmin) {
            if (!reservation.getUser().getEmail().equals(email)) {
                throw new AccessDeniedException("You can only change status of your own reservations.");
            }

            if (newStatus != ReservationStatus.CANCELLED) {
                throw new AccessDeniedException("Users can only cancel their own reservations.");
            }

            if (reservation.getStatus() != ReservationStatus.PENDING
                    && reservation.getStatus() != ReservationStatus.APPROVED) {
                throw new IllegalStateException("Only PENDING or APPROVED reservations can be cancelled.");
            }
        }

        if (newStatus == ReservationStatus.APPROVED) {
            boolean conflict = reservationRepository.existsHallReservationConflict(
                    reservation.getHall().getId(),
                    reservation.getStart(),
                    reservation.getEnd(),
                    ReservationStatus.APPROVED,
                    reservation.getId()
            );

            if (conflict) {
                throw new IllegalStateException("Cannot approve reservation. Hall is already reserved in that time period.");
            }
        }

        reservation.setStatus(newStatus);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toDto(saved);
    }

    @Override
    public void delete(Long id){
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException(RESERVATION_NOT_FOUND);
        }
        reservationRepository.deleteById(id);
    }

    private void validateReservation(ReservationDto dto, boolean requireUserId){
        if (dto.getStart() == null) {
           throw new IllegalArgumentException("Start time is required.");
        }
        if (dto.getEnd() == null) {
            throw new IllegalArgumentException("End time is required.");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(20, 0);
        if (dto.getStart().toLocalTime().isBefore(openingTime)) {
            throw new IllegalArgumentException("Reservations cannot start before 08:00.");
        }
        if (dto.getEnd().toLocalTime().isAfter(closingTime)) {
            throw new IllegalArgumentException("Reservations must end by 20:00.");
        }
        if (requireUserId && dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (dto.getHallId() == null) {
            throw new IllegalArgumentException("Hall ID is required.");
        }
        if (dto.getEventId() == null) {
            throw new IllegalArgumentException("Event ID is required.");
        }
    }

    @Override
    public Page<ReservationDto> getFiltered(int page, int size, String status, Long userId, Long hallId, Long eventId,
            String sortBy, String sortDir) {
        switch (sortBy) {
            case "user":
                sortBy = "user.name";
                break;
            case "hall":
                sortBy = "hall.name";
                break;
            case "event":
                sortBy = "event.name";
                break;
            case "created":
                sortBy = "timestamp";
                break;
            default:
                break;
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Reservation> spec = (root, query, cb) -> cb.conjunction();

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("status"), ReservationStatus.valueOf(status)));
        }

        if (userId != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("user").get("id"), userId));
        }

        if (hallId != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("hall").get("id"), hallId));
        }

        if (eventId != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("event").get("id"), eventId));
        }

        return reservationRepository.findAll(spec, pageable)
                .map(reservationMapper::toDto);
    }

  private ReservationStatus parseReservationStatus(String status) {
    if (status == null || status.isBlank()) {
        throw new IllegalArgumentException("Reservation status is required.");
    }

    try {
        return ReservationStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid reservation status.", e);
    }
}

}
