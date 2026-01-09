# BDD Example - Kotlin

Hello there! This is a showcase of **Behavior-Driven Development (BDD)** principles applied to a Claude Code codebase (technically Kotlin, but that's a side point). This repo demonstrates how to write "Given/When/Then" tests against unimplemented interfaces rather than writing ad-hoc unit tests against pre-existing implementations. This boiler plate can be reused in any project in any computer language to remove most of the conventional codebase development and scaling issues that teams encounter. You can search YouTube for interviews with Kent Beck and Dave Farley to learn the origins of Agile, TDD, BDD, Xtreme Programming, etc. Or you can chat with me to get the rundown.

---

## Background

### My Starting Point

- **Zero Kotlin experience** - This was my first Kotlin project
- **Minimal Java knowledge** - I used the JVM ecosystem way back in uni. days but hadn't written Java since then.
- **Strong BDD background** - Familiar with Dave Farley's testing philosophy and Gang of Four patterns. This builds on Kent Beck, Bob Martin, and Robert Fowler's work.

### How I built it in ~100 min

I used Claude Code to help implement the architecture. The process was collaborative:

1. I provided the Dave-Farley-BDD-principles.md to claude and the architectural direction
2. Claude converted those concepts into Kotlin idioms
3. I fed Claude baby steps to stay on a purist BDD approach and held it's hand when it tried to take shortcuts
4. We just iterated on the test DSL design and kept cleaning up until the codebase read like specifications.
5. Once you reach the desired functionality and pass tests, then ensure that you have async optimizations, clean naming, and good docs.

---

## Further Reading

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - Detailed patterns and control flow diagrams
- [1-Dave-Farley-BDD-principles.md](docs/1-Dave-Farley-BDD-principles.md) - The BDD philosophy behind this approach
