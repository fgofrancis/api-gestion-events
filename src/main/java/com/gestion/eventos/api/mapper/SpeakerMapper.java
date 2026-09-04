package com.gestion.eventos.api.mapper;

import com.gestion.eventos.api.domain.Speaker;
import com.gestion.eventos.api.dto.SpeakerRequestDto;
import com.gestion.eventos.api.dto.SpeakerResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpeakerMapper {

    SpeakerResponseDto toDto(Speaker speaker);

    @Mapping(target = "id",ignore = true )
    @Mapping(target = "events",ignore = true )
    Speaker toEntity(SpeakerRequestDto speakerDto);

    List<SpeakerResponseDto> toResponseDtoList(List<Speaker> speakers);

    @Mapping(target = "id", ignore = true) // los id nunca se modifican
    @Mapping(target = "events", ignore = true) // la logica compleja de gestionar events en Spearker la llevaremos a un servicio exclusivo
    void updateSpeakerFromDto(SpeakerRequestDto requestDto, @MappingTarget Speaker speaker);
}
