# Script para iniciar a aplicação completa
# Inicia: Docker (PostgreSQL), Auth-Server e Aplicação Principal

param(
    [switch]$Force
)

$ErrorActionPreference = "Continue"
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Iniciando Aplicação CRUD Completa" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurar JAVA_HOME se não estiver definido
if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
    # Tentar encontrar Java em locais comuns
    $possibleJavaPaths = @(
        "C:\Program Files\Java\jdk-25",
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk-11",
        "C:\Program Files\Java\jdk"
    )
    
    $javaFound = $false
    foreach ($javaPath in $possibleJavaPaths) {
        if (Test-Path $javaPath) {
            $env:JAVA_HOME = $javaPath
            Write-Host "[INFO] JAVA_HOME configurado: $javaPath" -ForegroundColor Green
            $javaFound = $true
            break
        }
    }
    
    # Se não encontrou, tentar usar where.exe para encontrar java.exe
    if (-not $javaFound) {
        try {
            $javaExe = where.exe java 2>$null | Select-Object -First 1
            if ($javaExe) {
                $javaDir = Split-Path (Split-Path $javaExe)
                if (Test-Path $javaDir) {
                    $env:JAVA_HOME = $javaDir
                    Write-Host "[INFO] JAVA_HOME encontrado automaticamente: $javaDir" -ForegroundColor Green
                    $javaFound = $true
                }
            }
        } catch {
            # Ignorar erro
        }
    }
    
    if (-not $javaFound) {
        Write-Host "[ERRO] Java não encontrado!" -ForegroundColor Red
        Write-Host "[INFO] Verifique se o Java está instalado e defina JAVA_HOME manualmente" -ForegroundColor Yellow
        Write-Host "[INFO] Exemplo: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-XX'" -ForegroundColor Yellow
        exit 1
    }
}

# Verificar se JAVA_HOME está correto
if (-not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    Write-Host "[ERRO] JAVA_HOME configurado incorretamente: $env:JAVA_HOME" -ForegroundColor Red
    Write-Host "[INFO] JAVA_HOME deve apontar para o diretório raiz do JDK (não bin)" -ForegroundColor Yellow
    exit 1
}

Write-Host "[INFO] JAVA_HOME verificado: $env:JAVA_HOME" -ForegroundColor Green

# Função para verificar se uma porta está em uso
function Test-Port {
    param([int]$Port)
    $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    return $null -ne $connection
}

# Função para parar processo em uma porta
function Stop-PortProcess {
    param([int]$Port, [string]$ServiceName)
    
    $processes = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | 
                 Select-Object -ExpandProperty OwningProcess -Unique
    
    if ($processes) {
        foreach ($procId in $processes) {
            try {
                $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
                if ($proc) {
                    if ($proc.ProcessName -eq "java") {
                        Write-Host "[INFO] Parando $ServiceName (PID: $procId, Processo: $($proc.ProcessName)) na porta $Port..." -ForegroundColor Yellow
                        Stop-Process -Id $procId -Force -ErrorAction Stop
                        Start-Sleep -Seconds 3
                        Write-Host "[OK] Processo $procId parado com sucesso" -ForegroundColor Green
                    } else {
                        Write-Host "[AVISO] Processo na porta $Port não é Java (PID: $procId, Nome: $($proc.ProcessName))" -ForegroundColor Yellow
                        Write-Host "[INFO] Você pode precisar parar este processo manualmente" -ForegroundColor Gray
                    }
                }
            } catch {
                Write-Host "[ERRO] Não foi possível parar processo $procId : ${_}" -ForegroundColor Red
                Write-Host "[INFO] Tente parar manualmente ou reinicie o computador" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "[INFO] Nenhum processo encontrado na porta $Port" -ForegroundColor Gray
    }
}

# Função para aguardar serviço ficar disponível
function Wait-ForService {
    param(
        [int]$Port,
        [string]$ServiceName,
        [int]$MaxWait = 60
    )
    
    $attempt = 0
    while ($attempt -lt $MaxWait) {
        if (Test-Port -Port $Port) {
            Write-Host "[OK] $ServiceName está respondendo na porta $Port" -ForegroundColor Green
            return $true
        }
        $attempt++
        Write-Host "[INFO] Aguardando $ServiceName iniciar... ($attempt/$MaxWait)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
    
    Write-Host "[ERRO] $ServiceName não iniciou após $MaxWait segundos" -ForegroundColor Red
    return $false
}

# ============================================
# 1. DOCKER - PostgreSQL
# ============================================
Write-Host "[1/3] Verificando Docker (PostgreSQL)..." -ForegroundColor Cyan

try {
    # Verificar se Docker está rodando
    Write-Host "[INFO] Verificando se Docker está rodando..." -ForegroundColor Gray
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERRO] Docker não está rodando!" -ForegroundColor Red
        Write-Host "[INFO] Ação necessária: Inicie o Docker Desktop e aguarde até que esteja totalmente iniciado" -ForegroundColor Yellow
        Write-Host "[INFO] Depois execute o script novamente" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "[OK] Docker está rodando" -ForegroundColor Green
    
    # Verificar se o container existe
    Write-Host "[INFO] Verificando container PostgreSQL..." -ForegroundColor Gray
    $containerExists = docker ps -a --filter "name=postgres-auth-server" --format "{{.Names}}" 2>&1
    
    if ($containerExists -and $containerExists.Contains("postgres-auth-server")) {
        # Container existe, verificar se está rodando
        $containerRunning = docker ps --filter "name=postgres-auth-server" --format "{{.Names}}" 2>&1
        
        if ($containerRunning -and $containerRunning.Contains("postgres-auth-server")) {
            Write-Host "[INFO] Container PostgreSQL já está rodando" -ForegroundColor Green
            Write-Host "[INFO] Recarregando container para garantir estado limpo..." -ForegroundColor Yellow
            try {
                Push-Location (Join-Path $projectRoot "docker")
                docker-compose down 2>&1 | Out-Null
                if ($LASTEXITCODE -ne 0) {
                    Write-Host "[AVISO] Erro ao parar container, tentando continuar..." -ForegroundColor Yellow
                }
                Start-Sleep -Seconds 3
                docker-compose up -d 2>&1 | Out-Null
                if ($LASTEXITCODE -ne 0) {
                    throw "Falha ao iniciar container"
                }
                Pop-Location
                Write-Host "[OK] Container recarregado com sucesso" -ForegroundColor Green
            } catch {
                Pop-Location
                Write-Host "[ERRO] Erro ao recarregar container: ${_}" -ForegroundColor Red
                Write-Host "[INFO] Tentando iniciar novamente..." -ForegroundColor Yellow
                Push-Location (Join-Path $projectRoot "docker")
                docker-compose up -d
                Pop-Location
            }
        } else {
            Write-Host "[INFO] Container existe mas não está rodando. Removendo e recriando..." -ForegroundColor Yellow
            try {
                Push-Location (Join-Path $projectRoot "docker")
                # Remover container parado primeiro
                Write-Host "[INFO] Removendo container parado..." -ForegroundColor Gray
                $downOutput = docker-compose down 2>&1
                if ($LASTEXITCODE -ne 0) {
                    Write-Host "[AVISO] Avisos ao remover container (pode ser normal): $downOutput" -ForegroundColor Yellow
                }
                Start-Sleep -Seconds 2
                # Criar e iniciar novamente
                Write-Host "[INFO] Criando e iniciando container..." -ForegroundColor Gray
                $upOutput = docker-compose up -d 2>&1
                $exitCode = $LASTEXITCODE
                if ($exitCode -ne 0) {
                    Write-Host "[ERRO] Saída do docker-compose: $upOutput" -ForegroundColor Red
                    throw "Falha ao iniciar container (código de saída: $exitCode)"
                }
                # Verificar se o container realmente iniciou
                Start-Sleep -Seconds 2
                $containerStatus = docker ps --filter "name=postgres-auth-server" --format "{{.Status}}" 2>&1
                if ($containerStatus -and $containerStatus -notmatch "Up") {
                    Write-Host "[AVISO] Container pode não ter iniciado corretamente. Status: $containerStatus" -ForegroundColor Yellow
                }
                Pop-Location
                Write-Host "[OK] Container reiniciado com sucesso" -ForegroundColor Green
            } catch {
                Pop-Location
                Write-Host "[ERRO] Detalhes do erro: ${_}" -ForegroundColor Red
                throw "Falha ao reiniciar container: ${_}"
            }
        }
    } else {
        Write-Host "[INFO] Container não existe. Criando e iniciando..." -ForegroundColor Yellow
        Push-Location (Join-Path $projectRoot "docker")
        docker-compose up -d
        Pop-Location
        if ($LASTEXITCODE -ne 0) {
            throw "Falha ao criar container"
        }
    }
    
    # Aguardar container ficar saudável
    Write-Host "[INFO] Aguardando PostgreSQL ficar pronto..." -ForegroundColor Gray
    $healthCheckAttempts = 0
    $maxHealthCheck = 30
    
    while ($healthCheckAttempts -lt $maxHealthCheck) {
        $health = docker inspect --format='{{.State.Health.Status}}' postgres-auth-server 2>&1
        if ($health -eq "healthy") {
            Write-Host "[OK] PostgreSQL está saudável e pronto" -ForegroundColor Green
            break
        }
        $healthCheckAttempts++
        Start-Sleep -Seconds 2
    }
    
    if ($healthCheckAttempts -ge $maxHealthCheck) {
        Write-Host "[AVISO] PostgreSQL pode não estar totalmente pronto, mas continuando..." -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "[ERRO] Erro ao gerenciar Docker: ${_}" -ForegroundColor Red
    exit 1
}

Write-Host ""

# ============================================
# 2. AUTH-SERVER (Porta 8082)
# ============================================
Write-Host "[2/3] Verificando Auth-Server (porta 8082)..." -ForegroundColor Cyan

try {
    # Sempre verificar e recarregar se necessário
    if (Test-Port -Port 8082) {
        Write-Host "[INFO] Porta 8082 em uso. Recarregando Auth-Server..." -ForegroundColor Yellow
        Stop-PortProcess -Port 8082 -ServiceName "Auth-Server"
        Start-Sleep -Seconds 2
        
        # Verificar se a porta foi liberada
        $maxWait = 10
        $waited = 0
        while (Test-Port -Port 8082) {
            if ($waited -ge $maxWait) {
                Write-Host "[AVISO] Porta 8082 ainda está em uso após $maxWait segundos" -ForegroundColor Yellow
                Write-Host "[INFO] Tentando iniciar mesmo assim..." -ForegroundColor Gray
                break
            }
            Start-Sleep -Seconds 1
            $waited++
        }
    }
    
    if (-not (Test-Port -Port 8082)) {
        Write-Host "[INFO] Iniciando Auth-Server..." -ForegroundColor Yellow
        
        $authServerPath = Join-Path $projectRoot "auth-server"
        if (-not (Test-Path $authServerPath)) {
            Write-Host "[ERRO] Diretório auth-server não encontrado!" -ForegroundColor Red
            exit 1
        }
        
        # Verificar se mvnw existe
        $mvnwPath = Join-Path $projectRoot "mvnw.cmd"
        if (-not (Test-Path $mvnwPath)) {
            throw "mvnw.cmd não encontrado no diretório raiz"
        }
        
        # Iniciar auth-server em nova janela
        $javaHomeValue = $env:JAVA_HOME
        Write-Host "[INFO] Iniciando Auth-Server com JAVA_HOME: $javaHomeValue" -ForegroundColor Gray
        
        try {
            $authServerProcess = Start-Process powershell -ArgumentList @(
                "-NoExit",
                "-Command",
                "cd '$authServerPath'; `$env:JAVA_HOME = '$javaHomeValue'; Write-Host '=== AUTH-SERVER ===' -ForegroundColor Cyan; Write-Host 'JAVA_HOME: ' `$env:JAVA_HOME; Write-Host 'Diretório: ' (Get-Location); Write-Host ''; ..\mvnw.cmd spring-boot:run"
            ) -PassThru -WindowStyle Normal -ErrorAction Stop
            
            Write-Host "[INFO] Auth-Server iniciando (PID: $($authServerProcess.Id))..." -ForegroundColor Gray
            Write-Host "[INFO] Aguarde a inicialização (pode levar até 60 segundos)..." -ForegroundColor Gray
            
            # Aguardar auth-server ficar disponível
            if (Wait-ForService -Port 8082 -ServiceName "Auth-Server" -MaxWait 60) {
                Write-Host "[OK] Auth-Server iniciado com sucesso!" -ForegroundColor Green
            } else {
                Write-Host "[AVISO] Auth-Server pode não estar totalmente pronto" -ForegroundColor Yellow
                Write-Host "[INFO] Verifique a janela do Auth-Server para ver os logs" -ForegroundColor Gray
            }
        } catch {
            throw "Falha ao iniciar processo do Auth-Server: ${_}"
        }
    } else {
        Write-Host "[AVISO] Porta 8082 ainda está em uso. Auth-Server pode não ter parado completamente." -ForegroundColor Yellow
        Write-Host "[INFO] Verifique manualmente e tente novamente" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "[ERRO] Erro ao iniciar Auth-Server: ${_}" -ForegroundColor Red
    Write-Host "[INFO] Possíveis causas:" -ForegroundColor Yellow
    Write-Host "  - JAVA_HOME não está configurado corretamente" -ForegroundColor Gray
    Write-Host "  - Porta 8082 está sendo usada por outro aplicativo" -ForegroundColor Gray
    Write-Host "  - Diretório auth-server não existe ou está corrompido" -ForegroundColor Gray
    Write-Host "  - Maven Wrapper não está disponível" -ForegroundColor Gray
    Write-Host "[INFO] Verifique os logs na janela do Auth-Server se ela foi aberta" -ForegroundColor Yellow
}

Write-Host ""

# ============================================
# 3. APLICAÇÃO PRINCIPAL (Porta 8080)
# ============================================
Write-Host "[3/3] Verificando Aplicação Principal (porta 8080)..." -ForegroundColor Cyan

try {
    # Sempre verificar e recarregar se necessário
    if (Test-Port -Port 8080) {
        Write-Host "[INFO] Porta 8080 em uso. Recarregando Aplicação Principal..." -ForegroundColor Yellow
        Stop-PortProcess -Port 8080 -ServiceName "Aplicação Principal"
        Start-Sleep -Seconds 2
        
        # Verificar se a porta foi liberada
        $maxWait = 10
        $waited = 0
        while (Test-Port -Port 8080) {
            if ($waited -ge $maxWait) {
                Write-Host "[AVISO] Porta 8080 ainda está em uso após $maxWait segundos" -ForegroundColor Yellow
                Write-Host "[INFO] Tentando iniciar mesmo assim..." -ForegroundColor Gray
                break
            }
            Start-Sleep -Seconds 1
            $waited++
        }
    }
    
    if (-not (Test-Port -Port 8080)) {
        Write-Host "[INFO] Iniciando Aplicação Principal..." -ForegroundColor Yellow
        
        # Verificar se mvnw existe
        $mvnwPath = Join-Path $projectRoot "mvnw.cmd"
        if (-not (Test-Path $mvnwPath)) {
            Write-Host "[ERRO] mvnw.cmd não encontrado!" -ForegroundColor Red
            exit 1
        }
        
        # Iniciar aplicação principal em nova janela
        $javaHomeValue = $env:JAVA_HOME
        Write-Host "[INFO] Iniciando Aplicação Principal com JAVA_HOME: $javaHomeValue" -ForegroundColor Gray
        
        try {
            $appProcess = Start-Process powershell -ArgumentList @(
                "-NoExit",
                "-Command",
                "cd '$projectRoot'; `$env:JAVA_HOME = '$javaHomeValue'; Write-Host '=== APLICAÇÃO PRINCIPAL ===' -ForegroundColor Cyan; Write-Host 'JAVA_HOME: ' `$env:JAVA_HOME; Write-Host 'Diretório: ' (Get-Location); Write-Host ''; .\mvnw.cmd spring-boot:run"
            ) -PassThru -WindowStyle Normal -ErrorAction Stop
            
            Write-Host "[INFO] Aplicação Principal iniciando (PID: $($appProcess.Id))..." -ForegroundColor Gray
            Write-Host "[INFO] Aguarde a inicialização (pode levar até 90 segundos)..." -ForegroundColor Gray
            
            # Aguardar aplicação ficar disponível
            if (Wait-ForService -Port 8080 -ServiceName "Aplicação Principal" -MaxWait 90) {
                Write-Host "[OK] Aplicação Principal iniciada com sucesso!" -ForegroundColor Green
            } else {
                Write-Host "[AVISO] Aplicação Principal pode não estar totalmente pronto" -ForegroundColor Yellow
                Write-Host "[INFO] Verifique a janela da aplicação para ver os logs" -ForegroundColor Gray
            }
        } catch {
            throw "Falha ao iniciar processo da Aplicação Principal: ${_}"
        }
    } else {
        Write-Host "[AVISO] Porta 8080 ainda está em uso. Aplicação pode não ter parado completamente." -ForegroundColor Yellow
        Write-Host "[INFO] Verifique manualmente e tente novamente" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "[ERRO] Erro ao iniciar Aplicação Principal: ${_}" -ForegroundColor Red
    Write-Host "[INFO] Possíveis causas:" -ForegroundColor Yellow
    Write-Host "  - JAVA_HOME não está configurado corretamente" -ForegroundColor Gray
    Write-Host "  - Porta 8080 está sendo usada por outro aplicativo" -ForegroundColor Gray
    Write-Host "  - Maven Wrapper não está disponível" -ForegroundColor Gray
    Write-Host "  - Erro de compilação ou dependências faltando" -ForegroundColor Gray
    Write-Host "[INFO] Verifique os logs na janela da aplicação se ela foi aberta" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Inicialização Concluída!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Serviços disponíveis:" -ForegroundColor Green
Write-Host "  - PostgreSQL:    localhost:5433" -ForegroundColor White
Write-Host "  - Auth-Server:   http://localhost:8082/auth-server/" -ForegroundColor White
Write-Host "  - Aplicação:     http://localhost:8080/" -ForegroundColor White
Write-Host ""
Write-Host "Credenciais de teste:" -ForegroundColor Yellow
Write-Host "  Usuário: teste@teste.com" -ForegroundColor White
Write-Host "  Senha:   123456" -ForegroundColor White
Write-Host ""
Write-Host "Nota: O script sempre recarrega serviços que já estão rodando" -ForegroundColor Gray
Write-Host ""
Write-Host "Para parar os serviços:" -ForegroundColor Gray
Write-Host "  - Feche as janelas do PowerShell dos serviços" -ForegroundColor White
Write-Host "  - Ou execute: cd docker; docker-compose down (para PostgreSQL)" -ForegroundColor White
Write-Host ""

