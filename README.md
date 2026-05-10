# BART Realtime Pipeline

A real-time transit event pipeline built to practice Kubernetes, Kafka, and Redis.

```
Python CLI → Spring Boot → Kafka → Redis
```

Events flow from a Python CLI that generates BART transit data, through a Java Spring Boot service, into Kafka as an event stream, and finally cached in Redis for low-latency lookups.

---

## Architecture

```
┌─────────────┐     HTTP POST      ┌──────────────────┐
│  Python CLI │ ─────────────────► │  Spring Boot API  │
│  (pod)      │                    │  (pod)            │
└─────────────┘                    └────────┬─────────┘
                                            │ Kafka Producer
                                            ▼
                                   ┌──────────────────┐
                                   │      Kafka        │
                                   │  bart-events      │
                                   │  (pod)            │
                                   └────────┬─────────┘
                                            │ Kafka Consumer
                                            ▼
                                   ┌──────────────────┐
                                   │      Redis        │
                                   │  (pod)            │
                                   └──────────────────┘
                                            ▲
                                   GET /api/bart/station/{name}
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Event generator | Python 3 |
| API service | Java 21 + Spring Boot 3.2 |
| Event streaming | Apache Kafka |
| Caching | Redis 7 |
| Container orchestration | Kubernetes (minikube) |
| Build tool | Maven |

---

## Project Structure

```
bart-realtime-pipeline/
  ├── python-cli/
  │     ├── main.py                # Generates BART events every 2 seconds
  │     ├── requirements.txt
  │     └── Dockerfile
  ├── spring-boot-service/
  │     ├── src/main/java/com/bart/service/
  │     │     ├── BartServiceApplication.java   # Entry point
  │     │     ├── BartEvent.java                # Event model
  │     │     ├── BartController.java           # REST endpoints
  │     │     ├── KafkaProducerService.java     # Publishes to Kafka
  │     │     ├── KafkaConsumerService.java     # Consumes from Kafka
  │     │     └── RedisService.java             # Caches to Redis
  │     ├── src/main/resources/
  │     │     └── application.properties
  │     ├── pom.xml
  │     └── Dockerfile
  ├── bart-k8s/
  │     ├── deploy.sh                # One-command minikube deploy
  │     └── k8s/
  │           ├── kafka.yaml
  │           ├── redis.yaml
  │           ├── spring-boot.yaml
  │           └── python-cli.yaml
  └── README.md
```

---

## Prerequisites

Make sure you have these installed:

| Tool | Version | Install |
|---|---|---|
| Docker Desktop | Latest | https://www.docker.com/products/docker-desktop |
| minikube | Latest | https://minikube.sigs.k8s.io/docs/start/ |
| kubectl | Latest | https://kubernetes.io/docs/tasks/tools/ |
| Java JDK | 21+ | https://adoptium.net/ |
| Maven | 3.9+ | `brew install maven` |
| Python | 3.11+ | https://www.python.org/downloads/ |

---

## Option 1 — Run on Kubernetes (minikube)

### Quick start (one command)
```bash
./bart-k8s/deploy.sh
```

This starts minikube if needed, builds both Docker images inside minikube's daemon, applies all manifests, waits for every deployment to roll out, and prints the service URL.

### Manual steps (if you prefer step-by-step)

#### 1. Start minikube
```bash
minikube start
```

#### 2. Point Docker to minikube
```bash
eval $(minikube docker-env)
```
> ⚠️ Run this in every terminal tab you use. It only applies to the current tab.

#### 3. Build Docker images inside minikube
```bash
docker build -t bart-service:latest ./spring-boot-service
docker build -t bart-python-cli:latest ./python-cli
```

Verify both images exist:
```bash
docker images | grep bart
```

#### 4. Deploy everything
```bash
kubectl apply -f bart-k8s/k8s/
```

#### 5. Wait for all pods to be Running
```bash
kubectl get pods -w
```

Expected output:
```
NAME                            READY   STATUS    RESTARTS   AGE
bart-service-xxx                1/1     Running   0          30s
kafka-xxx                       1/1     Running   0          35s
python-cli-xxx                  1/1     Running   0          20s
redis-xxx                       1/1     Running   0          40s
```

#### 6. Open the tunnel (keep this terminal open)
```bash
minikube service bart-service --url
```

Copy the URL (e.g. `http://127.0.0.1:50773`)

### Test it
```bash
# Health check
curl http://127.0.0.1:50773/api/bart/health

# Query a station (wait ~10 seconds for events to flow through first)
curl "http://127.0.0.1:50773/api/bart/station/Powell%20St"
curl "http://127.0.0.1:50773/api/bart/station/Montgomery%20St"
curl "http://127.0.0.1:50773/api/bart/station/Civic%20Center"
curl "http://127.0.0.1:50773/api/bart/station/16th%20St%20Mission"
curl "http://127.0.0.1:50773/api/bart/station/24th%20St%20Mission"
```

Expected response:
```json
{"station":"Powell St","line":"Blue","minutes":2,"status":"delayed"}
```

---

## Option 2 — Run Locally (no Kubernetes)

### 1. Start Kafka
```bash
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  apache/kafka:latest
```

### 2. Start Redis
```bash
docker run -d --name redis -p 6379:6379 redis:7
```

### 3. Start Spring Boot
```bash
cd spring-boot-service
mvn spring-boot:run
```

### 4. Run the Python CLI
```bash
cd python-cli
pip install -r requirements.txt
python main.py
```

### 5. Test it
```bash
curl http://localhost:8080/api/bart/health
curl "http://localhost:8080/api/bart/station/Powell%20St"
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/bart/health` | Health check |
| POST | `/api/bart/events` | Receive a BART event |
| GET | `/api/bart/station/{name}` | Get latest cached status for a station |

### Example POST payload
```json
{
  "station": "Montgomery St",
  "line": "Red",
  "destination": "Richmond",
  "minutes": 3,
  "status": "on time",
  "timestamp": "2026-05-09T10:30:00"
}
```

---

## Useful kubectl Commands

```bash
# See all pods
kubectl get pods

# Stream logs from Spring Boot
kubectl logs deployment/bart-service -f

# Stream logs from Python CLI
kubectl logs deployment/python-cli -f

# Scale Spring Boot to 2 replicas
kubectl scale deployment bart-service --replicas=2

# Check resource usage
kubectl top pods

# Exec into a pod
kubectl exec -it deployment/bart-service -- sh

# Delete everything
kubectl delete -f bart-k8s/k8s/
```

---

## How It Works

1. **Python CLI** generates fake BART transit events every 2 seconds and sends them via HTTP POST to Spring Boot
2. **Spring Boot** receives the events, logs them, and publishes each one to the `bart-events` Kafka topic using the station name as the message key
3. **Kafka** holds the event stream — acts as the decoupled backbone between producer and consumer
4. **Spring Boot Kafka Consumer** listens to `bart-events` and writes the latest event per station into Redis
5. **Redis** caches the latest status for each station — `GET /api/bart/station/{name}` reads directly from here for fast lookups

---

## Troubleshooting

**Pods stuck in Pending:**
```bash
kubectl describe pod <pod-name>
# Look for "Insufficient memory" or "No nodes available"
minikube start --memory=4096
```

**Python CLI showing connection errors:**
```bash
# Make sure SPRING_BOOT_URL env var is set
kubectl exec deployment/python-cli -- env | grep SPRING
# Should show: SPRING_BOOT_URL=http://bart-service:8080/api/bart/events
```

**Images not found in minikube:**
```bash
# Always run this before docker build
eval $(minikube docker-env)
docker images | grep bart
```

**Station returning empty:**
```bash
# Wait 10-15 seconds for events to flow through Kafka into Redis
# Then check Spring Boot logs
kubectl logs deployment/bart-service | grep "Cached in Redis"
```