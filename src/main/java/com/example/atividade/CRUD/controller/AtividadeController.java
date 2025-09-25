package com.example.atividade.CRUD.controller;

import com.example.atividade.CRUD.entity.Atividade;
import com.example.atividade.CRUD.service.AtividadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/atividades")
@CrossOrigin(origins = "*")
public class AtividadeController {

    @Autowired
    private AtividadeService atividadeService;

    // GET - Listar todas as atividades
    @GetMapping
    public ResponseEntity<List<Atividade>> listarTodasAtividades() {
        List<Atividade> atividades = atividadeService.listarTodasAtividades();
        return ResponseEntity.ok(atividades);
    }

    // GET - Buscar atividade por ID
    @GetMapping("/{id}")
    public ResponseEntity<Atividade> buscarAtividadePorId(@PathVariable UUID id) {
        Optional<Atividade> atividade = atividadeService.buscarAtividadePorId(id);
        return atividade.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Criar nova atividade
    @PostMapping
    public ResponseEntity<Atividade> criarAtividade(@RequestBody Atividade atividade) {
        try {
            Atividade novaAtividade = atividadeService.salvarAtividade(atividade);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaAtividade);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT - Atualizar atividade
    @PutMapping("/{id}")
    public ResponseEntity<Atividade> atualizarAtividade(@PathVariable UUID id, @RequestBody Atividade atividade) {
        try {
            Atividade atividadeAtualizada = atividadeService.atualizarAtividade(id, atividade);
            return ResponseEntity.ok(atividadeAtualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE - Deletar atividade
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAtividade(@PathVariable UUID id) {
        try {
            atividadeService.deletarAtividade(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET - Buscar por texto
    @GetMapping("/buscar/texto")
    public ResponseEntity<List<Atividade>> buscarPorTexto(@RequestParam String texto) {
        List<Atividade> atividades = atividadeService.buscarPorTexto(texto);
        return ResponseEntity.ok(atividades);
    }

    // GET - Buscar por descrição
    @GetMapping("/buscar/descricao")
    public ResponseEntity<List<Atividade>> buscarPorDescricao(@RequestParam String descricao) {
        List<Atividade> atividades = atividadeService.buscarPorDescricao(descricao);
        return ResponseEntity.ok(atividades);
    }

    // GET - Buscar atividades com foto
    @GetMapping("/com-foto")
    public ResponseEntity<List<Atividade>> buscarAtividadesComFoto() {
        List<Atividade> atividades = atividadeService.buscarAtividadesComFoto();
        return ResponseEntity.ok(atividades);
    }

    // POST - Upload de foto
    @PostMapping("/upload-foto")
    public ResponseEntity<String> uploadFoto(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            String urlFoto = atividadeService.uploadFoto(arquivo);
            return ResponseEntity.ok(urlFoto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao fazer upload da foto: " + e.getMessage());
        }
    }

    // GET - Estatísticas
    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasResponse> obterEstatisticas() {
        long totalAtividades = atividadeService.contarTotalAtividades();
        long atividadesComFoto = atividadeService.contarAtividadesComFoto();
        
        EstatisticasResponse estatisticas = new EstatisticasResponse(totalAtividades, atividadesComFoto);
        return ResponseEntity.ok(estatisticas);
    }

    // Classe interna para resposta de estatísticas
    public static class EstatisticasResponse {
        private long totalAtividades;
        private long atividadesComFoto;

        public EstatisticasResponse(long totalAtividades, long atividadesComFoto) {
            this.totalAtividades = totalAtividades;
            this.atividadesComFoto = atividadesComFoto;
        }

        public long getTotalAtividades() {
            return totalAtividades;
        }

        public void setTotalAtividades(long totalAtividades) {
            this.totalAtividades = totalAtividades;
        }

        public long getAtividadesComFoto() {
            return atividadesComFoto;
        }

        public void setAtividadesComFoto(long atividadesComFoto) {
            this.atividadesComFoto = atividadesComFoto;
        }
    }
}
