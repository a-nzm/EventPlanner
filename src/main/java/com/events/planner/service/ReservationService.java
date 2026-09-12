/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service;

import com.events.planner.dto.ReservationDto;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

/**
 *
 * @author MAU
 */
public interface ReservationService {

    ReservationDto create(ReservationDto dto, String email);

    ReservationDto getById(Long id);

    Page<ReservationDto> getAll(int page, int size);

    Page<ReservationDto> getByUserId(Long userId, int page, int size);

    Page<ReservationDto> getByHallId(Long hallId, int page, int size);

    Page<ReservationDto> getByEventId(Long eventId, int page, int size);

    ReservationDto update(Long id, ReservationDto dto, Authentication authentication);

    ReservationDto updateStatus(Long id, String status, Authentication authentication);

    Page<ReservationDto> getByStatus(String status, int page, int size);
    
    Page<ReservationDto> getFiltered(int page, int size, String status, Long userId, Long hallId, Long eventId, String sortBy, String sortDir);

    void delete(Long id);
}
