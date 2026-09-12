/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service;

import com.events.planner.dto.EventDto;
import org.springframework.data.domain.Page;

/**
 *
 * @author MAU
 */
public interface EventService {
    
    EventDto create(EventDto dto);

    EventDto getById(Long id);

    Page<EventDto> getAll(int page, int size);

    Page<EventDto> getByType(String type, int page, int size);

    Page<EventDto> searchByName(String name, int page, int size);

    Page<EventDto> getBySubjectId(Long subjectId, int page, int size);

    EventDto update(Long id, EventDto dto);

    void delete(Long id);
}
