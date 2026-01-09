# Dave Farely's BDD Architecture Explained

`Test Cases`

Declarative, intent-focused specifications.

Do not directly interact with the system or protocols.

DSL (Domain-Specific Language)

A semantic abstraction layer.

Encodes business intent and test meaning.

Insulates tests from protocol and transport changes.

Protocol Drivers

Imperative adapters.

Translate DSL commands into concrete protocol interactions.

Multiple drivers can coexist (HTTP, messaging, binary, etc.).

System Under Test

The actual runtime system.

Only communicates via protocols, never directly with tests.

Key Architectural Properties

Tests are stable; protocol drivers are volatile.

Behavior is tested, not implementation details.

`Enable`

Readable tests

Replaceable transports

Clear separation of intent vs mechanism

## Structure of the DSL and Protocol Drivers

```mermaid
flowchart TB
    T1[Test Case 1] & T2[Test Case 2] & T3[Test Case 3] & T4[Test Case 4] & T5[Test Case 5] & T6[Test Case 6] --> DSL[DSL<br/>Test API]

    DSL --> D1[Protocol Driver<br/>HTTP]
    DSL --> D2[Protocol Driver<br/>MQ]
    DSL --> D3[Protocol Driver<br/>Binary]

    D1 & D2 & D3 --> SUT[System Under Test]
```
