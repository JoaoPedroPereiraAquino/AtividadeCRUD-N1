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
- 🔒 **Validação**: Validação robusta de formulários e arquivos

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
- **Supabase**: Backend-as-a-Service para banco de dados e storage
- **PostgreSQL**: Banco de dados relacional
- **Supabase Storage**: Armazenamento de arquivos na nuvem
- **Maven**: Gerenciamento de dependências e build

## 📋 Pré-requisitos

- **Java 22** ou superior instalado
- **Maven 3.6+** para gerenciamento de dependências
- **Conta gratuita no Supabase** ([criar conta](https://supabase.com))
- **Navegador web moderno** (Chrome, Firefox, Safari, Edge)

## ⚙️ Configuração

### 1. Configuração do Supabase

1. Acesse [supabase.com](https://supabase.com) e crie um projeto
2. No SQL Editor, execute o seguinte comando para criar a tabela:

```sql
create table public."Atividade" (
  id uuid not null default gen_random_uuid (),
  texto text null,
  descricao text null,
  url_foto text null,
  created_at timestamp with time zone not null default now(),
  constraint Atividade_pkey primary key (id)
) TABLESPACE pg_default;
```

3. No Storage, crie um bucket chamado `atividade` (público)
4. Crie uma pasta chamada `atividade` dentro do bucket

### 2. Configuração da Aplicação

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd atividade-CRUD
```

2. Edite o arquivo `src/main/resources/application.properties` e configure:

```properties
# Configurações do Supabase
supabase.url=https://seu-projeto.supabase.co
supabase.anon.key=sua-chave-anonima-aqui
supabase.service.key=sua-chave-service-aqui

# Configurações do banco de dados PostgreSQL (Supabase)
spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:5432/postgres
spring.datasource.username=postgres.seu-projeto
spring.datasource.password=sua-senha-do-banco

# Configurações JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Configurações de upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 3. Executando a Aplicação

1. **Compile e execute**:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

2. **Acesse a aplicação**: 
   - URL: `http://localhost:8080`
   - Porta padrão: `8080`

### 4. Funcionalidades Disponíveis

Após executar a aplicação, você poderá:

- **📷 Adicionar Fotos**: Clique em "Adicionar Foto" para fazer upload
- **👁️ Visualizar Timeline**: Navegue pelas fotos em ordem cronológica
- **✏️ Editar Fotos**: Atualize título e descrição das fotos
- **🗑️ Excluir Fotos**: Remova fotos do álbum (com confirmação)
- **🔍 Buscar**: Use os filtros para encontrar fotos específicas
- **📊 Estatísticas**: Veja o total de fotos no seu álbum

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

## 🎓 Sobre o Projeto

### 📚 Contexto Acadêmico
Este projeto foi desenvolvido como uma atividade prática para demonstrar:
- Implementação de operações CRUD completas
- Integração com serviços de nuvem (Supabase)
- Desenvolvimento de interfaces web responsivas
- Uso de frameworks modernos (Spring Boot)
- Boas práticas de desenvolvimento

### 🎯 Objetivos de Aprendizado
- **Backend**: Spring Boot, JPA, REST APIs
- **Frontend**: HTML/CSS/JavaScript, Bootstrap
- **Banco de Dados**: PostgreSQL, SQL
- **Cloud**: Supabase, Storage em nuvem
- **DevOps**: Maven, Git, Deploy

## 📝 Licença

Este projeto é destinado para fins educacionais e acadêmicos.

## 👨‍💻 Desenvolvido por

**Projeto Acadêmico** - Sistema de Álbum de Fotos  
Curso: Desenvolvimento de Sistemas  
Tecnologias: Spring Boot + Supabase

---

**⚠️ Importante**: Configure suas credenciais do Supabase no arquivo `application.properties` antes de executar a aplicação.
