# 🏋️ MyFitnessPartner

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-6db33f.svg)](https://spring.io/projects/spring-ai)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-brightgreen.svg)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> Assistente fitness pessoal via chat web, powered by Google Gemini AI

🌐 **Demo ao vivo:** [fitness.renanleite.dev.br](https://fitness.renanleite.dev.br)

## 📋 Sobre o Projeto

MyFitnessPartner é uma API backend que integra **chat web com Inteligência Artificial** para auxiliar usuários em sua jornada fitness. Através de conversas naturais pelo chat web, os usuários podem calcular calorias de refeições, acompanhar metas diárias e receber orientações personalizadas.

O projeto demonstra a implementação de uma arquitetura moderna utilizando as melhores práticas do ecossistema Java/Spring, incluindo **Spring AI**, **JWT com blacklist em Redis** e **Testcontainers**.

## 🏗️ Arquitetura

```
                    ┌─────────────────────────────────────┐
                    │         MyFitnessPartner API         │
                    │                                     │
 Chat Web  ◄──────► │                   ┌──────────────┐  │
 (Frontend)         │                   │  REST API    │  │
                    │                   │  Controllers │  │
                    │                   └──────┬───────┘  │
                    │                          │           │
                    │  ┌───────────────────────▼───────┐  │
                    │  │      Application Services     │  │
                    │  │  Auth │ Meals │ Goals │ Chat  │  │
                    │  └────┬──────────────────────────┘  │
                    │       │                             │
                    │  ┌────▼────────────────────────┐   │
                    │  │      Infrastructure          │   │
                    │  │  ┌──────────┐ ┌──────────┐  │   │
                    │  │  │Spring AI │ │ Security │  │   │
                    │  │  │(Gemini)  │ │JWT+Redis │  │   │
                    │  │  └──────────┘ └──────────┘  │   │
                    │  └─────────────────────────────┘   │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │         PostgreSQL            │
                    │  users │ meals │ goals │ ...  │
                    └──────────────────────────────┘
```

### Decisões Arquiteturais

- **Arquitetura em camadas** — separação clara entre `presentation`, `application`, `domain` e `infrastructure`
- **Spring AI com abstração de provedor** — `FitnessAiPort` desacopla o domínio do provedor de IA (hoje Gemini, amanhã qualquer outro)
- **JWT stateless com blacklist no Redis** — logout real sem sessão no servidor
- **Chat web via REST** — o frontend consome os endpoints REST para conversar com a IA

## 🚀 Tecnologias

### Core
| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem base |
| Spring Boot | 3.2 | Framework principal |
| Spring AI | 1.0 | Abstração de IA (Gemini via OpenAI-compatible endpoint) |
| Spring Security | 6 | Autenticação JWT |
| Gradle | 8.5+ | Build tool |

### Banco de Dados & Cache
| Tecnologia | Uso |
|---|---|
| PostgreSQL 16 | Usuários, refeições, metas, conversas |
| Redis 7 | Blacklist de tokens JWT |

### Integrações Externas
| Serviço | Uso |
|---|---|
| Google Gemini 2.5 Flash | LLM para análise nutricional e chat |

### Testes & Qualidade
| Tecnologia | Uso |
|---|---|
| JUnit 5 + Mockito | Testes unitários |
| Testcontainers | Testes de integração com PostgreSQL e Redis reais |
| JaCoCo | Cobertura de código |

### DevOps
| Tecnologia | Uso |
|---|---|
| Docker + Docker Compose | Containerização (local e produção) |
| GitHub Actions | CI (testes) + CD (build de imagem → ghcr.io → deploy na VM) |
| GitHub Container Registry | Registry da imagem de produção |
| Nginx | Reverse proxy + terminação TLS + rate limiting |
| Let's Encrypt | Certificado HTTPS com renovação automática |
| Oracle Cloud (Always Free) | VM de produção |

## 📦 Pré-requisitos

- Java 21+
- Docker & Docker Compose
- Google AI Studio API Key (Gemini)

## ⚙️ Como Rodar Localmente

### 1. Clone o repositório
```bash
git clone https://github.com/MrRenan/MyFitnessPartner.git
cd MyFitnessPartner
```

### 2. Suba os serviços com Docker Compose
```bash
docker-compose up -d
```
Isso sobe **PostgreSQL** e **Redis** automaticamente.

### 3. Configure as variáveis no `application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myfitnesspartner
    username: postgres
    password: postgres
  ai:
    openai:
      api-key: SUA_GEMINI_API_KEY
      base-url: https://generativelanguage.googleapis.com/v1beta/openai
      chat:
        options:
          model: gemini-2.5-flash
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: sua-chave-secreta-minimo-256-bits
  expiration: 86400000
```

### 4. Execute a aplicação
```bash
./gradlew bootRun
```

A API estará disponível em: `http://localhost:8080/api`

Documentação Swagger: `http://localhost:8080/api/swagger-ui.html`

### 5. Acesse o chat web
Abra `http://localhost:8080/api/index.html` no navegador — a interface de chat consome os endpoints REST.

## ☁️ Deploy em Produção

A aplicação roda em produção numa VM **Oracle Cloud (Always Free)**, com deploy automatizado.

### Fluxo de CI/CD

```
git push origin main
      │
      ├─► Java CI (gradle.yml)          → compila + testes unitários
      │
      └─► Build & Deploy (deploy.yml)
             ├─ compila o JAR (bootJar)
             ├─ builda a imagem Docker
             ├─ publica em ghcr.io/mrrenan/myfitnesspartner:latest
             └─ conecta na VM via SSH → docker compose pull + up
```

O build acontece nos runners do GitHub (não na VM), e a VM só faz `pull` da imagem pronta.

### Topologia na VM

```
Internet ──► Nginx (443/TLS) ──► app:8080 (rede interna Docker)
                 │                      │
                 │                      ├─► postgres (interno)
          Let's Encrypt                └─► redis (interno)
          rate limiting
          /actuator bloqueado
```

- **Nginx** é o único serviço exposto (portas 80/443). Faz terminação TLS, rate limiting e bloqueia o `/actuator` externamente.
- **app, postgres, redis** ficam na rede interna do Docker, sem portas publicadas.
- Firewall (iptables + Security List da Oracle) permite apenas **22, 80 e 443**.

### Configuração (produção)

Toda configuração sensível vem de variáveis de ambiente (ver `.env.prod.example`):
`POSTGRES_*`, `GEMINI_API_KEY`, `JWT_SECRET`. O arquivo `.env` com valores reais **nunca** é versionado.

```bash
# Na VM, após copiar .env.prod.example para .env e preencher:
docker compose -f docker-compose.prod.yml up -d

# Primeira emissão do certificado HTTPS (uma vez):
./nginx/init-letsencrypt.sh
```

## 🔒 Segurança

- **HTTPS** obrigatório (Let's Encrypt, renovação automática via certbot)
- **JWT** em todos os endpoints de dados; apenas `/auth` e `/onboarding` são públicos (necessário para login/cadastro)
- **Rate limiting** no Nginx nos endpoints de IA — protege a cota do Gemini contra abuso
- **Actuator** acessível apenas internamente (bloqueado pelo Nginx)
- **Secrets** fora do versionamento (env vars); senhas com BCrypt
- **Rede** minimalista: banco e cache sem exposição externa

## 🧪 Testes

```bash
# Todos os testes
./gradlew test

# Apenas unitários (sem Docker)
./gradlew unitTest

# Apenas integração (requer Docker)
./gradlew integrationTest

# Relatório de cobertura
./gradlew test jacocoTestReport
# Relatório em: build/reports/jacoco/test/html/index.html
```

## 📡 Endpoints Principais

### Autenticação
```
POST /api/auth/register   → Cadastro com geração de JWT
POST /api/auth/login      → Login com geração de JWT
POST /api/auth/logout     → Logout (invalida token no Redis)
```

### Chat & Conversas
```
POST /api/conversations/chat      → Envia mensagem para IA
GET  /api/conversations/history   → Histórico de conversas
GET  /api/conversations/last      → Última conversa
```

### Refeições
```
POST /api/meals                      → Registra refeição manual
POST /api/meals/from-description     → Registra via descrição (IA calcula calorias)
GET  /api/meals                      → Lista todas as refeições
GET  /api/meals/today                → Refeições de hoje
```

### Metas Diárias
```
GET  /api/daily-goals/today     → Meta calórica de hoje
GET  /api/daily-goals/history   → Histórico dos últimos N dias
POST /api/daily-goals/reset     → Reseta contagem do dia
```

### IA
```
POST /api/ai/calculate-calories   → Calcula calorias de uma descrição
```

> **Públicos** (sem token): `/auth/**`, `/onboarding/**`, `/health`, Swagger.
> **Protegidos** (requerem `Authorization: Bearer <token>`): `/conversations/**`, `/meals/**`, `/daily-goals/**`, `/users/**`, `/ai/**`.
> O token é obtido ao concluir o onboarding ou fazer login.

## 📁 Estrutura do Projeto

```
src/main/java/io/github/mrrenan/myfitnesspartner/
├── application/
│   ├── port/out/
│   │   └── FitnessAiPort.java          # Porta de saída para IA
│   └── service/
│       ├── AuthService.java
│       ├── ConversationService.java
│       ├── DailyGoalService.java
│       ├── MealService.java
│       └── UserService.java
├── domain/
│   ├── model/                          # Entidades JPA
│   ├── repository/                     # Interfaces de repositório
│   └── exception/                      # Exceções de domínio
├── infrastructure/
│   ├── ai/
│   │   └── SpringAiAdapter.java        # Implementa FitnessAiPort via Spring AI
│   ├── config/                         # Configurações Spring
│   └── security/
│       ├── JwtService.java             # Geração e validação JWT + blacklist Redis
│       ├── JwtAuthenticationFilter.java
│       └── SecurityConfig.java
└── presentation/
    ├── controller/                      # REST Controllers
    ├── dto/                             # Request/Response DTOs
    └── mapper/                          # Entity ↔ DTO mappers
```

## 🗺️ Roadmap

### ✅ Fase 1 — MVP (Concluída)
- [x] Autenticação JWT com blacklist em Redis
- [x] CRUD de usuários com cálculo automático de metas calóricas (TMB/TDEE)
- [x] Registro de refeições com cálculo de calorias via IA
- [x] Acompanhamento de metas diárias
- [x] Histórico de conversas com contexto para a IA
- [x] Onboarding conversacional (a IA coleta os dados e cria o perfil)
- [x] Integração Spring AI + Google Gemini
- [x] Chat web consumindo a API REST
- [x] Testes unitários e de integração com Testcontainers

### ✅ Fase 2 — Produção & Segurança (Concluída)
- [x] Deploy em VM (Oracle Cloud Always Free) via Docker Compose
- [x] Pipeline CI/CD: GitHub Actions builda a imagem, publica no ghcr.io e faz deploy na VM via SSH
- [x] HTTPS com Nginx + Let's Encrypt (renovação automática)
- [x] Rate limiting no Nginx (protege a cota da IA contra abuso)
- [x] Endpoints de dados protegidos por JWT; actuator bloqueado externamente
- [x] Hardening de rede (só 22/80/443 expostas; banco e cache isolados)
- [x] Monitoramento via Spring Boot Actuator (health + métricas)

### 📅 Fase 3 — Evolução (Planejada / dívidas conhecidas)
- [ ] Migração de schema com Flyway/Liquibase (hoje usa `ddl-auto: update`)
- [ ] Rate limiting por usuário autenticado (hoje é por IP no Nginx)
- [ ] Dashboards visuais (Prometheus + Grafana) — requer VM com mais RAM
- [ ] Telas de perfil, histórico e relatórios no frontend
- [ ] Integração WhatsApp (depende de acesso à Meta Business API)

## 👤 Autor

**Renan de Carli Leite**
- GitHub: [@MrRenan](https://github.com/MrRenan)
- LinkedIn: [renan-leite-74a81076](https://www.linkedin.com/in/renan-leite-74a81076/)
- Portfolio: [renanleite.dev.br](https://renanleite.dev.br)

---

⭐ Se este projeto te ajudou de alguma forma, considere dar uma estrela!