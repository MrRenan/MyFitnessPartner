# 🏋️ MyFitnessPartner

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-6db33f.svg)](https://spring.io/projects/spring-ai)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-brightgreen.svg)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> Assistente fitness pessoal via WhatsApp, powered by Google Gemini AI

## 📋 Sobre o Projeto

MyFitnessPartner é uma API backend que integra **WhatsApp com Inteligência Artificial** para auxiliar usuários em sua jornada fitness. Através de conversas naturais pelo WhatsApp, os usuários podem calcular calorias de refeições, acompanhar metas diárias e receber orientações personalizadas.

O projeto demonstra a implementação de uma arquitetura moderna utilizando as melhores práticas do ecossistema Java/Spring, incluindo **Spring AI**, **JWT com blacklist em Redis**, **Testcontainers** e integração real com WhatsApp via Twilio.

## 🏗️ Arquitetura

```
                    ┌─────────────────────────────────────┐
                    │         MyFitnessPartner API         │
                    │                                     │
 WhatsApp  ◄──────► │  ┌──────────┐    ┌──────────────┐  │
 (Twilio)           │  │ Webhook  │    │  REST API    │  │
                    │  │ Handler  │    │  Controllers │  │
                    │  └────┬─────┘    └──────┬───────┘  │
                    │       │                 │           │
                    │  ┌────▼─────────────────▼───────┐  │
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
- **Processamento assíncrono** — webhook retorna 200 imediatamente, IA processa em background via `@Async`

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
| Google Gemini 2.0 Flash | LLM para análise nutricional e chat |
| Twilio WhatsApp | Envio e recebimento de mensagens |

### Testes & Qualidade
| Tecnologia | Uso |
|---|---|
| JUnit 5 + Mockito | Testes unitários |
| Testcontainers | Testes de integração com PostgreSQL e Redis reais |
| JaCoCo | Cobertura de código |

### DevOps
| Tecnologia | Uso |
|---|---|
| Docker + Docker Compose | Containerização local |
| GitHub Actions | CI/CD |

## 📦 Pré-requisitos

- Java 21+
- Docker & Docker Compose
- Conta Twilio com sandbox WhatsApp
- Google AI Studio API Key (Gemini)
- ngrok (para desenvolvimento local com webhook)

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
          model: gemini-2.0-flash
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: sua-chave-secreta-minimo-256-bits
  expiration: 86400000

twilio:
  account-sid: SEU_ACCOUNT_SID
  auth-token: SEU_AUTH_TOKEN
  whatsapp-number: whatsapp:+14155238886
```

### 4. Execute a aplicação
```bash
./gradlew bootRun
```

A API estará disponível em: `http://localhost:8080/api`

Documentação Swagger: `http://localhost:8080/api/swagger-ui.html`

### 5. Configure o webhook (WhatsApp)
```bash
# Em outro terminal
ngrok http 8080

# Configure no Twilio Sandbox Settings:
# https://SEU_NGROK.ngrok-free.app/api/webhook/whatsapp
```

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

> Todos os endpoints (exceto `/auth/**`, `/health` e `/webhook/**`) requerem `Authorization: Bearer <token>`

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
│   ├── security/
│   │   ├── JwtService.java             # Geração e validação JWT + blacklist Redis
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   └── whatsapp/
│       ├── WhatsAppWebhookController.java
│       ├── WhatsAppWebhookHandler.java  # Processamento assíncrono
│       └── WhatsAppMessageSender.java   # Envio via Twilio SDK
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
- [x] Integração Spring AI + Google Gemini
- [x] Webhook WhatsApp via Twilio
- [x] Testes unitários e de integração com Testcontainers

### 🚧 Fase 2 — Escalabilidade (Planejada)
- [ ] RabbitMQ para processamento assíncrono de mensagens
- [ ] Cache Redis para consultas frequentes
- [ ] Rate limiting por usuário

### 📅 Fase 3 — Cloud & Observabilidade (Planejada)
- [ ] Deploy AWS (ECS + RDS)
- [ ] Distributed tracing com OpenTelemetry
- [ ] Métricas com Prometheus + Grafana

## 👤 Autor

**Renan de Carli Leite**
- GitHub: [@MrRenan](https://github.com/MrRenan)
- LinkedIn: [renan-leite-74a81076](https://www.linkedin.com/in/renan-leite-74a81076/)
- Portfolio: [renanleite.dev.br](https://renanleite.dev.br)

---

⭐ Se este projeto te ajudou de alguma forma, considere dar uma estrela!