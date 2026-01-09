# BDD Example - Kotlin

`Hello there!`

This is a showcase of **Behavior-Driven Development (BDD)** principles applied to a user management service in Kotlin.

This repo demonstrates how to write "Given/When/Then" tests against unimplemented interfaces rather than writing ad-hoc unit tests against pre-existing implementations. You can recycle this structure for any project to remove most conventional codebase development and scaling issues that teams encounter.

## Background

`Context`

- Kotlin = 1/10  -  First Kotlin project ever (not as cool as Rust, but more fancy than Java).
- Java = 3/10  -  Hadn't used JVM ecosystem since back in uni. days.
- BDD = Good/10  -  Familiar with Dave Farley, Kent Becker, etc.
- AI = Great/10  -  Very familiar with Claude Code + VS Code.

`Process with Claude ~100 min`

Used Claude Code to implement & refactor the architecture following this procedure:

1. Provide the Dave-Farley-BDD-principles.md to claude and the architectural direction
2. Prompt Claude to convert the BDD concepts into Kotlin idioms
3. Feed Claude baby steps to stay on a purist BDD approach and held it's hand when it tried to take shortcuts
4. Iterated on the test DSL design and kept cleaning up until the codebase read like specifications.
5. Once you reach the desired functionality and pass tests, then ensure that you have async optimizations, clean naming, and good docs.

Then BAM, you've got a stable feature release.

---

`Unsolicited AI Agent Recommendation:`

- Claude Code is the Toyota of AI agents. It's backed by Amazon, AWS, and Anthropic. That is serious workplace culture right there.
- Let other people have fun driving Cursor (Audi), Augment AI (Mazda), or JetBrains (BMW). Repl.it (Honda) might be an interesting test drive...

## Further Reading

- [Principles.md](docs/Principles.md) - The Dave Farley BDD philosophy behind this approach
- [Architecture.md](docs/Architecture.md) - Detailed patterns and control flow diagrams
