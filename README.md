# PX3 – Java/SpringBoot Backend Building Blocks

Welcome to PX3.

This is a collection of Java modules for people who actually build things. PX3 handles the “boring but essential” backend stuff: moving data around (Kafka), storing it (MongoDB), logging people in (JWT), and keeping configuration and API code out of your hair.

It's modular so that you can use only what fits your project.

---

## What’s in the Box?

- **px3-auth**  
  Does authentication and user management with JWT. Plug it in by itself, or use it as a bridge to Auth0 if you don’t want to reinvent the wheel.

- **px3-config-hub**  
  Centralizes configuration. If you’ve ever lost a config file, this might help.

- **px3-crud-mongo**  
  MongoDB CRUD for grown-ups. Models, validation, access control—boring, but you’ll thank yourself later.

- **px3-pipe-kafka**  
  Kafka bits for sending, reading, and buffering messages. Useful for data pipelines, event-driven stuff, and whenever you wish things were synchronous (they’re not).

- **px3-rest**  
  A small pile of REST helpers and base classes.

- **px3-utils**  
  Utility belt: helpers, small hacks, random bits.

- **px3-utils-auth**  
  Helpers for authentication. Works behind the scenes, so you don’t have to.

- **px3-utils-mongo**  
  MongoDB-specific helpers. Some glue, some sugar.

---

## Why Does PX3 Exist?

- You need to build a backend, not a framework.
- You like working with Java, Spring Boot, and the latest versions.
- You hate writing the same boilerplate every project.
- You’d rather write features than config files, JWT logic, or Kafka producers for the hundredth time.

---

## Quickstart

You’ll need:
- Java 21+
- Gradle (or the wrapper)
- Docker (if you want to try the Kafka tests)

How to build:

```sh
git clone https://github.com/your-org/px3.git
cd px3
./gradlew build
