package com.example.atividade.CRUD.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Atividade")
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "texto")
    @Size(max = 255, message = "O texto deve ter no máximo 255 caracteres")
    private String texto;

    @Column(name = "descricao", columnDefinition = "TEXT")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @Column(name = "url_foto")
    private String urlFoto;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Construtores
    public Atividade() {}

    public Atividade(String texto, String descricao, String urlFoto) {
        this.texto = texto;
        this.descricao = descricao;
        this.urlFoto = urlFoto;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Atividade{" +
                "id=" + id +
                ", texto='" + texto + '\'' +
                ", descricao='" + descricao + '\'' +
                ", urlFoto='" + urlFoto + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
