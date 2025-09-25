package com.example.atividade.CRUD.repository;

import com.example.atividade.CRUD.entity.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, UUID> {

    // Buscar atividades por texto (busca parcial)
    @Query("SELECT a FROM Atividade a WHERE LOWER(a.texto) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Atividade> findByTextoContainingIgnoreCase(@Param("texto") String texto);

    // Buscar atividades por descrição (busca parcial)
    @Query("SELECT a FROM Atividade a WHERE LOWER(a.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    List<Atividade> findByDescricaoContainingIgnoreCase(@Param("descricao") String descricao);

    // Buscar atividades que tenham foto
    @Query("SELECT a FROM Atividade a WHERE a.urlFoto IS NOT NULL AND a.urlFoto != ''")
    List<Atividade> findAtividadesComFoto();

    // Buscar atividades ordenadas por data de criação (mais recentes primeiro)
    @Query("SELECT a FROM Atividade a ORDER BY a.createdAt DESC")
    List<Atividade> findAllOrderByCreatedAtDesc();

    // Contar total de atividades
    @Query("SELECT COUNT(a) FROM Atividade a")
    long countTotalAtividades();

    // Contar atividades com foto
    @Query("SELECT COUNT(a) FROM Atividade a WHERE a.urlFoto IS NOT NULL AND a.urlFoto != ''")
    long countAtividadesComFoto();
}
