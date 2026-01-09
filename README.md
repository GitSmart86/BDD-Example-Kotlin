# BDD Example - Kotlin

A showcase of **Behavior-Driven Development (BDD)** principles applied to a Kotlin codebase, demonstrating how to write tests against interfaces rather than implementations.

---

## Background

This project started as a timed coding exercise (refactoring + caching implementation). I approached it differently than intended - instead of just completing the tasks, I used it as an opportunity to demonstrate proper BDD architecture.

### My Starting Point

- **Zero Kotlin experience** - This was my first Kotlin project
- **Minimal Java knowledge** - Understood the JVM ecosystem conceptually but hadn't written production Java
- **Strong BDD background** - Familiar with Dave Farley's testing philosophy and Gang of Four patterns

### How I Built It

I used **Claude Code** (Anthropic's AI coding assistant) to help implement the architecture. The process was collaborative:

1. I provided the BDD principles and architectural direction (Dave Farley's approach)
2. Claude helped translate those concepts into Kotlin idioms
3. I guided decisions toward a more purist BDD approach when Claude suggested shortcuts
4. We iterated on the test DSL design until it read like specifications

The result isn't perfectly polished, but it demonstrates the core BDD concepts effectively.

---

## What This Project Demonstrates

### BDD Test Architecture

```
Test Cases (declarative, behavior-focused)
         │
         ▼
    Test DSL (semantic abstraction layer)
         │
         ▼
  Protocol Drivers (in-memory implementations)
         │
         ▼
   System Under Test
```

**Key principle**: Tests are stable; implementations are volatile. When you test behavior through interfaces, refactoring the implementation doesn't break your tests.

### Design Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| **Factory** | `LRUCacheProvider` | Hide implementation details |
| **Strategy** | `CreditPolicy` | Swappable business rules |
| **Decorator** | `CachedUserRepository` | Transparent caching layer |
| **Repository** | `UserRepository` | Abstract data access |
| **Sealed Class** | `AddUserResult` | Exhaustive result handling |

### Test Coverage

| Test Suite | Tests | Focus |
|------------|-------|-------|
| LruCacheTest | 9 | Cache mechanics |
| UserValidationBehaviorTest | 8 | Business rules |
| CreditLimitBehaviorTest | 3 | Credit policies |
| CacheBehaviorTest | 6 | Cache behavior |
| CacheIntegrationBehaviorTest | 5 | Cache + repository |
| ClientRepositoryBehaviorTest | 5 | Data access |
| **Total** | **36** | |

---

## Running the Tests

```bash
# Requires Java 17+
./gradlew test
```

---

## Project Structure

```
├── README.md                      # This file
├── ARCHITECTURE.md                # Detailed architecture guide
├── 1-Dave-Farley-BDD-principles.md  # BDD philosophy reference
│
├── src/main/kotlin/com/speechify/
│   ├── domain/                    # Immutable domain models
│   ├── repository/                # Data access layer
│   ├── service/                   # Business logic
│   └── policy/                    # Strategy implementations
│
└── src/test/kotlin/
    ├── dsl/                       # Test DSL (Given/When/Then)
    ├── drivers/                   # In-memory test doubles
    ├── fixtures/                  # Test data builders
    └── behavior/                  # BDD test specifications
```

---

## Further Reading

- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed patterns and control flow diagrams
- [1-Dave-Farley-BDD-principles.md](1-Dave-Farley-BDD-principles.md) - The BDD philosophy behind this approach

---

## Acknowledgments

Built with assistance from **Claude Code** (Claude Opus 4.5) - demonstrating how AI can help implement architectural patterns when given clear principles and direction.
