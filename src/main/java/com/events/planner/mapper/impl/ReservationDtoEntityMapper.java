 package com.events.planner.mapper.impl;

import com.events.planner.dto.ReservationDto;
import com.events.planner.entity.Reservation;
import com.events.planner.entity.ReservationStatus;
import com.events.planner.mapper.DtoEntityMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class ReservationDtoEntityMapper implements DtoEntityMapper<ReservationDto, Reservation> {

    @Override
    public ReservationDto toDto(Reservation e) {
        return new ReservationDto(
                e.getId(),
                e.getStart(),
                e.getEnd(),
                e.getStatus() != null ? e.getStatus().name() : null,
                e.getDescription(),
                e.getTimestamp(),
                e.getUser() != null ? e.getUser().getId() : null,
                e.getHall() != null ? e.getHall().getId() : null,
                e.getEvent() != null ? e.getEvent().getId() : null
        );
    }

    @Override
    public Reservation toEntity(ReservationDto t) {
        return new Reservation(
                t.getId(),
                t.getStart(),
                t.getEnd(),
                t.getStatus() != null ? ReservationStatus.valueOf(t.getStatus().trim().toUpperCase()) : null,
                t.getDescription(),
                t.getTimestamp(),
                null,
                null,
                null
        );
    }

    public void updateEntity(Reservation entity, ReservationDto dto) {
        entity.setStart(dto.getStart());
        entity.setEnd(dto.getEnd());
        entity.setStatus(dto.getStatus()!= null ? ReservationStatus.valueOf(dto.getStatus().trim().toUpperCase()) : null);
        entity.setDescription(dto.getDescription());
        entity.setTimestamp(LocalDateTime.now(ZoneId.of("Europe/Belgrade")));
    }
}
