package com.gestion.eventos.api.service;

import com.gestion.eventos.api.domain.Speaker;
import com.gestion.eventos.api.dto.SpeakerRequestDto;
import com.gestion.eventos.api.dto.SpeakerResponseDto;
import com.gestion.eventos.api.exception.ResourceNotFoundException;
import com.gestion.eventos.api.mapper.SpeakerMapper;
import com.gestion.eventos.api.repository.SpeakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeakerServiceImpl implements  ISpeakerService{
    private final SpeakerRepository speakerRepository;
    private final SpeakerMapper speakerMapper;


    @Override
    @Transactional(readOnly = true)
    public List<Speaker> findAll() {
        return speakerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Speaker findById(Long id) {
        return speakerRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Speaker no encontrado con  el id: "+ id));
    }

    @Override
    @Transactional
    public Speaker save(SpeakerRequestDto speakerRequestDto) {
        Speaker speaker = speakerMapper.toEntity(speakerRequestDto);
        return speakerRepository.save(speaker);
    }

    @Override
    @Transactional
    public Speaker update(Long id, SpeakerRequestDto speakerRequestDto) {
        Speaker existingSpeaker = speakerRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Speaker no encontrado con  el id: "+ id));

        speakerMapper.updateSpeakerFromDto(speakerRequestDto, existingSpeaker);
        return speakerRepository.save(existingSpeaker);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if(!speakerRepository.existsById(id)){
            throw  new ResourceNotFoundException("Speaker no encontrado con  el id: "+ id);
        }
        speakerRepository.deleteById(id);
    }
}
