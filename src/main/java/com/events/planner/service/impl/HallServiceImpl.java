/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;

import com.events.planner.dto.HallDto;
import com.events.planner.entity.Event;
import com.events.planner.entity.Hall;
import com.events.planner.entity.HallType;
import com.events.planner.entity.ReservationStatus;
import com.events.planner.mapper.impl.HallDtoEntityMapper;
import com.events.planner.repository.EventRepository;
import com.events.planner.repository.HallRepository;
import com.events.planner.service.HallService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
/**
 *
 * @author MAU
 */
@Service
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final EventRepository eventRepository;

    private final HallDtoEntityMapper hallMapper;
    private static final String HALL_NOT_FOUND = "Hall not found.";

    public HallServiceImpl(HallRepository hallRepository, HallDtoEntityMapper hallMapper, EventRepository eventRepository) {
        this.hallRepository = hallRepository;
        this.hallMapper = hallMapper;
        this.eventRepository = eventRepository;
    }

    @Override
    public HallDto create(HallDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Hall name is required.");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new IllegalArgumentException("Hall type is required.");
        }
        if (dto.getCapacity() < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }

        parseHallType(dto.getType());

        Hall saved = hallRepository.save(hallMapper.toEntity(dto));
        return hallMapper.toDto(saved);
    }

    @Override
    public HallDto getById(Long id) {
        return hallRepository.findById(id)
                .map(hallMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException(HALL_NOT_FOUND));
    }

    @Override
    public Page<HallDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return hallRepository.findAll(pageable).map(hallMapper::toDto);
    }

    @Override
    public Page<HallDto> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return hallRepository.findByNameContainingIgnoreCase(name, pageable).map(hallMapper::toDto);
    }

    @Override
    public Page<HallDto> getByType(String type, int page, int size){
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        HallType hallType = parseHallType(type);
        return hallRepository.findByType(hallType, pageable).map(hallMapper::toDto);
    }

    @Override
    public Page<HallDto> getByMinCapacity(int capacity, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return hallRepository.findByCapacityGreaterThanEqual(capacity, pageable).map(hallMapper::toDto);
    }

    @Override
    public HallDto update(Long id, HallDto dto) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(HALL_NOT_FOUND));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Hall name is required.");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new IllegalArgumentException("Hall type is required.");
        }
        if (dto.getCapacity() < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }

        parseHallType(dto.getType());

        hallMapper.updateEntity(hall, dto);
        Hall saved = hallRepository.save(hall);
        return hallMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!hallRepository.existsById(id)) {
            throw new NoSuchElementException(HALL_NOT_FOUND);
        }
        hallRepository.deleteById(id);
    }

   private HallType parseHallType(String type) {
    if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("Hall type is required.");
    }

    try {
        return HallType.valueOf(type.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid hall type.", e);
    }
}

    @Override
    public List<HallDto> getAvailableHalls(LocalDateTime start, LocalDateTime end, Long eventId) {

        if (start == null) {
            throw new IllegalArgumentException("Start time is required.");
        }

        if (end == null) {
            throw new IllegalArgumentException("End time is required.");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(20, 0);

        if (start.toLocalTime().isBefore(openingTime)) {
            throw new IllegalArgumentException("Reservations cannot start before 08:00.");
        }

        if (end.toLocalTime().isAfter(closingTime)) {
            throw new IllegalArgumentException("Reservations must end by 20:00.");
        }

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID is required.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found."));

        int requiredCapacity = event.getCapacity();

        return hallRepository.findAvailableHalls(
                start,
                end,
                ReservationStatus.APPROVED,
                requiredCapacity
        ).stream()
                .map(hallMapper::toDto)
                .toList();
    }
}
