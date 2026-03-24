# SmartTasker

Production-oriented full-stack task sync: **Android (Java, MVVM)** ↔ **Django REST + JWT** ↔ **PostgreSQL** ↔ **Docker** (AWS EC2–ready).

## Project overview

| Part | Description |
|------|-------------|
| **backend/** | Django project `backend`, app **`tasks`**: UUID tasks per user, JWT auth, PostgreSQL. |
| **android-app/** | **SmartTaskerApp** (`com.smarttasker.app`): Retrofit, ViewModel + LiveData, Material 3, JWT in `SharedPreferences`. |
| **docker-compose.yml** | **`web`** (Gunicorn + Django) + **`db`** (PostgreSQL), env-driven config. |

### End-to-end flow (verify locally)

1. Start API (Docker or local Postgres + `runserver`).
2. Android: default **emulator** base URL is `http://10.0.2.2:8000/api/` (see **Android setup** to override).
3. **Register** → **Login** → **Create task** → list updates → **Edit** / **toggle complete** / **Delete**.

## Tech stack

| Layer | Technology |
|-------|------------|
| API | Django 4.2+, Django REST Framework, **djangorestframework-simplejwt** (access + refresh, rotation + blacklist) |
| DB | **PostgreSQL** (psycopg2-binary), env: `DB_*` (and legacy `POSTGRES_*`) |
| Server | Gunicorn, Docker |
| Android | Java 17, MVVM, Retrofit + Gson, OkHttp, Material 3, RecyclerView, SwipeRefreshLayout |

## Backend configuration

Environment variables are read from **`backend/.env`** (if present) and the **repo root** `.env`. Prefer **PostgreSQL** with:

| Variable | Purpose |
|----------|---------|
| `DB_NAME` | Database name (fallback: `POSTGRES_DB`) |
| `DB_USER` | User (fallback: `POSTGRES_USER`) |
| `DB_PASSWORD` | Password (fallback: `POSTGRES_PASSWORD`) |
| `DB_HOST` | Host (fallback: `POSTGRES_HOST`; default `localhost`) |
| `DB_PORT` | Port (fallback: `POSTGRES_PORT`; default `5432`) |
| `DJANGO_SECRET_KEY` | Required in production |
| `DJANGO_DEBUG` | `True` / `False` |
| `DJANGO_ALLOWED_HOSTS` | Comma-separated hosts |
| `JWT_ACCESS_MINUTES`, `JWT_REFRESH_DAYS` | Optional |

Copy **`.env.example`** to **`.env`** and adjust.

## Backend setup (Docker — recommended)

Requires Docker Engine.

```bash
cd SmartTasker
cp .env.example .env
# Set DJANGO_SECRET_KEY, passwords, DJANGO_ALLOWED_HOSTS for real deployments
docker compose up --build
```

- API base: **`http://localhost:8000/api/`**
- Migrations run on container start (`migrate` + Gunicorn).
- Postgres data is stored in the **`postgres_data`** volume.

## Backend setup (local, without Docker)

1. Install **PostgreSQL** and create a database/user matching your `.env` / exports.
2. Python **3.11+**:

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

3. Export or place in **`.env`** (repo root or `backend/`):

```bash
export DJANGO_SECRET_KEY="dev-secret-change-me"
export DJANGO_DEBUG=True
export DB_NAME=smarttasker
export DB_USER=smarttasker
export DB_PASSWORD=smarttasker
export DB_HOST=localhost
export DB_PORT=5432
```

4. Migrate and run:

```bash
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

5. Optional admin: `python manage.py createsuperuser` → `/admin/`.

## Android setup

1. Install **Android Studio** with **JDK 17**.
2. Open **`android-app/`**.
3. **API base URL** (BuildConfig `API_BASE_URL`):
   - **Emulator** (default): `http://10.0.2.2:8000/api/` (already the default if you do not set `local.properties`).
   - **Physical device**: use your computer’s **LAN IP**, e.g. `http://192.168.1.10:8000/api/`, same Wi‑Fi as the phone.
4. Create **`android-app/local.properties`** (gitignored) or copy from **`local.properties.example`**:

```properties
smarttasker.api.baseUrl=http://YOUR_IP:8000/api/
```

5. Sync Gradle and run the app.

Cleartext HTTP is enabled for development (`usesCleartextTraffic` + `network_security_config`). Use **HTTPS** behind a reverse proxy in production.

## API reference

Base path: **`/api/`** (include trailing slashes).

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/register/` | No | Register. JSON: `username`, `password` (min 8), optional `email`. |
| `POST` | `/api/login/` | No | JWT login. JSON: `username`, `password`. Response: **`access`**, **`refresh`**. |
| `POST` | `/api/token/refresh/` | No | Refresh JSON: `refresh` → new `access` (and rotated `refresh` when rotation enabled). |
| `GET` | `/api/tasks/` | Bearer | List current user’s tasks. |
| `POST` | `/api/tasks/` | Bearer | Create task. JSON: `title`, `description`, `is_completed`. |
| `PUT` | `/api/tasks/{id}/` | Bearer | Full update (same fields). |
| `PATCH` | `/api/tasks/{id}/` | Bearer | Partial update. |
| `DELETE` | `/api/tasks/{id}/` | Bearer | Delete task. |
| `GET` | `/api/health/` | No | Liveness (`{"status":"ok"}`) for load balancers. |

**Authorization:** `Authorization: Bearer <access_token>`

**Security:** Task list/detail querysets are scoped to **`request.user`**; unauthenticated users cannot access task endpoints (global `IsAuthenticated` except register/login/refresh/health).

### Example requests

**Register**

```http
POST /api/register/
Content-Type: application/json

{"username": "alice", "password": "secret12345", "email": ""}
```

**Login**

```http
POST /api/login/
Content-Type: application/json

{"username": "alice", "password": "secret12345"}
```

**Create task**

```http
POST /api/tasks/
Authorization: Bearer <access>
Content-Type: application/json

{"title": "Buy milk", "description": "", "is_completed": false}
```

## AWS (EC2) notes

- Install Docker, clone the repo, configure **`.env`** (`DJANGO_ALLOWED_HOSTS`, strong secrets, DB if managed RDS).
- Open the app port (e.g. **8000**) or place **nginx/ALB** in front with TLS.
- Set `DEBUG=False`; `SECURE_PROXY_SSL_HEADER` is enabled when `DEBUG` is off for TLS-terminated proxies.

## Screenshots (placeholders)

Add PNGs under `docs/screenshots/` if you use these paths:

![Login](docs/screenshots/login.png)

![Tasks](docs/screenshots/tasks.png)

![Edit](docs/screenshots/edit.png)

## Repository layout

```
SmartTasker/
├── backend/
│   ├── Dockerfile
│   ├── manage.py
│   ├── requirements.txt
│   ├── backend/          # settings, urls, wsgi
│   └── tasks/               # models, serializers, views, urls, migrations
├── android-app/
│   ├── local.properties.example
│   └── app/...
├── docker-compose.yml
├── .env.example
└── README.md
```

## License

Sample project for SmartTasker; adjust for your product.
