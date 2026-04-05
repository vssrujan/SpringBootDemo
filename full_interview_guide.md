
JAVA + SPRING + MICROSERVICES + CLOUD INTERVIEW MASTER GUIDE (10+ YEARS)

================ JAVA (ADVANCED) =================

Q: Explain CompletableFuture
Answer:
Used for async programming.

Example:
CompletableFuture.supplyAsync(() -> "Hello")
.thenApply(res -> res + " World")
.thenAccept(System.out::println);

---

Q: What is ForkJoinPool?
Answer:
Used for parallel processing (divide and conquer).

---

Q: JVM Memory Structure?
Answer:
Heap, Stack, Metaspace, PC Register, Native Method Stack

---

================ SPRING =================

Q: Bean Lifecycle?
Answer:
Instantiation -> Dependency Injection -> Init -> Destroy

---

Q: @Transactional propagation types?
Answer:
REQUIRED, REQUIRES_NEW, NESTED

---

================ SPRING BOOT =================

Q: How auto-configuration works?
Answer:
Uses spring.factories + @EnableAutoConfiguration

---

================ MICROSERVICES =================

Q: Circuit Breaker?
Answer:
Prevents cascading failures.

Example: Resilience4j

---

Q: API Gateway role?
Answer:
Routing, Security, Rate limiting

---

================ REST =================

Q: Idempotent methods?
Answer:
GET, PUT, DELETE

---

================ GRAPHQL =================

Q: N+1 Problem?
Answer:
Multiple DB calls → solved using DataLoader

---

================ APIGEE =================

Q: Policies?
Answer:
Spike Arrest, Quota, OAuth, JWT

---

================ CLOUD =================

Q: Difference between AWS, GCP?
Answer:
GCP simpler, AWS broader services

---

================ BIGQUERY =================

Q: Partitioning?
Answer:
Improves query performance

---

================ JENKINS =================

Q: Pipeline as Code?
Answer:
Defined in Jenkinsfile

---

================ MAVEN =================

Q: Dependency scope?
Answer:
compile, test, provided, runtime

---

================ SQL =================

Q: Indexing?
Answer:
Improves query performance

Example:
CREATE INDEX idx_name ON emp(name);

