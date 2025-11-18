package com.example.atividade.CRUD.controller;

import com.example.atividade.CRUD.entity.Atividade;
import com.example.atividade.CRUD.service.AtividadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class WebController {

    @Autowired
    private AtividadeService atividadeService;

    // Redirecionar /login para /auth/login (modal flutuante)
    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/auth/login";
    }

    // Página inicial - listar todas as atividades
    @GetMapping
    public String index(Model model) {
        List<Atividade> atividades = atividadeService.listarTodasAtividades();
        long totalAtividades = atividadeService.contarTotalAtividades();
        long atividadesComFoto = atividadeService.contarAtividadesComFoto();
        
        model.addAttribute("atividades", atividades);
        model.addAttribute("totalAtividades", totalAtividades);
        model.addAttribute("atividadesComFoto", atividadesComFoto);
        return "index";
    }

    // Página para criar nova atividade
    @GetMapping("/nova")
    public String novaAtividade(Model model) {
        model.addAttribute("atividade", new Atividade());
        return "nova-atividade";
    }

    // Processar criação de nova atividade
    @PostMapping("/nova")
    public String criarAtividade(@ModelAttribute Atividade atividade,
                                @RequestParam(value = "foto", required = false) MultipartFile foto,
                                RedirectAttributes redirectAttributes) {
        try {
            // Se uma foto foi enviada, fazer upload
            if (foto != null && !foto.isEmpty()) {
                String urlFoto = atividadeService.uploadFoto(foto);
                atividade.setUrlFoto(urlFoto);
            }
            
            atividadeService.salvarAtividade(atividade);
            redirectAttributes.addFlashAttribute("sucesso", "Foto adicionada com sucesso ao álbum!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao adicionar foto: " + e.getMessage());
            return "redirect:/nova";
        }
    }

    // Página para editar atividade
    @GetMapping("/editar/{id}")
    public String editarAtividade(@PathVariable UUID id, Model model) {
        Optional<Atividade> atividade = atividadeService.buscarAtividadePorId(id);
        if (atividade.isPresent()) {
            model.addAttribute("atividade", atividade.get());
            return "editar-atividade";
        } else {
            return "redirect:/";
        }
    }

    // Processar atualização de atividade
    @PostMapping("/editar/{id}")
    public String atualizarAtividade(@PathVariable UUID id,
                                   @ModelAttribute Atividade atividade,
                                   @RequestParam(value = "foto", required = false) MultipartFile foto,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Se uma nova foto foi enviada, fazer upload
            if (foto != null && !foto.isEmpty()) {
                String urlFoto = atividadeService.uploadFoto(foto);
                atividade.setUrlFoto(urlFoto);
            }
            
            atividadeService.atualizarAtividade(id, atividade);
            redirectAttributes.addFlashAttribute("sucesso", "Foto atualizada com sucesso!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar foto: " + e.getMessage());
            return "redirect:/editar/" + id;
        }
    }

    // Deletar atividade
    @PostMapping("/deletar/{id}")
    public String deletarAtividade(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            atividadeService.deletarAtividade(id);
            redirectAttributes.addFlashAttribute("sucesso", "Foto removida com sucesso do álbum!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover foto: " + e.getMessage());
        }
        return "redirect:/";
    }

    // Buscar atividades
    @GetMapping("/buscar")
    public String buscarAtividades(@RequestParam(required = false) String texto,
                                 @RequestParam(required = false) String descricao,
                                 Model model) {
        List<Atividade> atividades;
        
        if (texto != null && !texto.trim().isEmpty()) {
            atividades = atividadeService.buscarPorTexto(texto);
            model.addAttribute("termoBusca", texto);
        } else if (descricao != null && !descricao.trim().isEmpty()) {
            atividades = atividadeService.buscarPorDescricao(descricao);
            model.addAttribute("termoBusca", descricao);
        } else {
            atividades = atividadeService.listarTodasAtividades();
        }
        
        model.addAttribute("atividades", atividades);
        return "index";
    }

    // Página de atividades com foto
    @GetMapping("/com-foto")
    public String atividadesComFoto(Model model) {
        List<Atividade> atividades = atividadeService.buscarAtividadesComFoto();
        model.addAttribute("atividades", atividades);
        model.addAttribute("titulo", "Galeria de Fotos");
        return "index";
    }
}
