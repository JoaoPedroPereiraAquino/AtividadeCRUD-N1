package com.example.atividade.CRUD.service;

import com.example.atividade.CRUD.entity.Atividade;
import com.example.atividade.CRUD.repository.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AtividadeService {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    private static final String BUCKET_NAME = "atividade";
    private static final String FOLDER_NAME = "atividade";
    private final RestTemplate restTemplate = new RestTemplate();

    // Operações CRUD básicas
    public List<Atividade> listarTodasAtividades() {
        return atividadeRepository.findAllOrderByCreatedAtDesc();
    }

    public Optional<Atividade> buscarAtividadePorId(UUID id) {
        return atividadeRepository.findById(id);
    }

    public Atividade salvarAtividade(Atividade atividade) {
        return atividadeRepository.save(atividade);
    }

    public Atividade atualizarAtividade(UUID id, Atividade atividadeAtualizada) {
        Optional<Atividade> atividadeExistente = atividadeRepository.findById(id);
        if (atividadeExistente.isPresent()) {
            Atividade atividade = atividadeExistente.get();
            atividade.setTexto(atividadeAtualizada.getTexto());
            atividade.setDescricao(atividadeAtualizada.getDescricao());
            atividade.setUrlFoto(atividadeAtualizada.getUrlFoto());
            return atividadeRepository.save(atividade);
        }
        throw new RuntimeException("Atividade não encontrada com ID: " + id);
    }

    public void deletarAtividade(UUID id) {
        Optional<Atividade> atividade = atividadeRepository.findById(id);
        if (atividade.isPresent()) {
            // Se a atividade tem uma foto, deletar do storage também
            if (atividade.get().getUrlFoto() != null && !atividade.get().getUrlFoto().isEmpty()) {
                deletarFotoDoStorage(atividade.get().getUrlFoto());
            }
            atividadeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Atividade não encontrada com ID: " + id);
        }
    }

    // Operações de busca
    public List<Atividade> buscarPorTexto(String texto) {
        return atividadeRepository.findByTextoContainingIgnoreCase(texto);
    }

    public List<Atividade> buscarPorDescricao(String descricao) {
        return atividadeRepository.findByDescricaoContainingIgnoreCase(descricao);
    }

    public List<Atividade> buscarAtividadesComFoto() {
        return atividadeRepository.findAtividadesComFoto();
    }

    // Operações de upload de arquivo
    public String uploadFoto(MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }

        // Validar tipo de arquivo
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem são permitidos");
        }

        // Gerar nome único para o arquivo
        String nomeArquivo = UUID.randomUUID().toString() + "_" + arquivo.getOriginalFilename();
        String caminhoArquivo = FOLDER_NAME + "/" + nomeArquivo;

        try {
            // Upload para o Supabase Storage via API REST (URL corrigida)
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + BUCKET_NAME + "/" + caminhoArquivo;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Authorization", "Bearer " + supabaseAnonKey);
            headers.set("x-upsert", "true"); // Permite sobrescrever arquivos
            
            // Usar bytes do arquivo diretamente
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(arquivo.getBytes(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // Retornar URL pública da imagem
                return supabaseUrl + "/storage/v1/object/public/" + BUCKET_NAME + "/" + caminhoArquivo;
            } else {
                throw new IOException("Erro no upload: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            // Log mais detalhado do erro
            System.err.println("Erro detalhado no upload: " + e.getMessage());
            throw new IOException("Erro ao fazer upload da foto: " + e.getMessage());
        }
    }

    public void deletarFotoDoStorage(String urlFoto) {
        try {
            // Extrair o nome do arquivo da URL
            String nomeArquivo = extrairNomeArquivoDaUrl(urlFoto);
            String caminhoArquivo = FOLDER_NAME + "/" + nomeArquivo;
            
            // Deletar do storage via API REST
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + BUCKET_NAME + "/" + caminhoArquivo;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseAnonKey);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
        } catch (Exception e) {
            System.err.println("Erro ao deletar foto do storage: " + e.getMessage());
        }
    }

    private String extrairNomeArquivoDaUrl(String url) {
        // Extrair o nome do arquivo da URL do Supabase
        String[] partes = url.split("/");
        return partes[partes.length - 1];
    }

    // Estatísticas
    public long contarTotalAtividades() {
        return atividadeRepository.countTotalAtividades();
    }

    public long contarAtividadesComFoto() {
        return atividadeRepository.countAtividadesComFoto();
    }
}
