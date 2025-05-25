# Qobuz Tmux API

Java-based REST API server for searching and downloading tracks from Qobuz using [`streamrip`](https://github.com/beurtschipper/streamrip) inside a virtual environment managed by `tmux`. Inspired by Flask-based CLI tools but implemented using:
- `tmux` to keep stateful CLI session
- Embedded Jetty (Java HTTP server)
- Jackson for JSON serialization
- SLF4J logging

---

## Features:

- `/search?param=<query>` — Searches tracks on Qobuz using `rip search`
- `/download?number=<n>` — Selects and downloads the `n`-th track from the search results
- Session managed via `tmux` — simulating user input and output

---

## Requirements:

- Java 17+
- Python 3.12+
- Linux|
- `tmux`
-`bash`
- Python virtual environment with installed `streamrip`
- Qobuz subscription


## Prerequisites:

```bash
python3 -m venv /opt/venvs/venv_1
source /opt/venvs/venv_1/bin/activate
pip install streamrip
```

## Running:

```bash 
git clone https://github.com/manpilogoff/qobuz-cli-api.git
cd qobuz-tmux-api
mvn compile exec:java -Dexec.mainClass="org.example.Main"  # or run Main.java via your IDE
```

## API 

  - GET /search?param=<query> | param (required): Search query
```bash
curl http://localhost:5000/search?param=<value>
``` 

  - GET /download?number=<num> | number (required): Track number
```bash
curl http://localhost:5000/download?number=<num>
```


