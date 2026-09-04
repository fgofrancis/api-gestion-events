package com.gestion.eventos.api.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    @Size(min = 4, max = 20, message = "El nombre de usuario debe tener entre 4 a 20 caracteres")
    private String username;

    @NotBlank(message = "El password no puede estar vacio")
    @Size(min = 6,message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El email on puede estar vacío")
    @Email(message = "Debe ser una direccion de correo electronico valido.")
    private String email;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    /* Si no llega roles lo asignaremos por defecto */
    private Set<String> roles;
}

/* Notas: En este cap 14, vamos a agregar la asignacion de roles en el RegisterDto
Actualmente está de esta manera:

 public class RegisterDto {
    private String username;
    private String password;
    private String email;
    private String name;
 } Esto conlleva implementar validaciones el RegisterDto

 Usaremos userMapper para la asignacion.
 Debemos alterar el userMapper que actualmente no asigna roles

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    User registerDtoToUser(RegisterDto registerDto);
}

La asignacion de roles la está haciendo nuestro aucontroller al registar un usuario
y esto no debe ser, la moveremos de lugar.
Acutal mente en el mapping(/register) está este codigo de asignacion de roles

        Role roles = roleRepository.findByName("ROLE_USER")
                .orElseThrow( () ->
                        new RuntimeException("Error, El rol de usuario no existe")
                        );

        user.setRoles(Collections.singleton(roles));

 */