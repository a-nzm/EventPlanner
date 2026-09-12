/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service;

import com.events.planner.dto.SubjectDto;
import org.springframework.data.domain.Page;

/**
 *
 * @author MAU
 */
public interface SubjectService {

    SubjectDto create(SubjectDto dto);

    SubjectDto getById(Long id);

    Page<SubjectDto> getAll(int page, int size);

    SubjectDto getByCode(String code);

    Page<SubjectDto> searchByName(String name, int page, int size);

    SubjectDto update(Long id, SubjectDto dto);

    void delete(Long id);
}
