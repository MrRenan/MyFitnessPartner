# 🏋️ MyFitnessPartner

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.5+-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> Seu parceiro fitness pessoal via WhatsApp, powered by AI

## 📋 Sobre o Projeto

MyFitnessPartner é uma aplicação backend que integra WhatsApp com Inteligência Artificial para auxiliar usuários em sua jornada fitness. Através de conversas naturais pelo WhatsApp, os usuários podem:

- 🍽️ Calcular calorias de refeições (texto ou foto)
- 📊 Acompanhar progresso diário e semanal
- 💪 Receber orientações sobre treinos
- 🎯 Definir e monitorar metas de saúde
- 📈 Visualizar histórico e relatórios personalizados

Este projeto demonstra a implementação de uma arquitetura moderna e escalável utilizando as melhores práticas do ecossistema Java/Spring.

## 🏗️ Arquitetura

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   WhatsApp  │◄────►│  MyFitness API   │◄────►│  AI Service │
│   (Twilio)  │      │  (Spring Boot)   │      │  (OpenAI)   │
└─────────────┘      └──────────────────┘      └─────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              ┌─────▼──────┐     ┌─────▼──────┐
              │ PostgreSQL │     │  MongoDB   │
              │  (Dados)   │     │ (Conversas)│
              └────────────┘     └────────────┘
```

### Componentes Principais

- **API Layer**: Recebe webhooks do WhatsApp e expõe endpoints REST
- **Service Layer**: Lógica de negócio e orquestração
- **Domain Layer**: Entidades e regras de domínio
- **Infrastructure Layer**: Integrações externas (IA, WhatsApp, Bancos)
- **Messaging Layer**: Processamento assíncrono com filas

## 🚀 Tecnologias

### Core
- **Java 21** - Linguagem base
- **Spring Boot 3.2+** - Framework principal
- **Spring WebFlux** - Programação reativa
- **Gradle 8.5+** - Gerenciamento de dependências

### Banco de Dados
- **PostgreSQL** - Dados estruturados (usuários, metas)
- **MongoDB** - Histórico de conversas
- **Redis** - Cache (planejado)

### Integrações
- **Twilio API** - Integração com WhatsApp
- **OpenAI API** - Processamento de linguagem natural
- **AWS S3** - Armazenamento de imagens (planejado)

### Mensageria
- **RabbitMQ / Amazon SQS** - Processamento assíncrono

### DevOps & Cloud
- **Docker** - Containerização
- **Docker Compose** - Orquestração local
- **AWS** - Deploy em produção (Elastic Beanstalk/ECS)
- **GitHub Actions** - CI/CD

### Testes & Qualidade
- **JUnit 5** - Testes unitários
- **Mockito** - Mocks
- **TestContainers** - Testes de integração
- **SonarQube** - Análise de código

## 📦 Pré-requisitos

- Java 21+
- Docker & Docker Compose
- Gradle 8.5+
- Conta Twilio (para WhatsApp)
- API Key OpenAI ou Google Gemini

## ⚙️ Configuração Local

### 1. Clone o repositório
```bash
git clone https://github.com/MrRenan/MyFitnessPartner.git
cd MyFitnessPartner
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Twilio
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_WHATSAPP_NUMBER=whatsapp:+14155238886

# OpenAI
OPENAI_API_KEY=your_openai_api_key

# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=myfitnesspartner
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_DB=myfitnesspartner
```

### 3. Inicie os serviços com Docker Compose

```bash
docker-compose up -d
```

### 4. Execute a aplicação

```bash
./gradlew bootRun
```

A aplicação estará disponível em: `http://localhost:8080`

## 🧪 Executando Testes

```bash
# Todos os testes
./gradlew test

# Com relatório de cobertura
./gradlew test jacocoTestReport

# Testes de integração
./gradlew integrationTest
```

## 📝 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/renan/myfitnesspartner/
│   │       ├── application/          # Casos de uso
│   │       ├── domain/               # Entidades e regras de negócio
│   │       ├── infrastructure/       # Implementações técnicas
│   │       │   ├── messaging/        # RabbitMQ/SQS
│   │       │   ├── persistence/      # Repositories
│   │       │   ├── ai/               # Cliente OpenAI
│   │       │   └── whatsapp/         # Cliente Twilio
│   │       └── presentation/         # Controllers REST
│   └── resources/
│       ├── application.yml           # Configuração principal
│       ├── application-dev.yml       # Profile dev
│       └── application-prod.yml      # Profile prod
└── test/
    ├── java/
    │   └── com/renan/myfitnesspartner/
    │       ├── unit/                 # Testes unitários
    │       └── integration/          # Testes de integração
    └── resources/
```

## 🗺️ Roadmap

### ✅ Fase 1 - MVP (Em Desenvolvimento)
- [x] Setup inicial do projeto
- [ ] Integração WhatsApp (Twilio)
- [ ] Integração com IA
- [ ] Cálculo de calorias por texto
- [ ] Persistência básica de dados

### 🚧 Fase 2 - Enriquecimento
- [ ] Sistema de mensageria assíncrona
- [ ] Gestão completa de usuários
- [ ] Histórico e relatórios
- [ ] Suporte a consultas sobre treinos

### 📅 Fase 3 - Cloud & Observabilidade
- [ ] Deploy AWS
- [ ] Análise de imagens de refeições
- [ ] Logs e métricas
- [ ] Alertas e monitoring

### 🎯 Fase 4 - Features Avançadas
- [ ] Cache com Redis
- [ ] Sistema de lembretes
- [ ] Dashboard web
- [ ] Gamificação

## 🤝 Como Contribuir

Este é um projeto de portfólio, mas sugestões e feedbacks são bem-vindos!

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👤 Autor

**Renan Leite**
- GitHub: [@MrRenan](https://github.com/MrRenan)
- LinkedIn: https://www.linkedin.com/in/renan-leite-74a81076/

Tem alguma dúvida ou sugestão? Entre em contato!

---

⭐ Se este projeto te ajudou de alguma forma, considere dar uma estrela no repositório!
