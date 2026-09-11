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

/**
 *
 * @author MAU
 */
@Service
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final EventRepository eventRepository;

    private final HallDtoEntityMapper hallMapper;

    public HallServiceImpl(HallRepository hallRepository, HallDtoEntityMapper hallMapper, EventRepository eventRepository) {
        this.hallRepository = hallRepository;
        this.hallMapper = hallMapper;
        this.eventRepository = eventRepository;
    }

    @Override
    public HallDto create(HallDto dto) throws Exception {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new Exception("Hall name is required.");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new Exception("Hall type is required.");
        }
        if (dto.getCapacity() < 0) {
            throw new Exception("Capacity cannot be negative.");
        }

        parseHallType(dto.getType());

        Hall saved = hallRepository.save(hallMapper.toEntity(dto));
        return hallMapper.toDto(saved);
    }

    @Override
    public HallDto getById(Long id) throws Exception {
        return hallRepository.findById(id)
                .map(hallMapper::toDto)
                .orElseThrow(() -> new Exception("Hall not found."));
    }

    @Override
    public Page<HallDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return hallRepository.findAll(pageable).map(hallMapper::toDto);
    }

    @Override
    public Page<HallDto> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return hallRepository.findByNameContainingIgnoreCase(name, pageable).map(hallMapper::toDto);
    }

    @Override
    public Page<HallDto> getByType(String type, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        HallType hallType = parseHallType(type);
        return hallRepository.findByType(hallType, pageable).map(hallMapper::toDto);
    }

    @Override
    public Page<HallDto> getByMinCapacity(int capacity, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return hallRepository.findByCapacityGreaterThanEqual(capacity, pageable).map(hallMapper::toDto);
    }

    @Override
    public HallDto update(Long id, HallDto dto) throws Exception {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new Exception("Hall not found."));

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new Exception("Hall name is required.");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new Exception("Hall type is required.");
        }
        if (dto.getCapacity() < 0) {
            throw new Exception("Capacity cannot be negative.");
        }

        parseHallType(dto.getType());

        hallMapper.updateEntity(hall, dto);
        Hall saved = hallRepository.save(hall);
        return hallMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) throws Exception {
        if (!hallRepository.existsById(id)) {
            throw new Exception("Hall not found.");
        }
        hallRepository.deleteById(id);
    }

    private HallType parseHallType(String type) throws Exception {
        try {
            return HallType.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new Exception("Invalid hall type.");
        }
    }

    @Override
    public List<HallDto> getAvailableHalls(LocalDateTime start, LocalDateTime end, Long eventId) throws Exception {

        if (start == null) {
            throw new Exception("Start time is required.");
        }

        if (end == null) {
            throw new Exception("End time is required.");
        }

        if (!end.isAfter(start)) {
            throw new Exception("End time must be after start time.");
        }

        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(20, 0);

        if (start.toLocalTime().isBefore(openingTime)) {
            throw new Exception("Reservations cannot start before 08:00.");
        }

        if (end.toLocalTime().isAfter(closingTime)) {
            throw new Exception("Reservations must end by 20:00.");
        }

        if (eventId == null) {
            throw new Exception("Event ID is required.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new Exception("Event not found."));

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
