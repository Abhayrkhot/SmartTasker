# SmartTasker

Cross-device task management: a **Django REST Framework** backend with **JWT** authentication and a **Java** Android client using **MVVM**, **Retrofit**, **LiveData**, and **ViewModel**.

## Project overview

- **backend/** — Django project `backend`, app `tasks`; PostgreSQL; JWT via `djangorestframework-simplejwt`.
- **android-app/** — Android Studio project **SmartTaskerApp** (`com.smarttasker.app`).
- **docker-compose.yml** — `web` (Gunicorn + Django) and `db` (PostgreSQL), suitable for local runs or **AWS EC2** (install Docker, open security group port, set `DJANGO_ALLOWED_HOSTS` and secrets).

## Tech stack

| Layer    | Technology |
|----------|------------|
| API      | Django 4.2+, Django REST Framework, Simple JWT |
| Database | PostgreSQL (psycopg2-binary) |
| Server   | Gunicorn, Docker |
| Android  | Java 17, MVVM, Retrofit, OkHttp, Material 3, LiveData, ViewModel, RecyclerView |

## Backend setup (local, without Docker)

1. Install **PostgreSQL** and create a database and user (or match defaults below).
2. Python 3.11+ recommended:

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

3. Environment (example):

```bash
export DJANGO_SECRET_KEY="dev-secret-change-me"
export DJANGO_DEBUG=True
export POSTGRES_DB=smarttasker
export POSTGRES_USER=smarttasker
export POSTGRES_PASSWORD=smarttasker
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
```

4. Migrate and run:

```bash
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

5. Create an optional admin user: `python manage.py createsuperuser` — admin UI at `/admin/`.

Copy **`.env.example`** to **`.env`** at the repo root when using Docker or documenting EC2 variables.

## Android setup

1. Install **Android Studio** (bundled **JDK 17**).
2. Open **`android-app/`**.
3. **Base URL**: in `app/build.gradle.kts`, `buildConfigField` sets `API_BASE_URL` (default `http://10.0.2.2:8000/api/` for the emulator). For a **physical device**, use your computer’s LAN IP, e.g. `http://192.168.1.10:8000/api/`, and ensure the device and server share a network.
4. Sync Gradle and run on an emulator or device.

Cleartext HTTP is allowed for local development via `network_security_config` and `usesCleartextTraffic`. Use **HTTPS** behind a reverse proxy in production.

## Run with Docker

Requires Docker Engine.

```bash
cd SmartTasker
cp .env.example .env
# Edit .env: set DJANGO_SECRET_KEY, POSTGRES_PASSWORD, DJANGO_ALLOWED_HOSTS as needed
docker compose up --build
```

- API: `http://localhost:8000/api/`
- PostgreSQL is not exposed to the host unless you keep the `db` ports mapping (default `5432`).

For **EC2**: install Docker, copy the project, set `.env` (including `DJANGO_ALLOWED_HOSTS` to your public DNS or IP), run `docker compose up -d`, and open the app port (e.g. `8000`) in the security group.

## API documentation

Base path: **`/api/`** (trailing slashes as shown).

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/register/` | No | Register user. Body: `username`, `password` (min 8), optional `email`. |
| `POST` | `/api/login/` | No | JWT login. Body: `username`, `password`. Response: `access`, `refresh`. |
| `GET` | `/api/tasks/` | Bearer | List current user’s tasks. |
| `POST` | `/api/tasks/` | Bearer | Create task. Body: `title`, `description`, `is_completed`. |
| `PUT` | `/api/tasks/{id}/` | Bearer | Full update (same fields as create). |
| `DELETE` | `/api/tasks/{id}/` | Bearer | Delete task. |

**Authorization header:** `Authorization: Bearer <access_token>`

**Example — register**

```http
POST /api/register/
Content-Type: application/json

{"username": "alice", "password": "secret12345", "email": ""}
```

**Example — login**

```http
POST /api/login/
Content-Type: application/json

{"username": "alice", "password": "secret12345"}
```

**Example — create task**

```http
POST /api/tasks/
Authorization: Bearer <access>
Content-Type: application/json

{"title": "Buy milk", "description": "", "is_completed": false}
```

Optional health check for load balancers: `GET /api/health/`.

## Screenshots (placeholders)

Replace these with real screenshots after you run the app:

![Login screen](docs/screenshots/login.png)

![Task list](docs/screenshots/tasks.png)

![Task edit](docs/screenshots/edit.png)

(Add image files under `docs/screenshots/` if you use the paths above.)

## Repository layout

```
SmartTasker/
├── backend/                 # Django project
│   ├── Dockerfile
│   ├── manage.py
│   ├── requirements.txt
│   ├── backend/             # settings, urls, wsgi
│   └── tasks/               # models, serializers, views, urls, migrations
├── android-app/             # Android (SmartTaskerApp)
├── docker-compose.yml
├── .env.example
└── README.md
```

## License

Provided as sample code for SmartTasker; adjust licensing for your product.
