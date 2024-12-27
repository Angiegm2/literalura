package com.alura.literalura.repository;

import com.alura.literalura.model.Autor;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);

    @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.librosDelAutor")
    List<Autor> findAllWithLibros();

    @EntityGraph(attributePaths = "librosDelAutor")
    @Query("SELECT a FROM Autor a WHERE " +
            "(a.fechaNacimiento <= :fecha AND (a.fechaFallecimiento IS NULL OR a.fechaFallecimiento > :fecha))")
    List<Autor>findAutoresVivosEnAño(String fecha);

    @EntityGraph(attributePaths = "librosDelAutor")
    List<Autor> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "librosDelAutor")
    @Query("SELECT a FROM Autor a WHERE " +
            "a.fechaFallecimiento <= :fecha AND a.fechaNacimiento IS NOT NULL")
    List<Autor> findAutoresFallecidosEnAño(String fecha);
}
