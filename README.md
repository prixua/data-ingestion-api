# Data Ingestion API

API Spring Boot para importação dinâmica de arquivos CSV e busca nos dados via MongoDB.

## Pré-requisitos

- Java 21
- Docker e Docker Compose
- (Opcional) MongoDB local na porta `27017`

---

## Rodar localmente

### 1. Subir o MongoDB com Docker Compose

```bash
docker compose up mongodb -d
```

### 2. Criar o arquivo `.env` na raiz do projeto

```env
MONGODB_URI=mongodb://localhost:27017/data_ingestion
```

### 3. Executar a aplicação

```bash
./gradlew bootRun
```

A API ficará disponível em `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Rodar tudo com Docker Compose (app + MongoDB)

```bash
docker compose up --build
```

---

## Build e push da imagem no Docker Hub

### 1. Fazer o build da imagem

```bash
docker build -t prixua/data-ingestion-api:0.0.1 .
```

### 2. Login no Docker Hub

```bash
docker login
```

### 3. Push da imagem

```bash
docker push prixua/data-ingestion-api:0.0.1
```

---

## Executar a partir da imagem publicada

Apenas o arquivo `docker-compose.prod.yml` é necessário — não precisa clonar o repositório.

### 1. Obter o arquivo `docker-compose.prod.yml`

**Opção A — via curl (somente se o repositório estiver público no GitHub):**

```bash
curl -O https://raw.githubusercontent.com/prixua/data-ingestion-api/main/docker-compose.prod.yml
```

> Se retornar erro 404, o repositório ainda não está publicado. Use a Opção B.

**Opção B — criar o arquivo manualmente:**

Crie um arquivo chamado `docker-compose.prod.yml` com o seguinte conteúdo:

```yaml
services:
  mongodb:
    image: mongo:7.0
    container_name: data-ingestion-mongodb
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: data_ingestion
    volumes:
      - mongodb_data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: prixua/data-ingestion-api:latest
    container_name: data-ingestion-api
    ports:
      - "8080:8080"
      - "9090:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      MONGODB_URI: mongodb://mongodb:27017/data_ingestion
    depends_on:
      mongodb:
        condition: service_healthy

volumes:
  mongodb_data:
```

### 2. Executar

```bash
docker compose -f docker-compose.prod.yml up -d
```

O Docker vai baixar automaticamente a imagem do Docker Hub, subir o MongoDB e iniciar a API.

| Serviço | URL |
|---------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| MongoDB (cliente externo) | `localhost:27017` |
| Actuator / Prometheus | `http://localhost:9090/actuator/prometheus` |

### Usar uma imagem específica

```bash
DOCKER_IMAGE=prixua/data-ingestion-api:0.0.1 docker compose -f docker-compose.prod.yml up -d
```

### Parar e remover

```bash
docker compose -f docker-compose.prod.yml down
```

### Limpar os dados do MongoDB

Para apagar todos os dados persistidos e começar do zero:

```bash
# Para os containers e remove o volume
docker compose -f docker-compose.prod.yml down -v
```

Ou, se quiser remover apenas o volume sem parar os containers:

```bash
# Para os containers primeiro
docker compose -f docker-compose.prod.yml down

# Remove o volume nomeado
docker volume rm data-ingestion-api_mongodb_data
```

> **Atenção:** essa operação é irreversível — todos os datasets importados serão perdidos.

---

## Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/ingest?dataset={name}` | Importa um arquivo CSV |
| `GET` | `/api/v1/search?dataset={name}&field={col}&value={val}` | Busca registros com filtro |

### Métricas (Prometheus)

```
GET /actuator/prometheus
```
