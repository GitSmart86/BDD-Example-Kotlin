# BDD Example - Kotlin

**Hello there!**

This is a showcase of **Behavior-Driven Development (BDD)** principles applied to a user management service in Kotlin.

1. **Use:** This repo demonstrates how to write "Given/When/Then" tests against unimplemented interfaces rather than writing ad-hoc unit tests against pre-existing implementations.

2. **Reuse:** You can recycle this boiler plate as a reference for any project to remove most conventional codebase development and scaling issues that teams encounter.

3. **Research:** Search YouTube for interviews with Kent Beck and Dave Farley to learn the origins of this BDD, TDD, Agile, SOLID, Xtreme Programming and stuff, or just chat with me to get the rundown. Its a really cool backstory for those who are curious.

---

## Background

### Starting Point

- **Zero Kotlin experience** - This was my first Kotlin project ever (not as cool as Rust, but more fancy than Java).
- **Minimal Java knowledge** - I hadn't used the JVM ecosystem since way back in uni. days.
- **Strong BDD background** - Familiar with Dave Farley's testing philosophy and Gang of Four patterns.
- **Stronger Agentic AI background** - Very familiar with Claude Code + VS Code. `Personally, Claude Code is the Toyota of AI agents. Other people can have fun with Cursor (Audi) or Augment AI (Mazda). Its hard to beat the brainchild of Amazon, AWS, and Anthropic.`

### How to build fast with Claude ~100 min

I used Claude Code to implement & refactor the architecture following this procedure:

1. I provided the Dave-Farley-BDD-principles.md to claude and the architectural direction
2. Claude converted those concepts into Kotlin idioms
3. I fed Claude baby steps to stay on a purist BDD approach and held it's hand when it tried to take shortcuts
4. We just iterated on the test DSL design and kept cleaning up until the codebase read like specifications.
5. Once you reach the desired functionality and pass tests, then ensure that you have async optimizations, clean naming, and good docs.

Then BAM, you've got a stable feature release.

---

## Further Reading

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - Detailed patterns and control flow diagrams
- [1-Dave-Farley-BDD-principles.md](docs/1-Dave-Farley-BDD-principles.md) - The BDD philosophy behind this approach
