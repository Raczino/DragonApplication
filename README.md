# Dragon Application

A Spring Boot application with Postgres and Redis, containerized using Docker Compose.  
Includes a custom **PowerShell CLI (`dragon`)** for faster development and management.

Dragon Application is an **article service** that allows:

- Creating and managing articles
- Adding hashtags and likes
- Download posts from Reddit with the same hashtag as comments under your posts
- Handling comments and notifications
- Managing subscriptions and premium features
- Creating surveys and collecting statistics
- Follow other interesting users and view their articles

---

## Requirements

- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- [Maven](https://maven.apache.org/) (for building JARs during restart)
- PowerShell (with `dragon` function configured in your `$PROFILE`)

---

## Getting Started

### 1. Clone the repository

```powershell
git clone https://github.com/Raczino/DragonApplication.git
cd DragonApplication
```

#### 2. Start Services

- Use dragon up
- The app will be available at: http://localhost:8080

##### 3. Dragon CLI Commands

| Command                   | Description                                                                 |
|---------------------------|-----------------------------------------------------------------------------|
| `dragon up`               | Start all services quickly (no Maven build, no image rebuild)               |
| `dragon down`             | Stop and remove containers and networks                                     |
| `dragon restart`          | Build JAR with Maven (skip tests), rebuild **only app image**, start app    |
| `dragon logs`             | Show application logs (follow mode)                                         |
| `dragon ps`               | List containers from the compose project                                    |
| `dragon status`           | Show detailed status (name, image, status, ports)                           |
| `dragon db`               | Open a `psql` shell in the Postgres container                               |
| `dragon redis`            | Open a `redis-cli` shell in the Redis container                             |
| `dragon prune`            | Remove unused containers, networks, dangling images, build cache (**asks**) |
| `dragon prune:force`      | Same as `prune` but executes **without asking** (⚠ irreversible cleanup)    |
| `dragon prune:images`     | Remove dangling images only                                                 |
| `dragon prune:images:all` | Remove **all unused images** (not just dangling)                            |
| `dragon prune:volumes`    | Remove unused volumes (⚠ may delete stored data)                            |
| `dragon prune:builder`    | Remove build cache                                                          |
