-- Script de inicialização do banco de dados auth
-- Este script é executado automaticamente quando o container PostgreSQL é criado pela primeira vez

-- Criar extensão pgcrypto para hash de senhas
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Tabelas OAuth2
CREATE TABLE IF NOT EXISTS public.oauth_access_token (
	token_id varchar(256) NULL,
	token bytea NULL,
	authentication_id varchar(256) NOT NULL,
	user_name varchar(256) NULL,
	client_id varchar(256) NULL,
	authentication bytea NULL,
	refresh_token varchar(256) NULL,
	CONSTRAINT oauth_access_token_pk PRIMARY KEY (authentication_id)
);

CREATE TABLE IF NOT EXISTS public.oauth_approvals (
	userid varchar(256) NULL,
	clientid varchar(256) NULL,
	"scope" varchar(256) NULL,
	status varchar(10) NULL,
	expiresat timestamp NULL,
	lastmodifiedat timestamp NULL
);

CREATE TABLE IF NOT EXISTS public.oauth_client_details (
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

CREATE TABLE IF NOT EXISTS public.oauth_client_token (
	token_id varchar(256) NULL,
	token bytea NULL,
	authentication_id varchar(256) NULL,
	user_name varchar(256) NULL,
	client_id varchar(256) NULL
);

CREATE TABLE IF NOT EXISTS public.oauth_code (
	code varchar(256) NULL,
	authentication bytea NULL
);

CREATE TABLE IF NOT EXISTS public.oauth_refresh_token (
	token_id varchar(256) NULL,
	token bytea NULL,
	authentication bytea NULL
);

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS public.user_auth (
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

-- Inserir cliente OAuth2 padrão
INSERT INTO oauth_client_details 
(client_id, client_secret, "scope", authorized_grant_types, access_token_validity, refresh_token_validity) 
VALUES 
('teste', 'ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413', 
 'read,write', 'password,authorization_code,refresh_token', 15552000, 15552000)
ON CONFLICT (client_id) DO NOTHING;

-- Criar usuário de teste
-- Senha: 123456 (hash SHA512)
INSERT INTO user_auth (login, "password", roles, tenant, active) 
VALUES ('teste@teste.com', (SELECT ENCODE(DIGEST('123456','sha512'),'hex')), 'ROLE_ADMIN', 'teste', true)
ON CONFLICT (login) DO NOTHING;

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_oauth_access_token_user_name ON oauth_access_token(user_name);
CREATE INDEX IF NOT EXISTS idx_oauth_access_token_client_id ON oauth_access_token(client_id);
CREATE INDEX IF NOT EXISTS idx_user_auth_email ON user_auth(email);
CREATE INDEX IF NOT EXISTS idx_user_auth_active ON user_auth(active);

