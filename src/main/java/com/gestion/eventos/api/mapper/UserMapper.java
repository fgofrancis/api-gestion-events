package com.gestion.eventos.api.mapper;

import com.gestion.eventos.api.domain.Role;
import com.gestion.eventos.api.domain.User;
import com.gestion.eventos.api.dto.UserResponseDto;
import com.gestion.eventos.api.exception.ResourceNotFoundException;
import com.gestion.eventos.api.security.dto.RegisterDto;
import com.gestion.eventos.api.repository.RoleRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected RoleRepository reloRepository;

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", source = "registerDto.roles", qualifiedByName ="mapRoleStringsToRoles" )
    @Mapping(target = "attendedEvents", ignore = true)
    public abstract User registerDtoToUser(RegisterDto registerDto);

    public abstract UserResponseDto toUserResponseDto(User user);
    public abstract List<UserResponseDto> toUserResponseDtos(List<User> users);

    @Named("mapRoleStringsToRoles") /* Esto es una etiqueta para usarla cuando necesite este metodo */
    public Set<Role> mapRoleStringsToRoles(Set<String> roleNames){

        try {
            if(roleNames==null || roleNames.isEmpty()){
                return reloRepository.findByName("ROLE_USER")
                        .map(Collections::singleton)
                        .orElseThrow(
                                ()-> new ResourceNotFoundException("Error: Rol 'ROLE_USER' no encontrado en la base de datos."+
                                        "Asegurate que el rol exista al inicial la aplicacion")
                        );
            }

            return roleNames.stream()
                    .map(
                            roleName -> reloRepository.findByName(roleName)
                                    .orElseThrow(
                                            ()-> new ResourceNotFoundException("Error: Rol no encontrado")
                                    ))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Figuereo- valor faltante"+ e);
        }


    }

}
