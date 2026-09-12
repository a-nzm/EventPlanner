/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;

import com.events.planner.dto.SubjectDto;
import com.events.planner.entity.Subject;
import com.events.planner.mapper.impl.SubjectDtoEntityMapper;
import com.events.planner.repository.SubjectRepository;
import com.events.planner.service.SubjectService;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author MAU
 */
@Service
public class SubjectServiceImpl implements SubjectService{
    private static final String SUBJECT_NOT_FOUND = "Subject not found.";
    private final SubjectRepository subjectRepository;
    private final SubjectDtoEntityMapper subjectMapper;

    public SubjectServiceImpl(SubjectRepository subjectRepository, SubjectDtoEntityMapper subjectMapper) {
        this.subjectRepository = subjectRepository;
        this.subjectMapper = subjectMapper;
    }

    @Override
    public SubjectDto create(SubjectDto dto){
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException("Subject code is required.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Subject name is required.");
        }

        Optional<Subject> existing = subjectRepository.findByCode(dto.getCode());
        if (existing.isPresent()) {
            throw new IllegalStateException("Subject code already exists.");
        }

        Subject saved = subjectRepository.save(subjectMapper.toEntity(dto));
        return subjectMapper.toDto(saved);
    }

    @Override
    public SubjectDto getById(Long id){
        return subjectRepository.findById(id)
                .map(subjectMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException(SUBJECT_NOT_FOUND));
    }

    @Override
    public Page<SubjectDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return subjectRepository.findAll(pageable).map(subjectMapper::toDto);
    }

    @Override
    public SubjectDto getByCode(String code) {
        return subjectRepository.findByCode(code)
                .map(subjectMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException(SUBJECT_NOT_FOUND));
    }

    @Override
    public Page<SubjectDto> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50));
        return subjectRepository.findByNameContainingIgnoreCase(name, pageable).map(subjectMapper::toDto);
    }

    @Override
    public SubjectDto update(Long id, SubjectDto dto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(SUBJECT_NOT_FOUND));

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException("Subject code is required.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Subject name is required.");
        }

        if (!dto.getCode().equals(subject.getCode())) {
            Optional<Subject> existing = subjectRepository.findByCode(dto.getCode());
            if (existing.isPresent()) {
                throw new IllegalStateException("Subject code already exists.");
            }
        }

        subjectMapper.updateEntity(subject, dto);
        Subject saved = subjectRepository.save(subject);
        return subjectMapper.toDto(saved);
    }

    @Override
    public void delete(Long id){
        if (!subjectRepository.existsById(id)) {
            throw new NoSuchElementException(SUBJECT_NOT_FOUND);
        }
        subjectRepository.deleteById(id);
    }
}
