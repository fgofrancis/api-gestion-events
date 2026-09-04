package com.gestion.eventos.api.service;

import com.gestion.eventos.api.domain.Speaker;
import com.gestion.eventos.api.dto.SpeakerRequestDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ISpeakerService {
    List<Speaker> findAll();
    Speaker findById(Long id);
    Speaker save(SpeakerRequestDto speakerRequestDto);
    Speaker update(Long id, SpeakerRequestDto speakerRequestDto);
    void deleteById(Long id);

}
// Nota: al usar Dtos (SpeakerRequestDto) debemos mapear desde el servicio, no desde el controller
