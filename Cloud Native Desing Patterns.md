Cloud Native Design Patterns – Interview Preparation Guide

Based on: Design Patterns for Cloud Native Applications (O'Reilly, 2021)



1\. Introduction to Cloud Native

What is Cloud Native?

Definition: Building applications as a collection of independent, loosely coupled, business‑capability‑oriented services (microservices) that run on dynamic environments (public/private/hybrid/multi‑cloud) in an automated, scalable, resilient, manageable, and observable way.



Key characteristics:



Designed as microservices



Containerized (Docker) and orchestrated (Kubernetes)



Automated CI/CD pipelines



Dynamic management (autoscaling, self‑healing, observability)



Microservices vs. SOA

SOA: Coarse‑grained services, often with a central Enterprise Service Bus (ESB).



Microservices: Fine‑grained, autonomous, no central ESB, “smart endpoints and dumb pipes”.



Example: Online retail – separate services for Catalog, Order, Payment, Shipping, each with its own database.



Containers \& Orchestration

Container: Isolated process with its own filesystem (image). Lightweight compared to VMs.



Kubernetes: De facto orchestrator – provides service abstraction, service discovery, load balancing, scaling, rolling updates.



Key Kubernetes objects: Pod (smallest deployable unit), Service (stable endpoint for pods), Deployment (desired state), ReplicaSet (number of replicas).



Twelve‑Factor App \& Methodology

Twelve‑Factor is a baseline but incomplete for cloud native.



Extended methodology: Design → Develop → Connect \& compose → Automate → Run → Control plane → Observe.



2\. Communication Patterns

Synchronous Patterns

Pattern	Use case	Example

Request‑Response	Client waits for immediate response	REST API for product search

Remote Procedure Call (RPC)	Efficient, type‑safe service‑to‑service calls	gRPC between Order and Payment

gRPC: Uses HTTP/2, Protocol Buffers, supports streaming. Ideal for low‑latency internal communication.



REST: Resource‑oriented, best for external APIs.



GraphQL: Client specifies exact data needs – avoids over‑fetching.



Asynchronous Patterns

Pattern	Description	Example

Single‑Receiver (Queue)	One message to one consumer, guaranteed delivery	Order placed → OrderProcessing service (RabbitMQ)

Multiple‑Receiver (Pub‑Sub)	One message to many consumers	Price update → ShoppingCart + FraudDetection + Subscription

Asynchronous Request‑Reply	Request via queue, reply via different queue	Loan approval request → callback queue

Message brokers: AMQP (RabbitMQ, ActiveMQ), Kafka (log‑based, replayable), NATS (lightweight, cloud native).



Service Definition

Synchronous: OpenAPI (REST), Protocol Buffers (gRPC), GraphQL schema.



Asynchronous: Schema registry (Avro, Protobuf) with AsyncAPI for contract.



Example: Kafka Schema Registry stores Avro schemas; producers/consumers validate.



3\. Connectivity \& Composition Patterns

Connectivity Patterns

Pattern	Purpose	Example

Service Abstraction	Stable endpoint hiding implementation	Kubernetes Service

Service Registry \& Discovery	Central metadata repository	Consul, etcd, Kubernetes DNS

Resilient Connectivity	Timeouts, retries, circuit breakers, deadlines	Resilience4j, Istio

Sidecar	Offload networking to colocated container	Envoy proxy, Dapr

Service Mesh	Data plane (sidecars) + control plane (Istio)	Traffic routing, mTLS, observability

Sidecarless Service Mesh	Embed proxy logic into client library	gRPC + xDS (Google Traffic Director)

Example – Circuit Breaker:

If Payment service fails 5 times in 10 seconds, circuit opens. Subsequent calls fail fast without hitting Payment. After 30s, half‑open state allows one trial call.



Composition Patterns

Pattern	Description	When to use

Service Orchestration	Central service calls others	Order service coordinating Inventory, Payment, Shipping

Service Choreography	Event‑driven, no central coordinator	Each service reacts to events via broker

Saga	Distributed transaction with compensating actions	Travel booking: book flight, hotel, car – if hotel fails, cancel flight

Saga Example (Orchestration):

Order service starts Saga:



Create Order → 2. Reserve Payment → 3. Reserve Inventory.

If Inventory fails, execute compensating transactions: Release Payment, Cancel Order.



4\. Data Management Patterns

Data Architecture

Data sources → Ingestion (brokers) → Processing (real‑time/batch) → Data stores → APIs.



Types of data: Input (request), Configuration (env vars), State (persisted).



Forms: Structured (RDBMS), Semi‑structured (JSON/XML), Unstructured (blobs).



Data Store Selection

Store	Use case	Example

Relational (RDBMS)	ACID transactions, structured data	PostgreSQL for Payments

NoSQL – Key‑Value	Session cache, high‑speed lookups	Redis

NoSQL – Column	Large scale, high write throughput	Cassandra for events

NoSQL – Document	Semi‑structured, flexible schema	MongoDB for Product Catalog

NoSQL – Graph	Relationships, fraud detection	Neo4j

Filesystem / Object	Unstructured, big files	Amazon S3 for product images

Data Management Approaches

Centralized: Single database – antipattern for microservices.



Decentralized (per service): Each microservice owns its data store. External access only via API.



Hybrid: Multiple services share a database within the same bounded context.



Data Composition Patterns

Pattern	Description

Data Service	Expose database as an API (abstraction)

Composite Data Service	Combine data from multiple services on server side

Client‑Side Mashup	Browser calls multiple APIs asynchronously (Ajax)

Data Scaling Patterns

Data Sharding: Horizontal (by key), Vertical (by column groups), Functional (by use case).

Example: Shard orders by customer\_id % 4.



CQRS (Command Query Responsibility Segregation): Separate write (command) and read (query) models.

Example: Command uses PostgreSQL; Query uses Elasticsearch, updated via events.



Performance Optimization

Pattern	How it works	Example

Materialized View	Pre‑computed, replicated data for fast reads	Product details + average rating from Reviews service

Data Locality	Move execution closer to data (stored procedure, colocation)	Run aggregation on database server

Caching	In‑memory store (Redis) with eviction policies	Cache product catalog, TTL 5 minutes

Static Content Hosting	CDN for static assets	Images, CSS, JS served from CloudFront

Reliability \& Security

Transaction Pattern: ACID guarantees within a single data store.



Saga for distributed transactions (see earlier).



Vault Key Pattern: Issue short‑lived tokens for direct data store access (e.g., AWS IAM roles, Vault).



5\. Event‑Driven Architecture Patterns

Core Concepts

Event: Significant state change.



Event delivery guarantees: at‑most‑once, at‑least‑once, exactly‑once processing (via idempotency or sequence numbers).



CloudEvents: CNCF standard for event metadata.



Event Delivery Patterns

Pattern	Broker?	Guarantee	Use case

Producer‑Consumer	Yes (queue)	At‑least‑once	Command: process order

Publisher‑Subscriber	Yes (topic)	Best effort or durable	Notify all interested services

Fire and Forget	No	At‑most‑once	Sensor sending telemetry

Store and Forward	Client‑side store	At‑least‑once	Partner API that may be down

Polling	No	Client pulls	Long‑running job status

Request‑Callback	No	Webhook/WebSocket	Async response

State Management

Event Sourcing: Store every state change as an event in an append‑only log (e.g., Kafka).

Example: Bank account – events: Deposited 100, Withdrew 20. Current balance = replay events.

Benefits: audit trail, time travel, rebuild state.



Orchestration Patterns

Pattern	Description

Mediator	Central coordinator (stateful) routes events, manages parallel/sequential steps

Pipe and Filter	Decoupled stages connected via queues/topics – each filter does one task

Priority Queue	Multiple queues with different priorities; consumer polls proportionally

Example – Mediator for insurance claim:

Receive claim → parallel: address verification, credit check, referral → combine → discount → approval.



6\. Stream‑Processing Patterns

What is Stream Processing?

Continuous, stateful processing of unbounded sequences of events ordered by time.



Stream vs event: Stream is a sequence; event is a single occurrence.



Data Processing Patterns

Pattern	Description	Example

Transformation	Map event format/protocol	XML → JSON

Filters \& Thresholds	Keep only relevant events	amount > 1000 AND country != 'USA'

Windowed Aggregation	Time‑based or count‑based windows	Sum of sales last 5 minutes (sliding window)

Stream Join	Join two streams over a window	Enrich transaction with user profile

Temporal Event Ordering	Detect sequence or absence of events	Fraud: transaction in US then outside US within 3 hours

Machine Learner	Real‑time predictions (prebuilt or online)	Predict delivery ETA

Scaling \& Performance

Pattern	Problem solved

Sequential Convoy	Partition stream by key to process in parallel while preserving order per key

Buffered Event Ordering	Reorder out‑of‑order events using sequence numbers or timestamps

Course Correction	Emit early estimate, then correct when late data arrives

Watermark	Synchronize multiple streams with periodic “watermark” events

Reliability Patterns for Streams

Replay: Re‑process events from a log (Kafka) after failure.



Periodic Snapshot State Persistence: Save state (e.g., checkpoints) to durable storage; restore from latest snapshot + replay.



Two‑Node Failover: Active‑standby pair; standby processes same events but only active publishes output.



Example – Snapshot with Kafka:

Flink job checkpoints state every 30 seconds to S3. On restart, load last checkpoint and replay events after that offset.



7\. API Management \& Consumption Patterns

API Management Components

API Gateway: Front door – security, throttling, caching, routing.



API Control Plane: Create, publish, manage API life cycle.



Developer Portal: Discover, subscribe, get keys.



API Management Patterns

Pattern	Description

API Gateway	Single monolithic gateway for all APIs

API Microgateway	One gateway per API (decentralized) – scales independently

Service Mesh Sidecar as API Gateway	Reuse sidecar (Envoy) for API management

API Consumption Patterns (Frontend → Backend)

Pattern	Description

Direct Frontend‑to‑Microservices	Simple but tight coupling, security risks

Frontend via API Gateway	Central entry point, managed policies

Backend for Frontends (BFF)	Dedicated API per frontend type (mobile vs web)

Example – BFF:

Mobile BFF returns compact JSON, uses OAuth2; Web BFF returns HTML fragments, uses session cookies.



8\. Patterns in Practice – Online Retail Case Study

The book concludes with an online retail system applying many patterns:



Component	Patterns Used

Product Catalog	CQRS (command: RDBMS, query: NoSQL), caching, API Gateway

Order Management	Service Orchestration (Order service), Saga for payment+inventory, Event Sourcing for order status

Order Tracking \& Prediction	Stream processing (Flink), Windowed Aggregation, Machine Learner for ETA

Recommendations	Pub‑Sub events (user actions) → stream join with product data

External APIs	API Gateway + BFF for mobile and web

Deployment	Kubernetes, Istio service mesh, CI/CD with Jenkins

Dynamic Management

Autoscaling based on queue depth (KEDA).



Observability: Prometheus metrics, Jaeger tracing, ELK logs.



Security: OAuth2, JWT, mTLS (Istio).



Key Takeaways for Interviews

Know the “why” behind each pattern: decoupling, scalability, resilience, observability.



Be able to compare synchronous vs asynchronous, orchestration vs choreography, CQRS vs Event Sourcing.



Give concrete examples from the online retail use case or your own experience.



Understand trade‑offs: CQRS adds complexity, Event Sourcing requires handling schema evolution, Service Mesh adds operational overhead.



Mention CNCF projects: Kubernetes, Envoy, Jaeger, Prometheus, CloudEvents, NATS, etc.



Explain how patterns work together – e.g., API Gateway + Service Mesh + Event Sourcing in a single application.



End of Guide

