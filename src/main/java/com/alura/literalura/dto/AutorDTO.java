package com.alura.literalura.dto;

public record AutorDTO(
        Long id,
        String nombre,
        String fechaNacimiento,
        String fechaFallecimiento
) {
}
