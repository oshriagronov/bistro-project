# Bistro Project

## Purpose

Group project for Braude College.

Client/Server Java project with a GUI client and a server that connects to a database.

## Structure

- `Client/` – GUI client (see `Client/src/gui/Main.java`)
- `Server/` – server + DB connection (see `Server/src/server/Main.java`)

## Prerequisites

- Java (JDK 8+)
- A reachable database configured in `Server/src/server/ConnectionToDB.java`

## Database setup

- Use the provided `.sql` script in the repository to create/build the database schema and initial data (run it on your DB server before starting the app).

## Run

1. Start the server:
   - Run `Server/src/server/Main.java`
2. Start the client:
   - Run `Client/src/gui/Main.java`
