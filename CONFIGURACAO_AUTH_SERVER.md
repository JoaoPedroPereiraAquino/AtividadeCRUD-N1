# Configuração do Servidor de Autenticação

Este guia explica como configurar e executar o servidor de autenticação necessário para o sistema de login.

## 🐳 Opção 1: Usando Docker (Recomendado)

A forma mais simples de configurar o banco de dados PostgreSQL é usando Docker.

### Pré-requisitos
- **Docker** e **Docker Compose** instalados
- **Java 8** ou superior (para o auth-server)
- **Maven** instalado

### Passos:

1. **Iniciar o PostgreSQL com Docker:**
```bash
docker-compose up -d
```

Isso irá:
- Criar um container PostgreSQL na porta **5433** (para não conflitar com PostgreSQL local)
- Criar automaticamente o banco de dados `auth`
- Criar todas as tabelas OAuth2 necessárias
- Inserir o cliente OAuth2 padrão
- Criar um usuário de teste (teste@teste.com / 123456)

2. **Verificar se o container está rodando:**
```bash
docker-compose ps
```

3. **Configurar o application.yml do Auth Server:**

Edite o arquivo `auth-server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5433/auth
    username: postgres
    password: 123456
```

**Nota:** A porta é **5433** (não 5432) porque o Docker mapeia a porta interna 5432 para a porta externa 5433.

4. **Parar o container (quando necessário):**
```bash
docker-compose down
```

5. **Parar e remover todos os dados:**
```bash
docker-compose down -v
```

### Credenciais de Teste (já criadas automaticamente):
- **Login:** teste@teste.com
- **Senha:** 123456

---

## 📦 Opção 2: Instalação Manual do PostgreSQL

Se preferir instalar o PostgreSQL manualmente:

### Pré-requisitos

1. **PostgreSQL** instalado e rodando
2. **Java 8** ou superior
3. **Maven** instalado

## Passo 1: Criar o Banco de Dados

1. Abra o PostgreSQL e crie um banco de dados chamado `auth`:

```sql
CREATE DATABASE auth;
```

2. Crie um usuário para o banco (opcional, pode usar `postgres`):

```sql
CREATE USER auth_user WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE auth TO auth_user;
```

## Passo 2: Criar as Tabelas OAuth2

Execute o seguinte script SQL no banco `auth`:

```sql
CREATE TABLE public.oauth_access_token (
	token_id varchar(256) NULL,
	token bytea NULL,
	authentication_id varchar(256) NOT NULL,
	user_name varchar(256) NULL,
	client_id varchar(256) NULL,
	authentication bytea NULL,
	refresh_token varchar(256) NULL,
	CONSTRAINT oauth_access_token_pk PRIMARY KEY (authentication_id)
);

CREATE TABLE public.oauth_approvals (
	userid varchar(256) NULL,
	clientid varchar(256) NULL,
	"scope" varchar(256) NULL,
	status varchar(10) NULL,
	expiresat timestamp NULL,
	lastmodifiedat timestamp NULL
);

CREATE TABLE public.oauth_client_details (
	client_id varchar(256) NOT NULL,
	resource_ids varchar(256) NULL,
	client_secret varchar(256) NULL,
	"scope" varchar(256) NULL,
	authorized_grant_types varchar(256) NULL,
	web_server_redirect_uri varchar(256) NULL,
	authorities varchar(256) NULL,
	access_token_validity int4 NULL,
	refresh_token_validity int4 NULL,
	additional_information varchar(4096) NULL,
	autoapprove varchar(256) NULL,
	CONSTRAINT oauth_client_details_pkey PRIMARY KEY (client_id)
);

CREATE TABLE public.oauth_client_token (
	token_id varchar(256) NULL,
	token bytea NULL,
	authentication_id varchar(256) NULL,
	user_name varchar(256) NULL,
	client_id varchar(256) NULL
);

CREATE TABLE public.oauth_code (
	code varchar(256) NULL,
	authentication bytea NULL
);

CREATE TABLE public.oauth_refresh_token (
	token_id varchar(256) NULL,
	token bytea NULL,
	authentication bytea NULL
);

-- Criar tabela de usuários
CREATE TABLE public.user_auth (
	login varchar(256) NOT NULL,
	password varchar(256) NULL,
	email varchar(256) NULL,
	roles varchar(256) NULL,
	tenant varchar(256) NULL,
	active boolean NULL,
	pass_date timestamp NULL,
	extra jsonb NULL,
	CONSTRAINT user_auth_pkey PRIMARY KEY (login)
);
```

## Passo 3: Configurar o application.yml do Auth Server

Edite o arquivo `auth-server/src/main/resources/application.yml` e ajuste as credenciais do banco de dados:

**Se estiver usando Docker:**
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5433/auth
    username: postgres
    password: 123456
```

**Se estiver usando PostgreSQL local:**
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/auth
    username: postgres
    password: 123456
```

## Passo 4: Criar o Cliente OAuth2

Execute o seguinte SQL no banco `auth`:

```sql
INSERT INTO oauth_client_details 
(client_id, client_secret, "scope", authorized_grant_types, access_token_validity, refresh_token_validity) 
VALUES 
('teste', 'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413', 
 'read,write', 'password,authorization_code,refresh_token', 15552000, 15552000);
```

## Passo 5: Criar um Usuário de Teste

Execute o seguinte SQL no banco `auth`:

```sql
-- Primeiro, ative a extensão pgcrypto (como superuser)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Criar usuário de teste
INSERT INTO user_auth (login, "password", roles, tenant, active) 
VALUES ('teste@teste.com', (SELECT ENCODE(DIGEST('123456','sha512'),'hex')), 'ROLE_ADMIN', 'teste', true);
```

**Credenciais de teste:**
- **Login:** teste@teste.com
- **Senha:** 123456

## Passo 6: Compilar e Executar o Auth Server

1. Navegue até a pasta do auth-server:

```bash
cd auth-server
```

2. Compile o projeto:

```bash
mvn clean install
```

3. Execute o servidor:

```bash
mvn spring-boot:run
```

O servidor estará rodando em: `http://localhost:8082/auth-server`

## Passo 7: Testar o Servidor de Auth

Você pode testar o servidor usando curl:

```bash
curl -X POST http://teste:123456@localhost:8082/auth-server/oauth/token \
  -d grant_type=password \
  -d username=teste@teste.com \
  -d password=123456
```

Se tudo estiver funcionando, você receberá um token de acesso.

## Passo 8: Executar a Aplicação Principal

1. Certifique-se de que o auth-server está rodando na porta 8082
2. Execute a aplicação principal:

```bash
mvn spring-boot:run
```

3. Acesse: `http://localhost:8080/login`
4. Faça login com as credenciais:
   - **Usuário:** teste@teste.com
   - **Senha:** 123456

## Solução de Problemas

### Erro de conexão com o banco
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `application.yml`
- Verifique se o banco `auth` foi criado

### Erro ao criar usuário
- Certifique-se de que a extensão `pgcrypto` está instalada
- Verifique se está executando como superuser

### Porta 8082 já em uso
- Altere a porta no `application.yml` do auth-server
- Atualize a URL no `application.properties` da aplicação principal

## Notas Importantes

- O banco de dados do Supabase **NÃO** será alterado - ele continua sendo usado apenas para o CRUD de atividades
- O servidor de auth usa um banco de dados PostgreSQL separado apenas para autenticação
- Os tokens são armazenados no banco `auth` e validados a cada requisição

