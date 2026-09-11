/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service;

import com.events.planner.dto.HallDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 *
 * @author MAU
 */
public interface HallService {

    HallDto create(HallDto dto) throws Exception;

    HallDto getById(Long id) throws Exception;

    Page<HallDto> getAll(int page, int size);

    Page<HallDto> searchByName(String name, int page, int size);

    Page<HallDto> getByType(String type, int page, int size) throws Exception;

    Page<HallDto> getByMinCapacity(int capacity, int page, int size);

    HallDto update(Long id, HallDto dto) throws Exception;

    void delete(Long id) throws Exception;
    
    List<HallDto> getAvailableHalls(LocalDateTime start,LocalDateTime end, Long eventId) throws Exception;
}
