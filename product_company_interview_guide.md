
PRODUCT COMPANY INTERVIEW MASTER GUIDE (JAVA + SYSTEM DESIGN + CLOUD)

==================== JAVA (ADVANCED - AMAZON LEVEL) ====================

Q: Explain how HashMap works internally?
Answer:
- Uses array of Node
- Hashing + index calculation
- Handles collisions using LinkedList / Tree (Java 8+)

Key Points:
- Load factor (0.75 default)
- Resize when threshold exceeded

Follow-up:
Why Tree instead of LinkedList?
→ O(log n) vs O(n)

---

Q: Explain JVM Garbage Collectors (G1 vs ZGC)?
Answer:
G1:
- Region-based
- Predictable pause times

ZGC:
- Low latency (<10ms)
- Concurrent processing

Use Case:
ZGC → high throughput + low latency apps

---

Q: Thread Pool vs Virtual Threads (Java 21)?
Answer:
Thread Pool:
- Limited threads
- Blocking operations expensive

Virtual Threads:
- Lightweight
- Millions of threads possible

---

==================== SPRING INTERNALS ====================

Q: How does DispatcherServlet work?
Answer:
1. Request → DispatcherServlet
2. HandlerMapping → Controller
3. HandlerAdapter executes
4. ViewResolver returns response

---

Q: @Transactional pitfalls?
Answer:
- Self-invocation issue
- Checked exception doesn't rollback

---

==================== MICROSERVICES DESIGN ====================

Q: How do you design a scalable system?
Answer:
- Load balancer
- API Gateway (Apigee)
- Stateless services
- Caching (Redis)
- DB sharding

---

Q: Circuit Breaker pattern?
Answer:
- Prevents cascading failures
- States: CLOSED, OPEN, HALF-OPEN

---

Q: How do you handle distributed transactions?
Answer:
- Saga pattern
- Event-driven architecture

---

==================== SYSTEM DESIGN ====================

Q: Design URL Shortener
Answer:
- Hashing / Base62 encoding
- DB + Cache
- Unique key generation

Trade-offs:
- DB vs NoSQL
- Consistency vs availability

---

==================== GRAPHQL (ADVANCED) ====================

Q: Solve N+1 problem?
Answer:
Use DataLoader batching

---

==================== APIGEE ====================

Q: Rate limiting strategies?
Answer:
- Spike arrest
- Quota

---

==================== CLOUD (GCP FOCUS) ====================

Q: Cloud Run advantages?
Answer:
- Serverless
- Auto scaling
- Pay per use

---

Q: CI/CD pipeline design?
Answer:
- GitLab → Jenkins → Deploy → Monitor

---

==================== BIGQUERY ====================

Q: Optimization techniques?
Answer:
- Partitioning
- Clustering
- Avoid SELECT *

---

==================== SQL (ADVANCED) ====================

Q: Window function example?
Answer:
SELECT name, RANK() OVER (ORDER BY salary DESC) FROM emp;

---

Q: Index vs Partition?
Answer:
Index → improves lookup
Partition → improves large scan

---

==================== BEHAVIORAL (VERY IMPORTANT) ====================

Q: Tell me about a challenging project?
Answer:
Use STAR method:
Situation, Task, Action, Result

---

Q: Leadership question?
Answer:
Explain how you handled team conflicts

---

==================== INTERVIEW STRATEGY ====================

- Always explain trade-offs
- Think aloud
- Clarify requirements
- Give scalable solution

