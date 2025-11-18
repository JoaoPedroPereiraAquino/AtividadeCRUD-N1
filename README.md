# 📸 Meu Álbum de Fotos

Uma aplicação web moderna e elegante para gerenciar seu álbum de fotos pessoal, desenvolvida com Spring Boot e integrada ao Supabase para armazenamento na nuvem.

## 📖 Descrição

O **Meu Álbum de Fotos** é uma aplicação completa que permite organizar, visualizar e compartilhar suas memórias de forma intuitiva. Com um design inspirado em timeline, a aplicação oferece uma experiência visual atrativa para navegar através das suas fotos organizadas cronologicamente.

### 🎯 Objetivo
Esta aplicação foi desenvolvida como um projeto acadêmico para demonstrar a implementação de um sistema CRUD completo, integrando tecnologias modernas de desenvolvimento web com serviços de nuvem.

## ✨ Características Principais

- 📷 **Gerenciamento de Fotos**: Upload, visualização, edição e exclusão de fotos
- 🎨 **Interface Timeline**: Design moderno com layout de linha do tempo
- ☁️ **Armazenamento na Nuvem**: Integração com Supabase Storage
- 🔍 **Sistema de Busca**: Pesquisa por título ou descrição
- 📱 **Design Responsivo**: Compatível com dispositivos móveis e desktop
- ⚡ **Performance**: Carregamento rápido com animações suaves
- 🔒 **Autenticação**: Sistema completo de login e validação de tokens OAuth2
- 🐳 **Docker**: Configuração simplificada com Docker Compose para PostgreSQL

## 🛠️ Tecnologias Utilizadas

### Backend
- **Spring Boot 3.5.6**: Framework principal para desenvolvimento da API REST
- **Java 22**: Linguagem de programação
- **Spring Data JPA**: Para persistência e operações com banco de dados
- **Hibernate**: ORM para mapeamento objeto-relacional
- **Thymeleaf**: Engine de templates para renderização server-side

### Frontend
- **HTML5 & CSS3**: Estrutura e estilização das páginas
- **JavaScript**: Interatividade e animações
- **Bootstrap 5**: Framework CSS para design responsivo
- **Font Awesome**: Ícones vetoriais

### Infraestrutura
- **Supabase**: Backend-as-a-Service para banco de dados e storage (CRUD de atividades)
- **PostgreSQL**: Banco de dados relacional (autenticação via Docker)
- **Docker**: Containerização do banco de dados PostgreSQL
- **Supabase Storage**: Armazenamento de arquivos na nuvem
- **OAuth2**: Servidor de autenticação para login e validação de tokens
- **Maven**: Gerenciamento de dependências e build

## 📋 Pré-requisitos

- **Java 21** ou superior instalado
- **Maven 3.6+** para gerenciamento de dependências
- **Docker** e **Docker Compose** (para banco de dados de autenticação)
- **Java 8** ou superior (para o servidor de autenticação)
- **Conta gratuita no Supabase** ([criar conta](https://supabase.com))
- **Navegador web moderno** (Chrome, Firefox, Safari, Edge)

## 🚀 Início Rápido

### 1. Configurar Banco de Dados de Autenticação (Docker)

```bash
# Iniciar PostgreSQL com Docker
docker-compose up -d

# Verificar se está rodando
docker-compose ps
```

Isso criará automaticamente:
- Banco de dados `auth` na porta 5433
- Todas as tabelas OAuth2 necessárias
- Cliente OAuth2 padrão
- Usuário de teste (teste@teste.com / 123456)

📖 **Guia completo:** Veja [README_DOCKER.md](README_DOCKER.md) para mais detalhes.

### 2. Configurar e Executar o Servidor de Autenticação

```bash
cd auth-server
mvn clean install
mvn spring-boot:run
```

O servidor estará rodando em: `http://localhost:8082/auth-server`

📖 **Guia completo:** Veja [CONFIGURACAO_AUTH_SERVER.md](CONFIGURACAO_AUTH_SERVER.md) para mais detalhes.

### 3. Executar a Aplicação Principal

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### 4. Fazer Login

Acesse `http://localhost:8080/login` e use as credenciais:
- **Usuário:** teste@teste.com
- **Senha:** 123456

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/example/atividade/CRUD/
│   │   ├── config/
│   │   │   └── SupabaseConfig.java
│   │   ├── controller/
│   │   │   ├── AtividadeController.java
│   │   │   └── WebController.java
│   │   ├── entity/
│   │   │   └── Atividade.java
│   │   ├── repository/
│   │   │   └── AtividadeRepository.java
│   │   ├── service/
│   │   │   └── AtividadeService.java
│   │   └── AtividadeCrudApplication.java
│   └── resources/
│       ├── static/
│       │   ├── css/
│       │   │   └── style.css
│       │   └── js/
│       │       └── app.js
│       ├── templates/
│       │   ├── index.html
│       │   ├── nova-atividade.html
│       │   └── editar-atividade.html
│       └── application.properties
└── test/
```

## 🔌 Endpoints da API

### Atividades
- `GET /api/atividades` - Listar todas as atividades
- `GET /api/atividades/{id}` - Buscar atividade por ID
- `POST /api/atividades` - Criar nova atividade
- `PUT /api/atividades/{id}` - Atualizar atividade
- `DELETE /api/atividades/{id}` - Deletar atividade

### Busca
- `GET /api/atividades/buscar/texto?texto={termo}` - Buscar por texto
- `GET /api/atividades/buscar/descricao?descricao={termo}` - Buscar por descrição
- `GET /api/atividades/com-foto` - Buscar atividades com foto

### Upload
- `POST /api/atividades/upload-foto` - Upload de foto

### Estatísticas
- `GET /api/atividades/estatisticas` - Obter estatísticas

### Autenticação
- `POST /api/auth/login` - Fazer login e obter token
- `POST /api/auth/validate` - Validar token
- `GET /login` - Página de login
- `POST /auth/login` - Processar login (web)
- `POST /auth/logout` - Fazer logout

**Nota:** Todas as rotas (exceto `/login`, `/auth/**` e recursos estáticos) requerem autenticação.

## 🎨 Interface Web

### 📱 Páginas Disponíveis
- **`/`** - Página principal com timeline de fotos
- **`/nova`** - Formulário para adicionar nova foto
- **`/editar/{id}`** - Formulário para editar foto existente
- **`/com-foto`** - Galeria com todas as fotos
- **`/excluir/{id}`** - Exclusão de fotos (POST)

### ✨ Recursos da Interface
- **🎨 Design Timeline**: Layout moderno inspirado em redes sociais
- **📱 Totalmente Responsivo**: Funciona em mobile, tablet e desktop
- **🎭 Animações Suaves**: Efeitos de scroll e transições elegantes
- **🖼️ Preview de Imagens**: Visualização prévia antes do upload
- **🔍 Busca Inteligente**: Filtros por título e descrição
- **📊 Dashboard**: Estatísticas em tempo real
- **⚡ Performance**: Carregamento otimizado de imagens
- **🎯 UX/UI Moderno**: Interface intuitiva e acessível

## 🔧 Configurações Avançadas

### Limites de Upload
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Configurações JPA
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## 🐛 Solução de Problemas

### Erro de Conexão com Supabase
- Verifique se as credenciais estão corretas
- Confirme se o projeto está ativo no Supabase
- Teste a conexão com o banco de dados

### Erro de Upload de Arquivo
- Verifique se o bucket `atividade` existe no Supabase Storage
- Confirme se a pasta `atividade` foi criada dentro do bucket
- Verifique se o bucket está configurado como público
- Confirme se as credenciais do Supabase estão corretas

### Erro de Compilação
- Verifique se o Java 22 está instalado: `java --version`
- Execute `./mvnw clean install` para limpar e recompilar
- Confirme se a variável `JAVA_HOME` está configurada
