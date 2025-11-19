# Meu Álbum de Fotos

Uma aplicação web moderna e elegante para gerenciar seu álbum de fotos pessoal, desenvolvida com Spring Boot e integrada ao Supabase para armazenamento na nuvem.

## Descrição

O **Meu Álbum de Fotos** é uma aplicação completa que permite organizar, visualizar e compartilhar suas memórias de forma intuitiva. Com um design inspirado em timeline, a aplicação oferece uma experiência visual atrativa para navegar através das suas fotos organizadas cronologicamente.

### Objetivo
Esta aplicação foi desenvolvida como um projeto acadêmico para demonstrar a implementação de um sistema CRUD completo, integrando tecnologias modernas de desenvolvimento web com serviços de nuvem.

## Características Principais

- **Gerenciamento de Fotos**: Upload, visualização, edição e exclusão de fotos
- **Interface Timeline**: Design moderno com layout de linha do tempo
- **Armazenamento na Nuvem**: Integração com Supabase Storage
- **Sistema de Busca**: Pesquisa por título ou descrição
- **Design Responsivo**: Compatível com dispositivos móveis e desktop
- **Performance**: Carregamento rápido com animações suaves
- **Autenticação**: Sistema completo de login e validação de tokens OAuth2
- **Docker**: Configuração simplificada com Docker Compose para PostgreSQL

## Tecnologias Utilizadas

### Backend
- **Spring Boot 3.5.6**: Framework principal para desenvolvimento da API REST
- **Java 22**: Linguagem de programação
- **Spring Data JPA**: Para persistência e operações com banco de dados
- **Hibernate**: ORM para mapeamento objeto-relacional
- **Thymeleaf**: Engine de templates para renderização server-side
- **Spring Security**: Autenticação e autorização
- **OAuth2**: Servidor de autenticação para login e validação de tokens

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
- **Maven**: Gerenciamento de dependências e build

## Pré-requisitos

- **Java 21** ou superior instalado
- **Maven 3.6+** para gerenciamento de dependências
- **Docker** e **Docker Compose** (para banco de dados de autenticação)
- **Java 17** ou superior (para o servidor de autenticação)
- **Conta gratuita no Supabase** ([criar conta](https://supabase.com))
- **Navegador web moderno** (Chrome, Firefox, Safari, Edge)
- **PowerShell** (Windows) para executar scripts de inicialização

## Estrutura do Projeto

```
AtividadeCRUD-N1/
├── auth-server/              # Servidor de autenticação OAuth2
│   ├── src/
│   │   └── main/
│   │       ├── java/         # Código fonte do auth-server
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── src/                      # Aplicação principal
│   └── main/
│       ├── java/             # Código fonte da aplicação
│       └── resources/
│           ├── application.properties
│           ├── static/       # CSS e JavaScript
│           └── templates/    # Templates HTML (Thymeleaf)
├── docker/                   # Configurações Docker
│   ├── docker-compose.yml    # Configuração do PostgreSQL
│   └── init-db.sql          # Script de inicialização do banco
├── scripts/                  # Scripts de automação
│   └── start-aplicacao.ps1  # Script para iniciar toda a aplicação
├── pom.xml                  # Configuração Maven da aplicação principal
├── mvnw                     # Maven Wrapper (Unix)
├── mvnw.cmd                 # Maven Wrapper (Windows)
└── README.md               # Este arquivo
```

## Início Rápido

### Opção 1: Usando o Script Automatizado (Recomendado)

A forma mais simples de iniciar toda a aplicação é usando o script PowerShell:

```powershell
.\scripts\start-aplicacao.ps1
```

Este script irá:
1. Verificar e configurar JAVA_HOME automaticamente
2. Iniciar o Docker container (PostgreSQL na porta 5433)
3. Iniciar o Auth-Server (porta 8082)
4. Iniciar a Aplicação Principal (porta 8080)
5. Aguardar todos os serviços ficarem prontos

**Nota:** O script sempre recarrega serviços que já estão rodando automaticamente.

### Opção 2: Iniciar Manualmente

#### 1. Configurar Banco de Dados de Autenticação (Docker)

```bash
# Navegar para a pasta docker
cd docker

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

#### 2. Configurar e Executar o Servidor de Autenticação

```bash
# Voltar para a raiz do projeto
cd ..

# Navegar para o auth-server
cd auth-server

# Compilar o projeto
mvn clean install

# Executar o servidor
mvn spring-boot:run
```

O servidor estará rodando em: `http://localhost:8082/auth-server`

#### 3. Executar a Aplicação Principal

Em um novo terminal:

```bash
# Na raiz do projeto
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

#### 4. Fazer Login

Acesse `http://localhost:8080/login` e use as credenciais:
- **Usuário:** teste@teste.com
- **Senha:** 123456

## Configuração do Banco de Dados

### Docker (Recomendado)

O arquivo `docker/docker-compose.yml` configura automaticamente:
- Container PostgreSQL 15 na porta 5433
- Volume persistente para dados
- Script de inicialização (`docker/init-db.sql`) que cria:
  - Todas as tabelas OAuth2
  - Cliente OAuth2 padrão (`teste`)
  - Usuário de teste (`teste@teste.com` / `123456`)

**Comandos úteis:**
```bash
# Iniciar
cd docker
docker-compose up -d

# Parar
docker-compose down

# Parar e remover dados
docker-compose down -v

# Ver logs
docker-compose logs -f
```

### Configuração Manual do PostgreSQL

Se preferir instalar o PostgreSQL manualmente:

1. Crie o banco de dados `auth`
2. Execute o script `docker/init-db.sql` no banco
3. Configure `auth-server/src/main/resources/application.yml` com as credenciais

## Endpoints da API

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
- `GET /auth/login` - Página de login
- `POST /auth/login` - Processar login (web)
- `POST /auth/logout` - Fazer logout
- `POST /auth/register` - Registrar novo usuário

**Nota:** Todas as rotas (exceto `/login`, `/auth/**` e recursos estáticos) requerem autenticação.

## Interface Web

### Páginas Disponíveis
- **`/`** - Página principal com timeline de fotos
- **`/nova`** - Formulário para adicionar nova foto
- **`/editar/{id}`** - Formulário para editar foto existente
- **`/com-foto`** - Galeria com todas as fotos
- **`/auth/login`** - Página de login

### Recursos da Interface
- **Design Timeline**: Layout moderno inspirado em redes sociais
- **Totalmente Responsivo**: Funciona em mobile, tablet e desktop
- **Animações Suaves**: Efeitos de scroll e transições elegantes
- **Preview de Imagens**: Visualização prévia antes do upload
- **Busca Inteligente**: Filtros por título e descrição
- **Dashboard**: Estatísticas em tempo real
- **Performance**: Carregamento otimizado de imagens
- **UX/UI Moderno**: Interface intuitiva e acessível

## Configurações

### Aplicação Principal (`src/main/resources/application.properties`)

```properties
# Supabase
supabase.url=https://seu-projeto.supabase.co
supabase.anon.key=sua-chave-anon
supabase.service.key=sua-chave-service

# Banco de dados Supabase
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=postgres.xxx
spring.datasource.password=sua-senha

# Auth Server
auth.server.url=http://localhost:8082/auth-server
auth.server.client.id=teste
auth.server.client.secret=123456

# Servidor
server.port=8080
```

### Auth Server (`auth-server/src/main/resources/application.yml`)

```yaml
server:
  port: 8082
  servlet:
    context-path: /auth-server

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/auth
    username: postgres
    password: 123456
```

## Solução de Problemas

### Erro: "Docker não está rodando"
- Inicie o Docker Desktop
- Aguarde até que o Docker esteja totalmente iniciado
- Execute o script novamente: `.\scripts\start-aplicacao.ps1`

### Erro: "Java não encontrado"
- Verifique se o Java está instalado: `java --version`
- O script tenta encontrar Java automaticamente
- Se necessário, defina JAVA_HOME manualmente

### Erro: "Porta já está em uso"
- O script recarrega automaticamente serviços em execução
- Se persistir, pare manualmente os processos usando as portas:
  - 5433 (PostgreSQL)
  - 8082 (Auth-Server)
  - 8080 (Aplicação Principal)

### Erro de Conexão com Supabase
- Verifique se as credenciais estão corretas em `application.properties`
- Confirme se o projeto está ativo no Supabase
- Teste a conexão com o banco de dados

### Erro de Upload de Arquivo
- Verifique se o bucket `atividade` existe no Supabase Storage
- Confirme se a pasta `atividade` foi criada dentro do bucket
- Verifique se o bucket está configurado como público
- Confirme se as credenciais do Supabase estão corretas

### Erro de Compilação
- Verifique se o Java está instalado: `java --version`
- Execute `./mvnw clean install` para limpar e recompilar
- Confirme se a variável `JAVA_HOME` está configurada

### Auth-Server não inicia
- Verifique se o Docker está rodando e o PostgreSQL está acessível
- Confirme as credenciais em `auth-server/src/main/resources/application.yml`
- Verifique os logs na janela do PowerShell do auth-server

## Parar os Serviços

### Usando o Script
O script abre janelas separadas do PowerShell para cada serviço. Para parar:
- Feche as janelas do PowerShell dos serviços (Auth-Server e Aplicação Principal)
- Ou pressione `Ctrl+C` em cada janela

### Docker
```bash
cd docker
docker-compose down
```

Para remover todos os dados:
```bash
docker-compose down -v
```

## Credenciais de Teste

- **Usuário:** teste@teste.com
- **Senha:** 123456

## Desenvolvimento

### Compilar o Projeto

```bash
# Aplicação principal
mvn clean install

# Auth-server
cd auth-server
mvn clean install
```

### Executar Testes

```bash
mvn test
```

## Licença

Este projeto foi desenvolvido para fins acadêmicos.

## Autor

Desenvolvido como projeto acadêmico para demonstrar implementação de sistema CRUD completo.
