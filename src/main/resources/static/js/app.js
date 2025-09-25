// JavaScript para funcionalidades do Álbum de Fotos

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar tooltips do Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Auto-hide alerts após 5 segundos
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    // Adicionar animação de fade-in aos cards
    var cards = document.querySelectorAll('.card');
    cards.forEach(function(card, index) {
        card.style.animationDelay = (index * 0.1) + 's';
        card.classList.add('fade-in');
    });
});

// Função para confirmar exclusão
function confirmarExclusao(id) {
    if (confirm('Tem certeza que deseja excluir esta atividade? Esta ação não pode ser desfeita.')) {
        const form = document.getElementById('formExclusao');
        form.action = '/deletar/' + id;
        form.submit();
    }
}

// Função para preview de imagem
function previewImage(input) {
    const preview = document.getElementById('preview');
    const previewImage = document.getElementById('previewImage');
    
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        
        reader.onload = function(e) {
            previewImage.src = e.target.result;
            preview.style.display = 'block';
        }
        
        reader.readAsDataURL(input.files[0]);
    } else {
        preview.style.display = 'none';
    }
}

// Função para remover preview
function removePreview() {
    const preview = document.getElementById('preview');
    const input = document.getElementById('foto');
    
    preview.style.display = 'none';
    input.value = '';
}

// Validação de formulário
function validarFormulario(form) {
    const texto = form.querySelector('#texto').value.trim();
    const foto = form.querySelector('#foto').files[0];
    
    if (!texto) {
        alert('Por favor, preencha o título da atividade.');
        return false;
    }
    
    if (foto) {
        const maxSize = 10 * 1024 * 1024; // 10MB
        if (foto.size > maxSize) {
            alert('O arquivo de foto deve ter no máximo 10MB.');
            return false;
        }
        
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
        if (!allowedTypes.includes(foto.type)) {
            alert('Por favor, selecione apenas arquivos de imagem (JPG, PNG, GIF).');
            return false;
        }
    }
    
    return true;
}

// Função para mostrar loading
function mostrarLoading(button) {
    const originalText = button.innerHTML;
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Carregando...';
    button.disabled = true;
    button.classList.add('loading');
    
    return function() {
        button.innerHTML = originalText;
        button.disabled = false;
        button.classList.remove('loading');
    };
}

// Função para buscar atividades via AJAX
function buscarAtividades(termo) {
    fetch(`/api/atividades/buscar/texto?texto=${encodeURIComponent(termo)}`)
        .then(response => response.json())
        .then(data => {
            atualizarListaAtividades(data);
        })
        .catch(error => {
            console.error('Erro ao buscar atividades:', error);
        });
}

// Função para atualizar lista de atividades
function atualizarListaAtividades(atividades) {
    const container = document.querySelector('.row');
    if (!container) return;
    
    container.innerHTML = '';
    
    if (atividades.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="fas fa-search fa-4x text-muted mb-3"></i>
                <h4 class="text-muted">Nenhuma atividade encontrada</h4>
                <p class="text-muted">Tente usar outros termos de busca</p>
            </div>
        `;
        return;
    }
    
    atividades.forEach(atividade => {
        const card = criarCardAtividade(atividade);
        container.appendChild(card);
    });
}

// Função para criar card de atividade
function criarCardAtividade(atividade) {
    const col = document.createElement('div');
    col.className = 'col-md-6 col-lg-4 mb-4';
    
    const fotoHtml = atividade.urlFoto ? 
        `<img src="${atividade.urlFoto}" class="card-img-top" alt="Foto da atividade">` :
        `<div class="card-img-top bg-light d-flex align-items-center justify-content-center no-image">
            <i class="fas fa-image fa-3x text-muted"></i>
         </div>`;
    
    const dataFormatada = new Date(atividade.createdAt).toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
    
    col.innerHTML = `
        <div class="card h-100 shadow-sm">
            ${fotoHtml}
            <div class="card-body d-flex flex-column">
                <h5 class="card-title">${atividade.texto || 'Sem título'}</h5>
                <p class="card-text flex-grow-1">${atividade.descricao || 'Sem descrição'}</p>
                <small class="text-muted">
                    <i class="fas fa-calendar me-1"></i>
                    ${dataFormatada}
                </small>
            </div>
            <div class="card-footer bg-transparent">
                <div class="btn-group w-100" role="group">
                    <a href="/editar/${atividade.id}" class="btn btn-outline-primary btn-sm">
                        <i class="fas fa-edit me-1"></i>Editar
                    </a>
                    <button type="button" class="btn btn-outline-danger btn-sm" 
                            onclick="confirmarExclusao('${atividade.id}')">
                        <i class="fas fa-trash me-1"></i>Excluir
                    </button>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

// Função para upload de arquivo com progress
function uploadComProgress(arquivo, callback) {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    
    const xhr = new XMLHttpRequest();
    
    xhr.upload.addEventListener('progress', function(e) {
        if (e.lengthComputable) {
            const percentComplete = (e.loaded / e.total) * 100;
            console.log('Upload progress: ' + percentComplete + '%');
        }
    });
    
    xhr.addEventListener('load', function() {
        if (xhr.status === 200) {
            callback(null, xhr.responseText);
        } else {
            callback(new Error('Erro no upload: ' + xhr.statusText), null);
        }
    });
    
    xhr.addEventListener('error', function() {
        callback(new Error('Erro de rede durante o upload'), null);
    });
    
    xhr.open('POST', '/api/atividades/upload-foto');
    xhr.send(formData);
}

// Função para formatar tamanho de arquivo
function formatarTamanhoArquivo(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Função para copiar URL da imagem
function copiarUrlImagem(url) {
    navigator.clipboard.writeText(url).then(function() {
        // Mostrar notificação de sucesso
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-white bg-success border-0';
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    URL copiada para a área de transferência!
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        // Remover o toast após ser fechado
        toast.addEventListener('hidden.bs.toast', function() {
            document.body.removeChild(toast);
        });
    }).catch(function(err) {
        console.error('Erro ao copiar URL: ', err);
        alert('Erro ao copiar URL para a área de transferência');
    });
}
