/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.service.impl;

/**
 *
 * @author MAU
 */
import com.events.planner.dto.SubjectDto;
import com.events.planner.entity.Subject;
import com.events.planner.mapper.impl.SubjectDtoEntityMapper;
import com.events.planner.repository.SubjectRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceImplTest {
    
    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectDtoEntityMapper subjectMapper;

    private SubjectServiceImpl subjectService;

    @BeforeEach
    void setUp() {
        subjectService = new SubjectServiceImpl(
                subjectRepository,
                subjectMapper
        );
    }

    @Test
    void create_shouldCreateSubject() {
        SubjectDto dto = new SubjectDto(1L, "MAT101", "Mathematics");
        Subject entity = new Subject(1L, "MAT101", "Mathematics");
        Subject saved = new Subject(1L, "MAT101", "Mathematics");
        SubjectDto resultDto = new SubjectDto(1L, "MAT101", "Mathematics");

        when(subjectRepository.findByCode("MAT101"))
                .thenReturn(Optional.empty());

        when(subjectMapper.toEntity(dto))
                .thenReturn(entity);

        when(subjectRepository.save(entity))
                .thenReturn(saved);

        when(subjectMapper.toDto(saved))
                .thenReturn(resultDto);

        SubjectDto result = subjectService.create(dto);

        assertNotNull(result);
        assertEquals("MAT101", result.getCode());
        assertEquals("Mathematics", result.getName());

        verify(subjectRepository).save(entity);
    }

    @Test
    void create_shouldThrowWhenCodeMissing() {
        SubjectDto dto = new SubjectDto(1L, null, "Mathematics");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> subjectService.create(dto)
                );

        assertEquals(
                "Subject code is required.",
                exception.getMessage()
        );

        verify(subjectRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenNameMissing() {
        SubjectDto dto = new SubjectDto(1L, "MAT101", null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> subjectService.create(dto)
                );

        assertEquals(
                "Subject name is required.",
                exception.getMessage()
        );

        verify(subjectRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenCodeAlreadyExists() {
        SubjectDto dto =
                new SubjectDto(1L, "MAT101", "Mathematics");

        Subject existing =
                new Subject(2L, "MAT101", "Existing subject");

        when(subjectRepository.findByCode("MAT101"))
                .thenReturn(Optional.of(existing));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> subjectService.create(dto)
                );

        assertEquals(
                "Subject code already exists.",
                exception.getMessage()
        );

        verify(subjectRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnSubject() {
        Subject subject =
                new Subject(1L, "MAT101", "Mathematics");

        SubjectDto dto =
                new SubjectDto(1L, "MAT101", "Mathematics");

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.of(subject));

        when(subjectMapper.toDto(subject))
                .thenReturn(dto);

        SubjectDto result = subjectService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MAT101", result.getCode());
    }

    @Test
    void getById_shouldThrowWhenSubjectNotFound() {
        when(subjectRepository.findById(99L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> subjectService.getById(99L)
                );

        assertEquals(
                "Subject not found.",
                exception.getMessage()
        );
    }

    @Test
    void update_shouldUpdateSubject() {
        Subject existing =
                new Subject(1L, "MAT101", "Old Mathematics");

        SubjectDto updateDto =
                new SubjectDto(1L, "MAT101", "New Mathematics");

        SubjectDto resultDto =
                new SubjectDto(1L, "MAT101", "New Mathematics");

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(subjectRepository.save(existing))
                .thenReturn(existing);

        when(subjectMapper.toDto(existing))
                .thenReturn(resultDto);

        SubjectDto result =
                subjectService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals(
                "New Mathematics",
                result.getName()
        );

        verify(subjectMapper)
                .updateEntity(existing, updateDto);

        verify(subjectRepository)
                .save(existing);
    }

    @Test
    void delete_shouldThrowWhenSubjectNotFound() {
        when(subjectRepository.existsById(99L))
                .thenReturn(false);

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> subjectService.delete(99L)
                );

        assertEquals(
                "Subject not found.",
                exception.getMessage()
        );

        verify(subjectRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void delete_shouldDeleteSubject() {
        when(subjectRepository.existsById(1L))
                .thenReturn(true);

        subjectService.delete(1L);

        verify(subjectRepository)
                .deleteById(1L);
    }
}
