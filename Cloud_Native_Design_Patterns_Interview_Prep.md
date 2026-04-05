# Design Patterns for Cloud-Native Applications — Interview Preparation Guide

> **Book:** *Design Patterns for Cloud Native Applications* by Kasun Indrasiri & Sriskandarajah Suhothayan (O'Reilly, 2021)
> **Purpose:** Comprehensive interview preparation covering all major topics with explanations, examples, and key points.

---

## Chapter 1: Introduction to Cloud Native

### What Is Cloud Native?

Cloud native is an architectural approach for building software as a collection of **independent, loosely coupled, business-capability-oriented microservices** that run in dynamic environments (public, private, hybrid, multi-cloud) in an automated, scalable, resilient, manageable, and observable way.

**Key pillars of cloud native:**
- Designed as a collection of microservices
- Containerization and container orchestration (Kubernetes)
- Automated CI/CD development life cycle
- Dynamic management with control planes
- Observability and monitoring

**Interview Tip:** When asked "What is cloud native?", emphasize the 4 pillars: microservices, containers, automation (CI/CD), and dynamic management.

---

### Microservices Design Principles

A microservice is a small, autonomous service that is responsible for a single business capability. Key design principles include:

- **Single Responsibility:** Each service owns one bounded context (e.g., Order service, Payment service)
- **Independent Deployment:** Services can be deployed, scaled, and updated independently
- **Decentralized Data:** Each microservice has its own dedicated data store (no shared databases)
- **API-based Communication:** Services expose APIs; no direct database-to-database access

**Example:** In an e-commerce application:
- `ProductCatalog` service → owns product data, exposed via REST API
- `OrderManagement` service → owns order data, talks to ProductCatalog via API (never via direct DB access)
- `Payment` service → owns transaction data, triggered asynchronously via message queue

**Interview Question:** *"Why should microservices have separate databases?"*
> Because shared databases create tight coupling — one team's schema change can break another team's service. Independent databases allow each service to be scaled, deployed, and evolved independently.

---

### Containerization & Kubernetes

- **Container:** A lightweight, self-contained executable unit packaging app + all its dependencies. Ensures "works on my machine = works everywhere."
- **Container Orchestration (Kubernetes):** Manages deployment, scaling, networking, and self-healing of containers.
- **Key Kubernetes objects:** Pod, Deployment, Service, ConfigMap, Ingress

**Example:** A Dockerfile for a Java Spring Boot microservice packages the JAR + JDK. Kubernetes then schedules, auto-scales, and restarts the container if it crashes.

---

## Chapter 2: Communication Patterns

### Synchronous Messaging Patterns

#### 1. Request-Response Pattern

**Definition:** One microservice sends a request and **blocks until** it receives a response from another microservice.

**How it works:**
1. Client sends HTTP/gRPC request
2. Server processes it
3. Server sends back a response
4. Client resumes execution

**When to use:**
- Interactive, real-time communication
- External-facing APIs
- Business logic depends on an immediate response

**When NOT to use:**
- Long-running background tasks
- When high throughput with no blocking is needed

**Example:** A user calls `GET /products/123` on the Product Catalog service — the browser waits for the response before rendering the page.

```
Mobile App → [REST GET /order/42] → OrderService → [Response: {orderId:42, status: "Shipped"}]
```

**Interview Tip:** Point out the coupling risk — chaining too many synchronous calls creates a "chain of dependent services" where one slow service causes cascading delays.

---

#### 2. Remote Procedure Calls (RPC) Pattern — gRPC

**Definition:** Distributed services invoke procedures (functions) on remote services as if they were local function calls. The underlying framework handles serialization, network transport, and deserialization.

**How gRPC works:**
1. Define service interface in **Protobuf** (IDL — Interface Definition Language)
2. Generate client/server stubs
3. Client calls the stub as a local function
4. gRPC framework serializes call over **HTTP/2** (binary, compressed)
5. Server stub deserializes, executes, and returns

**Advantages over REST:**
| Feature | gRPC | REST |
|---|---|---|
| Protocol | HTTP/2 (binary) | HTTP/1.1 (text) |
| Contract | Mandatory (Protobuf) | Optional (OpenAPI) |
| Performance | High — binary serialization | Lower — JSON is verbose |
| Streaming | Bi-directional streaming | No native support |
| Browser support | Limited | Excellent |

**When to use:** Internal microservice-to-microservice communication needing high performance.

**When NOT to use:** APIs exposed directly to web/mobile clients.

**Example:**
```
// Protobuf definition
service OrderService {
  rpc GetOrder (OrderRequest) returns (OrderResponse);
}

// Java client calls it like a local method:
OrderResponse resp = orderStub.getOrder(OrderRequest.newBuilder().setOrderId(42).build());
```

---

### Asynchronous Messaging Patterns

#### 3. Single-Receiver (Point-to-Point) Pattern

**Definition:** One producer sends a message to exactly **one consumer** via a **queue** in a message broker.

**How it works:**
- Producer puts a message in a queue
- Only one consumer reads and processes that message
- Guarantees ordered delivery and at-least-once delivery

**When to use:** Fire-and-forget commands where only one service should act on the message.

**Example:**
```
CheckoutService → [Queue: order-requests] → OrderManagementService
```
The Checkout service places an order message in the queue and doesn't wait. The OrderManagement service processes it independently.

**Technologies:** RabbitMQ, Apache ActiveMQ, Azure Service Bus

---

#### 4. Multiple-Receiver (Publisher-Subscriber) Pattern

**Definition:** One producer publishes a message to a **topic**, and **multiple subscribers** each receive a copy.

**How it works:**
- Producer publishes to a topic/exchange
- Multiple consumers subscribed to that topic all receive the message
- No guaranteed delivery to offline consumers (unless durable subscriptions are configured)

**When to use:** Event broadcasting — notify many services about the same event.

**Example:**
```
ProductManagementService → [Topic: price-update] → ShoppingCartService
                                                  → FraudDetectionService
                                                  → SubscriptionService
```
All three services independently react to the same price update event.

**Technologies:** Apache Kafka, RabbitMQ (exchanges), Amazon SNS, Azure Event Grid

---

#### 5. Asynchronous Request-Reply Pattern

**Definition:** The producer sends a message via a queue AND expects a reply on a **separate callback queue**.

**How it works:**
1. Producer sends message with `correlationId` + `replyTo` queue info
2. Consumer processes and sends reply to the `replyTo` queue
3. Producer polls/subscribes to the reply queue and correlates the response

**When to use:** When you need an eventual reply but don't want to block the thread (decoupled request-reply).

**Key difference vs. Request-Response:** No open HTTP connection — both directions are queue-based.

**Example:**
```
CheckoutService → [order-requests queue, replyTo: orderprocreply, correlationId: abc123]
             ← [orderprocreply queue, correlationId: abc123, status: "processed"]
```

---

### Service Definition Patterns

#### 6. OpenAPI / Protobuf / AsyncAPI

Service definitions are contracts describing how to interact with a service.

| Communication Style | Schema Format | Registry |
|---|---|---|
| REST (synchronous) | OpenAPI (Swagger) | Consul, etcd |
| gRPC (synchronous) | Protocol Buffers | Consul |
| Async messaging | Apache Avro / JSON Schema / AsyncAPI | Kafka Schema Registry |

**Interview Tip:** Always mention that service definitions enable contract-first development, tooling for code generation, and schema validation at runtime.

---

### Technologies for Communication

| Technology | Type | Use Case |
|---|---|---|
| RESTful HTTP | Sync | External APIs, CRUD-style services |
| GraphQL | Sync | Flexible queries, BFF pattern |
| WebSocket | Sync/Duplex | Real-time bidirectional (chat, dashboards) |
| gRPC | Sync RPC | Internal high-performance microservice calls |
| AMQP (RabbitMQ) | Async | Queue-based reliable messaging |
| Apache Kafka | Async | High-throughput event streaming |
| NATS | Async | Lightweight pub-sub messaging |

---

## Chapter 3: Connectivity and Composition Patterns

### Connectivity Patterns

#### 7. Service Connectivity Pattern

**Definition:** Establishes how microservices find and connect to each other in a cloud native environment.

**Key elements:**
- Services communicate via well-defined APIs (REST/gRPC)
- Communication goes through load balancers and API gateways
- Legacy/proprietary systems are wrapped in microservice adapters

**Example:** The `InventoryService` calls a proprietary ERP system via a microservice wrapper that translates REST calls into the ERP's native protocol.

---

#### 8. Service Abstraction Pattern

**Definition:** Represents all entities (services, databases, legacy systems, external APIs) as **services** with standardized interfaces. Everything is treated as a service.

**Why it matters:** Decouples consumers from the underlying implementation. A consumer doesn't know if it's calling a microservice or a legacy COTS application — it just calls an API.

**Example:** Wrap a legacy Oracle database behind a RESTful `CustomerDataService` so other services don't need to know the DB schema.

---

#### 9. Service Registry and Discovery Pattern

**Definition:** A central registry that stores information about all available microservices. Consumers query the registry to discover service endpoints dynamically.

**Two approaches:**
- **Client-side discovery:** Client queries registry, then calls the service directly
- **Server-side discovery:** Client calls a load balancer; load balancer queries registry and routes request

**Why needed:** In cloud environments, service IPs change dynamically (containers restart, scale up/down). Hard-coding IPs is not feasible.

**Technologies:** Consul, etcd, Apache ZooKeeper, Kubernetes built-in DNS

**Example:**
```
OrderService startup → registers itself in Consul with IP + port + health endpoint
ShippingService → queries Consul for "OrderService" → gets current IP → calls it
```

---

#### 10. Resilient Connectivity Pattern

**Definition:** Makes inter-service communication fault-tolerant by handling failures gracefully.

**Key resilience techniques:**

| Technique | How it works | Example |
|---|---|---|
| **Timeout** | Abort if no response within N ms | ProductService waits max 200ms for InventoryService |
| **Retry** | Retry failed calls N times with backoff | Retry 3x with 10s intervals on transient network errors |
| **Circuit Breaker** | Stop calling a failing service after threshold; auto-reset after cooldown | After 5 failures, stop calling PaymentService for 30s |
| **Deadline** | Propagate a fixed expiry time across entire call chain | Client sets deadline at T+500ms; all downstream services check it |
| **Fail-fast** | Validate before calling; detect failures early | Check DB connection pool before invoking DB-dependent service |

**Circuit Breaker States:**
- **Closed** → Normal operation
- **Open** → Calls blocked (failure threshold reached)
- **Half-open** → One trial call allowed; resets if it succeeds

**Java Example with Resilience4j:**
```java
CircuitBreaker cb = CircuitBreaker.ofDefaults("paymentService");
Supplier<String> supplier = CircuitBreaker.decorateSupplier(cb, () -> paymentClient.charge(orderId));
String result = Try.ofSupplier(supplier).getOrElse("Payment unavailable");
```

---

#### 11. Sidecar Pattern

**Definition:** Deploy a helper container **alongside** each microservice container (in the same pod) to handle cross-cutting concerns like logging, security, and service mesh proxying.

**How it works:** The sidecar proxy intercepts all inbound/outbound traffic. The microservice talks to localhost; the sidecar handles the network.

**What sidecars can handle:**
- mTLS (mutual TLS for service-to-service security)
- Distributed tracing
- Traffic routing and load balancing
- Health checks

**Example:** Envoy proxy deployed as a sidecar next to each microservice in Istio service mesh.

```
[OrderService container] ←→ [Envoy Sidecar] ←→ Network
```

**Interview Tip:** The sidecar externalizes cross-cutting concerns so the business service code stays clean.

---

#### 12. Service Mesh Pattern

**Definition:** An infrastructure layer that manages all service-to-service communication using sidecar proxies. Provides traffic management, security (mTLS), observability, and resilience without changing application code.

**Components:**
- **Data plane:** Sidecar proxies (Envoy) handle actual traffic
- **Control plane:** Manages and configures all proxies centrally (Istio Pilot)

**Key capabilities:**
- Automatic mTLS between services
- Traffic splitting (canary deployments, A/B testing)
- Distributed tracing across services
- Circuit breaking, retries, timeouts — configured centrally

**Technologies:** Istio, Linkerd, Consul Connect

**Sidecarless Service Mesh:** Newer approach (Cilium, Ambient Mesh) where mesh functionality is implemented in the OS/kernel, avoiding per-pod sidecar overhead.

---

### Service Composition Patterns

#### 13. Service Orchestration Pattern

**Definition:** A central **orchestrator service** controls and calls multiple downstream services in a specific sequence to fulfill a business process.

**How it works:** The orchestrator explicitly invokes Service A, waits for response, then invokes Service B, etc.

**Pros:** Clear centralized logic, easy to debug  
**Cons:** Orchestrator becomes a bottleneck; tight coupling to downstream services

**Example:**
```
OrderOrchestrator:
  1. Call InventoryService.reserve(productId)
  2. Call PaymentService.charge(customerId, amount)
  3. Call ShippingService.schedule(orderId)
  4. Return order confirmation
```

---

#### 14. Service Choreography Pattern

**Definition:** No central coordinator. Each service **reacts to events** and publishes new events to trigger the next step. Services are loosely coupled and autonomous.

**How it works:** Services subscribe to events from an event broker and publish their own events when their work is done.

**Pros:** Highly decoupled, resilient, easy to add new participants  
**Cons:** Harder to debug (no single place to trace the flow); eventual consistency

**Example:**
```
OrderService publishes → [OrderPlaced event]
  → InventoryService listens, reserves stock, publishes → [StockReserved event]
    → PaymentService listens, charges, publishes → [PaymentSucceeded event]
      → ShippingService listens, schedules delivery
```

---

#### 15. Saga Pattern

**Definition:** Manages **distributed transactions** across multiple microservices without a two-phase commit (2PC). Each step has a **compensating transaction** to undo it if a later step fails.

**Two types:**
- **Choreography-based Saga:** Each service publishes events; compensating events roll back on failure
- **Orchestration-based Saga:** A Saga Orchestrator explicitly calls services and triggers compensations

**Why needed:** In microservices, each service has its own DB. You can't use a single ACID transaction. Instead, you coordinate through local transactions + compensations.

**Example — Order Saga (Orchestration):**
```
Step 1: Reserve inventory    → Compensation: Release inventory
Step 2: Charge payment       → Compensation: Refund payment
Step 3: Schedule shipping    → Compensation: Cancel shipping

If Step 3 fails:
  → Cancel shipping (compensation 3)
  → Refund payment (compensation 2)
  → Release inventory (compensation 1)
```

**Technologies:** Apache Camel, Camunda, Axon Framework, Azure Logic Apps

---

## Chapter 4: Data Management Patterns

### Data Stores Overview

| Store Type | Best For | Examples |
|---|---|---|
| **Relational (RDBMS)** | Structured data, ACID transactions, financial systems | PostgreSQL, MySQL, Oracle |
| **Key-Value (NoSQL)** | Session data, caching, simple lookups | Redis, DynamoDB, Memcached |
| **Column Store (NoSQL)** | Big data, time-series, wide rows | Apache Cassandra, Apache HBase |
| **Document Store (NoSQL)** | Semi-structured JSON/XML data, product catalogs | MongoDB, CouchDB |
| **Graph Store (NoSQL)** | Networks, relationships, fraud detection | Neo4j, Azure Cosmos DB |
| **Filesystem / Object Store** | Unstructured data (images, videos, files) | Amazon S3, Azure Blob, HDFS |

**CAP Theorem:** A distributed system can guarantee at most two of: **Consistency**, **Availability**, **Partition tolerance**.

| Store | Favors |
|---|---|
| Redis, MongoDB | Consistency over Availability |
| Apache Cassandra, CouchDB | Availability over Consistency |
| DynamoDB, Cosmos DB | Tunable (both possible) |

---

### Data Management Approaches

| Approach | Description | When to Use |
|---|---|---|
| **Centralized** | Single database for all services | Legacy monoliths (anti-pattern in microservices) |
| **Decentralized** | Each microservice has its own DB | Preferred in microservices — independent scaling |
| **Hybrid** | Services in same bounded context share a DB | Small/medium organizations; same owning team |

**Key rule:** Services must never directly access another service's database. Data is shared only via APIs.

---

### Data Composition Patterns

#### 16. Data Service Pattern

**Definition:** Exposes data from a database as a **dedicated service API** rather than allowing direct DB access.

**When to use:**
- Data that doesn't belong to any single microservice
- Abstracting a legacy/proprietary database

**Example:**
```
DiscountService exposes:
  GET /discounts/{productId}

Both OrderService and ProductDetailService call this API.
Neither accesses the discount table directly.
```

---

#### 17. Composite Data Services Pattern (Server-Side Mashup)

**Definition:** Aggregates data from **multiple data services** and returns a combined response. Eliminates duplicate data composition by multiple consumers.

**Example:**
```
InventoryAggregatorService:
  → Calls FulfillmentCenterA.getStock()
  → Calls FulfillmentCenterB.getStock()
  → Calls FulfillmentCenterC.getStock()
  → Returns combined inventory count (cached for performance)
```

---

#### 18. Client-Side Mashup Pattern

**Definition:** The **client** (browser/mobile app) calls multiple APIs and combines the data itself.

**When to use:** When asynchronous data loading is acceptable (e.g., a dashboard where widgets load independently).

**Example:** A retail dashboard where the left panel loads from ProductAPI and the right panel from OrderAPI — browser renders both independently.

---

### Data Scaling Patterns

#### 19. Data Sharding Pattern

**Definition:** Splits a large dataset across multiple database nodes (shards) based on a shard key so each node holds only a subset of the data.

**Sharding strategies:**
- **Range-based sharding:** Users A–M in Shard 1, N–Z in Shard 2
- **Hash-based sharding:** `hash(customerId) % N` determines the shard
- **Directory-based sharding:** A lookup table maps keys to shards

**Example:**
```
OrderService shards by customer region:
  Shard 1: Orders from India
  Shard 2: Orders from US
  Shard 3: Orders from Europe
```

**Challenge:** Cross-shard queries are expensive. Choose the shard key carefully based on most-frequent query patterns.

---

#### 20. Command and Query Responsibility Segregation (CQRS) Pattern

**Definition:** Separates the **write model (Command)** from the **read model (Query)**. Different data stores are optimized for reads vs. writes.

**How it works:**
- Command side: RDBMS (for ACID, consistency)
- Query side: NoSQL/read-optimized store (for fast reads, complex queries)
- Synchronization: Event broker (Kafka/NATS) propagates writes to the read model

**When to use:** High read/write imbalance; need to scale reads independently.

**Example:**
```
ProductCatalog (CQRS):
  Command model: PostgreSQL (for writes: add/update product)
  Query model: Elasticsearch (for search queries, autocomplete)
  Sync: Kafka streams changes from Postgres to Elasticsearch
```

---

### Performance Optimization Patterns

#### 21. Materialized View Pattern

**Definition:** Pre-computes and stores complex query results as a dedicated view/table, updated asynchronously when source data changes.

**Example:**
```
Instead of joining Orders + Products + Customers on every request:
→ Pre-compute and store as "OrderSummaryView" table
→ Rebuild the view when orders/products change
→ Reporting queries hit the pre-computed view (near-instant)
```

---

#### 22. Data Locality Pattern

**Definition:** Move computation closer to the data (or move data closer to the compute) to reduce network overhead.

**Example:** Running an aggregation query directly in the database (stored procedure) rather than fetching all rows to the application layer and computing in Java.

---

#### 23. Caching Pattern

**Definition:** Store frequently read data in a fast in-memory cache to reduce load on the origin database.

**Cache strategies:**
- **Cache-aside (Lazy loading):** App checks cache first; if miss, loads from DB, then stores in cache
- **Write-through:** Write to cache and DB simultaneously
- **Write-behind:** Write to cache, async write to DB later

**Cache invalidation:** The hardest part. Common approaches: TTL (time-to-live), event-based invalidation.

**Example:**
```
ProductService:
  1. Check Redis for product:123
  2. If cache hit → return immediately
  3. If cache miss → query PostgreSQL → store in Redis with 5-min TTL → return
```

**Technologies:** Redis, Memcached, Ehcache

---

#### 24. Static Content Hosting Pattern

**Definition:** Serve static assets (HTML, CSS, JS, images) from a CDN (Content Delivery Network) instead of the application server.

**Example:** Upload React build artifacts to Amazon S3 + CloudFront CDN. The CDN serves assets from edge locations closest to the user — millisecond latency globally.

---

### Reliability Patterns

#### 25. Transaction Pattern (Distributed Transactions)

**Definition:** Apply ACID-style guarantees across distributed microservices using a coordinated commit strategy.

**In microservices:** Use Saga (compensating transactions) instead of 2PC, as 2PC is a distributed systems anti-pattern due to its blocking nature and performance issues.

---

#### 26. Security Vault Key Pattern

**Definition:** Use a secrets management system (Vault) to securely store and access sensitive configuration (DB passwords, API keys, certificates) rather than hardcoding them.

**Example:**
```
Service startup:
  1. Authenticate to HashiCorp Vault using service identity (Kubernetes ServiceAccount)
  2. Vault returns DB credentials (short-lived, auto-rotated)
  3. Service uses credentials to connect to DB
```

---

## Chapter 5: Event-Driven Architecture (EDA) Patterns

### Event-Driven Architecture Fundamentals

**EDA** is an architecture where services communicate through **events** — immutable facts about something that happened in the system.

**Message delivery semantics:**
- **At-most-once:** Message may be lost (no retry) — fire and forget
- **At-least-once:** Message delivered at least once (may duplicate) — consumer must be idempotent
- **Exactly-once:** Guaranteed single delivery — expensive but critical for payments

---

### Event-Delivery Patterns

#### 27. Producer-Consumer Pattern

**Definition:** One or more producers write events to a queue; one or more consumers read events from it. Events are consumed once and removed.

**Example:** Order events produced to a Kafka topic, consumed by the Shipping service.

---

#### 28. Publisher-Subscriber Pattern

**Definition:** Publisher sends events to a **topic**; all active subscribers receive a copy independently.

**Key difference from Producer-Consumer:** Each subscriber gets its own copy; events are not removed after one consumption.

**Example:**
```
InventoryService publishes StockUpdated event:
  → RecommendationService subscribes and retrains ML model
  → NotificationService subscribes and emails subscribed customers
  → AnalyticsService subscribes and updates dashboard
```

---

#### 29. Fire and Forget Pattern

**Definition:** Producer sends a message and does **not** wait for or expect any response. Best-effort delivery with at-most-once semantics.

**When to use:** Non-critical notifications, analytics events, logging.

**Example:** User activity tracking — log every click event. If one event is lost, it doesn't matter.

---

#### 30. Store and Forward Pattern

**Definition:** Producer stores events locally first, then forwards them to the broker when connectivity is available.

**Use case:** Edge devices with intermittent connectivity (IoT sensors, mobile apps in offline mode).

---

#### 31. Polling Pattern

**Definition:** Consumer periodically polls the broker or service for new messages/events.

**When to use:** When push-based subscription is not supported or when you need rate-controlled consumption.

---

#### 32. Request Callback Pattern

**Definition:** Producer sends a request event and provides a **callback endpoint**. Broker calls back the producer with the result when ready.

**Example:**
```
ReportGenerationService:
  → Publishes: {requestId: 123, callbackUrl: "https://myapp/callback/123"}
  ← Broker/consumer calls back: POST /callback/123 with {report: <data>}
```

---

### State Management Patterns

#### 33. Event Sourcing Pattern

**Definition:** Instead of storing the current state of an entity, store **all the events** that led to that state. The current state is derived by replaying events.

**Advantages:**
- Full audit log built-in
- Can replay to reconstruct state at any point in time
- Enables event-driven projections (CQRS read models)

**Disadvantages:** Querying current state requires replaying events; snapshots needed for performance.

**Example:**
```
BankAccount — Instead of:
  {accountId: 1, balance: 500}

Store events:
  [AccountOpened(balance=0), Deposited(100), Deposited(500), Withdrawn(100)]
  
Replay events → current balance = 500
```

---

### Orchestration Patterns (EDA)

#### 34. Mediator Pattern

**Definition:** A central mediator orchestrates the processing of complex events by routing them through multiple processing steps.

**Example:** An order event enters the Mediator, which routes it to inventory check, fraud detection, and payment in sequence.

---

#### 35. Pipe and Filter Pattern

**Definition:** Events flow through a **pipeline** of independent processing stages (filters). Each filter transforms/enriches/filters the event.

**Example:**
```
RawLogEvent → [ParseFilter] → [EnrichFilter] → [AnomalyDetectionFilter] → [StoreFilter] → AlertsDB
```

---

#### 36. Priority Queue Pattern

**Definition:** Events are assigned priority levels. Higher-priority events are processed before lower-priority ones.

**Example:** In an order processing system, premium customer orders get Priority 1 and are processed ahead of regular orders.

---

## Chapter 6: Stream Processing Patterns

### Stream vs. Event

| | Event-Driven | Stream Processing |
|---|---|---|
| Processing unit | Single event at a time | Sequence/window of events |
| State | Stateless | Stateful |
| Correlation | No correlation between events | Events correlated within time windows |

---

### Streaming Data Processing Patterns

#### 37. Transformation Pattern

**Definition:** Transforms each incoming event in the stream from one format/structure to another.

**Example:**
```
Raw sensor event: {sensor_id: "S1", raw_temp: 2985}  // Kelvin
→ Transformed: {sensor_id: "S1", temperature_celsius: 25.35, timestamp: "2026-04-06T03:41:00"}
```

---

#### 38. Filters and Thresholds Pattern

**Definition:** Pass events through a stream only if they meet certain criteria; discard others.

**Example:**
```
Stream of temperature readings:
→ Filter: Only pass events where temperature > 80°C
→ Output: Alert stream for high-temperature incidents only
```

---

#### 39. Windowed Aggregation Pattern

**Definition:** Aggregates events within a **time window** or **count window** to produce summary results.

**Window types:**
- **Tumbling window:** Fixed, non-overlapping windows (e.g., every 5 minutes)
- **Sliding window:** Overlapping windows (e.g., last 5 minutes, evaluated every 1 minute)
- **Session window:** Windows based on activity gaps

**Example:**
```
E-commerce click stream:
Tumbling window (1 min) → COUNT(clicks per product) → Trending products every minute
```

---

#### 40. Stream Join Pattern

**Definition:** Joins two or more event streams based on a common key and time proximity to enrich events.

**Example:**
```
Stream 1: OrderEvents {orderId, customerId}
Stream 2: CustomerEvents {customerId, loyaltyTier}

Join on customerId within 5-minute window:
→ EnrichedOrder {orderId, customerId, loyaltyTier}
```

---

#### 41. Temporal Event Ordering Pattern

**Definition:** Reorders out-of-order events based on their event timestamps (not arrival timestamps) before processing.

**Problem:** Network delays can cause events to arrive out of order. Processing them in arrival order gives wrong results.

**Example:**
```
Arrived order: [T=5, T=3, T=4]  ← wrong processing order
After reordering by timestamp: [T=3, T=4, T=5] ← correct
```

---

#### 42. Machine Learner Pattern

**Definition:** Applies a pre-trained ML model to a stream of events in real-time to produce predictions.

**Example:**
```
Transaction stream → [FraudDetectionMLModel] → Fraud probability score
→ If score > 0.9 → Block transaction, trigger alert
```

---

### Scaling and Performance Patterns

#### 43. Sequential Convoy Pattern

**Definition:** Ensures events with the same key (e.g., same orderId) are processed **in order** even when horizontally scaling.

**How:** Partition the event stream by key. All events with the same key always go to the same partition/worker.

**Example in Kafka:** Partition by `customerId` — all events for customer 123 go to Partition 2, processed sequentially.

---

#### 44. Buffered Event Ordering Pattern

**Definition:** Buffer incoming events and sort them by event time before processing to handle out-of-order arrival.

---

#### 45. Course Correction Pattern

**Definition:** Detect and correct previously computed incorrect aggregations when late-arriving events arrive.

**Example:** Sales report for Monday was computed at midnight. Two late events arrive Tuesday. The system re-computes and corrects Monday's report.

---

#### 46. Watermark Pattern

**Definition:** A **watermark** is a timestamp threshold declaring "all events with time ≤ T have arrived." Enables stream processors to close windows and trigger aggregations confidently.

**Example:** Watermark at T-2 minutes means: "wait 2 extra minutes after window end before finalizing the aggregation, to account for late events."

---

### Stream Reliability Patterns

#### 47. Replay Pattern

**Definition:** Re-process previously consumed events from the broker's log to rebuild state after failure or for testing.

**Requirement:** Log-based broker (Kafka) that retains messages.

**Example:** After a bug fix is deployed, replay the last 24 hours of Kafka events to recompute analytics correctly.

---

#### 48. Periodic Snapshot State Persistence Pattern

**Definition:** Periodically save the current processing state (checkpoint) to durable storage. On restart, load the latest snapshot and replay only events after the snapshot timestamp.

**Example:** Apache Flink checkpointing to S3 every 30 seconds — recovery loads the last checkpoint and only replays the last 30 seconds of events.

---

#### 49. Two-Node Failover Pattern

**Definition:** Run two identical stream-processing nodes (active + standby). Standby takes over immediately if the active node fails — no state rebuild needed.

**Use case:** Ultra-low-latency processing where even short recovery times are unacceptable.

---

### Stream Processing Technologies

| Technology | Type | Key Feature |
|---|---|---|
| Apache Kafka + ksqlDB | Log-based streaming | SQL-like stream queries, high throughput |
| Apache Flink | Distributed stream processing | Exactly-once, low latency, stateful |
| Apache Spark Streaming | Micro-batch streaming | Batch + streaming unified |
| Amazon Kinesis | Managed cloud streaming | AWS native, serverless |
| Azure Stream Analytics | Managed cloud streaming | Azure native, SQL queries on streams |
| Google Dataflow | Managed cloud streaming | GCP native, Apache Beam |

---

## Chapter 7: API Management and Consumption Patterns

### API Management Patterns

#### 50. API Gateway Pattern

**Definition:** A single entry point (reverse proxy) for all external clients. The gateway handles cross-cutting concerns and routes requests to backend microservices.

**What API Gateway does:**
- Authentication & Authorization (OAuth2, JWT, API Keys)
- Rate limiting & Throttling
- Request/Response transformation
- SSL termination
- Caching
- Logging, monitoring, analytics
- Developer portal & API documentation

**Roles:**
- **API Creator/Developer:** Creates and documents APIs in the gateway
- **API Publisher:** Manages API lifecycle, subscriptions, monetization
- **Application Developer:** Discovers and consumes APIs via developer portal
- **API Control Plane Admin:** Manages the gateway infrastructure

**Example:**
```
Mobile App → [API Gateway: auth check, rate limit] → OrderService
                                                    → ProductService
                                                    → PaymentService
```

**Technologies:** Kong, AWS API Gateway, Azure API Management, Apigee

---

#### 51. API Microgateway Pattern

**Definition:** Deploy a **lightweight, decentralized gateway** per service team or API group, rather than a single centralized gateway.

**Advantages:**
- Teams independently manage their APIs
- No single point of failure/bottleneck
- API gateway scales with the individual service

**Example:** Team A deploys Kong as a microgateway for the Catalog APIs. Team B has a separate Nginx-based microgateway for Order APIs. Both are independent.

---

#### 52. Service Mesh Sidecar as API Gateway Pattern

**Definition:** Leverage the existing service mesh sidecar proxy (e.g., Envoy in Istio) to act as the API gateway, avoiding a separate gateway deployment.

**Use case:** When you already have a service mesh and want to consolidate the gateway and mesh layers.

---

### API Consumption Patterns

#### 53. Direct Frontend-to-Microservices Communication Pattern

**Definition:** Frontend apps directly call individual microservices without an intermediary gateway.

**Problems:**
- Frontend must know about all microservices
- Multiple network round trips per page load
- CORS issues, authentication duplication

**When acceptable:** Simple apps with few services; internal tools.

---

#### 54. Frontends Consuming Services Through API Gateway Pattern

**Definition:** Frontend apps always go through the API Gateway. All cross-cutting concerns (auth, rate limiting, routing) are centralized.

**Advantages:** Frontend decoupled from service topology; security enforced at one point.

---

#### 55. Backend for Frontends (BFF) Pattern

**Definition:** Build a **separate backend service for each type of frontend** (mobile BFF, web BFF, partner BFF) that aggregates and adapts microservice APIs for that specific client's needs.

**Why:** A mobile app needs compact, bandwidth-efficient responses. A web dashboard needs rich data. A single API can't serve both optimally.

**Example:**
```
Mobile App → [Mobile BFF]
  → Calls ProductService + InventoryService
  → Returns compact, mobile-optimized response {name, price, inStock}

Web App → [Web BFF]
  → Calls same services + ReviewService + RecommendationService
  → Returns full-featured response with all details
```

---

## Chapter 8: Cloud Native Patterns in Practice

### Online Retail System — Pattern Application Map

The book synthesizes all patterns through a retail application. This is an excellent reference for interviews.

| Business Capability | Patterns Applied |
|---|---|
| Product Catalog | CQRS (RDBMS command, NoSQL query), API Gateway, Service Registry |
| Order Management | Service Orchestration, Saga, Single-Receiver (Kafka), Request-Response |
| Inventory Service | CQRS, Caching (legacy ERP cache), Relational DB |
| Order Tracking | Event Sourcing, Event-Driven (Pub-Sub), Stream Processing |
| Product Recommendations | Machine Learner Pattern, Pub-Sub (purchase events) |
| External APIs | API Gateway (REST/GraphQL), API Microgateway, BFF |
| Interservice Communication | gRPC (internal), REST (external), Service Mesh (Istio) |
| Dynamic Management | Kubernetes, Service Registry (Consul), Control Plane |

---

## Quick Reference: Pattern Cheat Sheet

| Pattern | Category | One-Line Summary |
|---|---|---|
| Request-Response | Communication | Synchronous call-and-wait; most common for external APIs |
| RPC (gRPC) | Communication | Efficient binary protocol for internal service calls |
| Single-Receiver | Communication | Queue-based point-to-point guaranteed delivery |
| Multiple-Receiver | Communication | Topic-based broadcast to many subscribers |
| Asynchronous Request-Reply | Communication | Queue-based request + callback queue for reply |
| Service Connectivity | Connectivity | Standard inter-service communication via APIs |
| Service Abstraction | Connectivity | Represent all entities (including legacy) as services |
| Service Registry & Discovery | Connectivity | Dynamic service endpoint registration and lookup |
| Resilient Connectivity | Connectivity | Timeout, retry, circuit breaker, deadlines |
| Sidecar | Connectivity | Helper container for cross-cutting concerns |
| Service Mesh | Connectivity | Platform-level traffic management and security |
| Sidecarless Mesh | Connectivity | Kernel-level mesh without per-pod sidecar overhead |
| Orchestration | Composition | Central coordinator calls services sequentially |
| Choreography | Composition | Services react to events; no central coordinator |
| Saga | Composition | Distributed transactions with compensating operations |
| Data Service | Data | Expose data as an API; no direct DB access |
| Composite Data Services | Data | Aggregate multiple data services into one response |
| Client-Side Mashup | Data | Client calls multiple APIs and combines data |
| Data Sharding | Data | Split data across nodes by shard key for scalability |
| CQRS | Data | Separate read and write models for independent scaling |
| Materialized View | Data | Pre-computed query results for read performance |
| Caching | Data | In-memory store for frequent reads (Redis) |
| Static Content Hosting | Data | CDN for static assets |
| Transaction | Data | ACID guarantees (use Saga for distributed) |
| Security Vault Key | Data | Secrets management via HashiCorp Vault |
| Producer-Consumer | EDA | Events consumed once from queue |
| Publisher-Subscriber | EDA | Events broadcast to all subscribers |
| Fire and Forget | EDA | No response expected; best-effort delivery |
| Event Sourcing | EDA | Store events as source of truth; derive current state |
| Mediator | EDA | Central routing of complex event flows |
| Pipe and Filter | EDA | Pipeline of independent event transformation stages |
| Priority Queue | EDA | Process high-priority events first |
| Transformation | Stream | Convert event format/structure |
| Filters and Thresholds | Stream | Pass only events meeting criteria |
| Windowed Aggregation | Stream | Aggregate events within time/count windows |
| Stream Join | Stream | Join multiple event streams on key + time |
| Temporal Event Ordering | Stream | Reorder events by event time, not arrival time |
| Machine Learner | Stream | Apply ML model to stream for real-time predictions |
| Sequential Convoy | Stream | Ordered processing of same-key events at scale |
| Watermark | Stream | Threshold for closing time windows with late events |
| Replay | Stream | Reprocess past events to rebuild state |
| Periodic Snapshot | Stream | Checkpoint state; replay only from last checkpoint |
| Two-Node Failover | Stream | Active-standby for zero-downtime stream processing |
| API Gateway | API Mgmt | Single entry point with cross-cutting concerns |
| API Microgateway | API Mgmt | Decentralized gateway per team/API |
| Backend for Frontends (BFF) | API Consumption | Dedicated backend per frontend type |
| Direct Frontend-to-Service | API Consumption | Frontend calls services directly (simple cases only) |

---

## Top Interview Questions & Model Answers

**Q: What is the difference between Service Orchestration and Service Choreography?**
> Orchestration uses a central coordinator that explicitly calls services in sequence. Choreography uses events — each service reacts to events and publishes new ones, with no central controller. Orchestration is easier to debug; choreography is more loosely coupled and resilient.

**Q: Explain the Saga pattern. When would you use it?**
> Saga manages distributed transactions across microservices by breaking a transaction into local steps, each with a compensating action (undo). If any step fails, compensating transactions roll back completed steps. Use it whenever a business operation spans multiple microservices with separate databases.

**Q: What is CQRS and when should you use it?**
> CQRS separates write operations (commands) from read operations (queries) using different data models and often different databases. Use it when reads and writes have very different scalability requirements, or when complex query patterns would degrade write performance on the same store.

**Q: What is Event Sourcing?**
> Event sourcing stores all state-changing events rather than current state. The current state is derived by replaying the event log. It provides a complete audit trail, enables time-travel debugging, and integrates naturally with CQRS.

**Q: How does a Circuit Breaker work?**
> A circuit breaker monitors calls to a downstream service. When failures exceed a threshold, it opens (blocks calls) to prevent cascading failures. After a cooldown period, it enters half-open (allows test call). If the test succeeds, it closes; otherwise, it stays open.

**Q: What is the BFF pattern?**
> Backend for Frontends creates a separate backend service per client type (mobile, web, partner). Each BFF aggregates and shapes the response optimally for its client, avoiding a one-size-fits-all API that serves none of them well.

**Q: What is the difference between a Service Mesh and an API Gateway?**
> An API Gateway handles north-south traffic (external clients to services) with concerns like authentication, rate limiting, and routing. A Service Mesh handles east-west traffic (service-to-service) with mTLS, circuit breaking, and distributed tracing — transparently, without application code changes.

**Q: How do you handle data consistency in microservices without distributed transactions?**
> Use the Saga pattern with compensating transactions for business processes. Use Event Sourcing for audit trails and state reconstruction. Accept eventual consistency through event-driven synchronization (e.g., CQRS read model updated via Kafka events).

---

*Generated from: "Design Patterns for Cloud Native Applications" by Kasun Indrasiri & Sriskandarajah Suhothayan (O'Reilly, 2021)*  
*Document prepared: April 2026*
