# 🐳 Guia Rápido - Docker PostgreSQL

Este guia mostra como usar Docker para configurar rapidamente o banco de dados PostgreSQL para autenticação.

## 🚀 Início Rápido

### 1. Iniciar o PostgreSQL

```bash
docker-compose up -d
```

Este comando irá:
- ✅ Baixar a imagem do PostgreSQL 15 (se necessário)
- ✅ Criar o container `postgres-auth-server`
- ✅ Criar o banco de dados `auth`
- ✅ Executar automaticamente o script `init-db.sql` que cria:
  - Todas as tabelas OAuth2
  - Cliente OAuth2 padrão
  - Usuário de teste (teste@teste.com / 123456)

### 2. Verificar Status

```bash
docker-compose ps
```

Você deve ver algo como:
```
NAME                  STATUS          PORTS
postgres-auth-server  Up (healthy)    0.0.0.0:5433->5432/tcp
```

### 3. Ver Logs (opcional)

```bash
docker-compose logs -f postgres-auth
```

### 4. Configurar o Auth Server

Edite `auth-server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5433/auth
    username: postgres
    password: 123456
```

**Importante:** A porta é **5433** (não 5432) porque o Docker mapeia a porta interna.

### 5. Testar Conexão

Você pode testar a conexão usando qualquer cliente PostgreSQL:

- **Host:** localhost
- **Porta:** 5433
- **Database:** auth
- **Usuário:** postgres
- **Senha:** 123456

## 📋 Comandos Úteis

### Parar o container
```bash
docker-compose stop
```

### Iniciar novamente
```bash
docker-compose start
```

### Parar e remover container
```bash
docker-compose down
```

### Parar, remover container E todos os dados
```bash
docker-compose down -v
```

**⚠️ Atenção:** O último comando remove todos os dados do banco!

### Acessar o banco via terminal
```bash
docker exec -it postgres-auth-server psql -U postgres -d auth
```

### Executar SQL manualmente
```bash
docker exec -i postgres-auth-server psql -U postgres -d auth < seu-script.sql
```

## 🔍 Verificar Dados

### Ver usuários criados:
```bash
docker exec -it postgres-auth-server psql -U postgres -d auth -c "SELECT login, email, roles, active FROM user_auth;"
```

### Ver clientes OAuth2:
```bash
docker exec -it postgres-auth-server psql -U postgres -d auth -c "SELECT client_id, scope, authorized_grant_types FROM oauth_client_details;"
```

### Ver tabelas criadas:
```bash
docker exec -it postgres-auth-server psql -U postgres -d auth -c "\dt"
```

## 🗄️ Estrutura do Banco

O script `init-db.sql` cria automaticamente:

### Tabelas OAuth2:
- `oauth_access_token` - Tokens de acesso
- `oauth_refresh_token` - Tokens de refresh
- `oauth_client_details` - Clientes OAuth2
- `oauth_client_token` - Tokens de clientes
- `oauth_code` - Códigos de autorização
- `oauth_approvals` - Aprovações OAuth2

### Tabelas de Usuários:
- `user_auth` - Usuários do sistema

### Dados Iniciais:
- Cliente OAuth2: `teste` (client_id)
- Usuário de teste: `teste@teste.com` / `123456`

## 🔧 Solução de Problemas

### Container não inicia
```bash
# Ver logs de erro
docker-compose logs postgres-auth

# Verificar se a porta 5433 está livre
netstat -an | grep 5433
```

### Erro de permissão
```bash
# No Windows, pode ser necessário executar como administrador
# No Linux/Mac, pode ser necessário usar sudo
```

### Porta já em uso
Se a porta 5433 já estiver em uso, edite o `docker-compose.yml`:

```yaml
ports:
  - "5434:5432"  # Mude 5433 para outra porta
```

E atualize o `application.yml` do auth-server com a nova porta.

### Resetar banco de dados
```bash
# Parar e remover tudo
docker-compose down -v

# Iniciar novamente (vai recriar tudo do zero)
docker-compose up -d
```

## 📝 Notas Importantes

- O banco de dados é persistente - os dados são salvos no volume `postgres-auth-data`
- O script `init-db.sql` só é executado na primeira criação do container
- Para recriar tudo do zero, use `docker-compose down -v` e depois `docker-compose up -d`
- A porta externa é 5433 para não conflitar com PostgreSQL local (se houver)
- O volume Docker mantém os dados mesmo após parar o container

## 🎯 Próximos Passos

Após configurar o banco com Docker:

1. ✅ Banco PostgreSQL rodando em `localhost:5433`
2. ⏭️ Configurar e executar o auth-server (ver `CONFIGURACAO_AUTH_SERVER.md`)
3. ⏭️ Executar a aplicação principal
4. ⏭️ Testar login com `teste@teste.com` / `123456`

