# SmartTasker

SmartTasker is a **production-style** reference app for **synced tasks** across Android and a **Django REST** backend. It demonstrates a clear **Android → API → PostgreSQL → Docker → AWS-ready** path without rewriting the stack from scratch.

---

## 1. Project overview

| Part | Responsibility |
|------|----------------|
| **android-app** | Java client: **MVVM**, **Retrofit**, **LiveData**, JWT in **SharedPreferences**, Material UI. |
| **backend** | Django + **Django REST Framework** + **JWT** (Simple JWT), task CRUD scoped per user. |
| **PostgreSQL** | Single source of truth for users and tasks. |
| **Docker** | `web` (Gunicorn) + `db` (Postgres) with env-driven configuration. |

**Demonstrable flow:** Register → Login → Create task → List → **Update** / toggle complete → **Delete** → Logout.

---

## 2. Tech stack

| Layer | Technology |
|-------|------------|
| **Android** | Java 17, MVVM (ViewModel + LiveData), Retrofit 2, OkHttp, Gson, Material 3, RecyclerView, SwipeRefreshLayout |
| **Backend** | Django 4.2+, Django REST Framework, djangorestframework-simplejwt (access + refresh, rotation + blacklist) |
| **Database** | PostgreSQL (`psycopg2-binary`) |
| **Static files (prod)** | WhiteNoise + `collectstatic` |
| **Server** | Gunicorn, Docker |
| **Cloud** | AWS EC2–compatible: env-based config, TLS-aware proxy headers, Docker Compose |

---

## 3. Architecture

### Android (MVVM)

- **UI:** `LoginActivity`, `RegisterActivity`, `TaskListActivity`, `TaskEditActivity` (ViewBinding).
- **ViewModels:** `LoginViewModel`, `RegisterViewModel`, `TaskListViewModel`, `TaskEditViewModel` expose **LiveData** for UI state (loading, errors, task list, auth phases).
- **Data:** `AuthRepository` / `TaskRepository` call Retrofit; **no business logic in Activities** beyond wiring.
- **Networking:** `ApiClient` (singleton) + `ApiService` (`register`, `login`, `getTasks`, `createTask`, `updateTask`, `deleteTask`). `AuthInterceptor` adds `Authorization: Bearer <access>` except on public routes (`register/`, `login/`, `token/refresh/`, `health/`).
- **Tokens:** `TokenManager` persists access/refresh in app-private `SharedPreferences`.

### Backend

- **REST** + **JWT**; task querysets filtered by `request.user`.
- **PostgreSQL** via env (`DB_*` with `POSTGRES_*` fallbacks).
- **Production:** `DEBUG=False`, `ALLOWED_HOSTS`, WhiteNoise for static files, `SECURE_PROXY_SSL_HEADER` / `USE_X_FORWARDED_HOST` behind TLS-terminating proxies.

---

## 4. Backend setup

### 4.1 Docker (recommended)

```bash
cd SmartTasker
cp .env.example .env
# Set DJANGO_SECRET_KEY, DB_* passwords, DJANGO_ALLOWED_HOSTS for real deployments
docker compose up --build
```

- **API:** `http://localhost:8000/api/`
- **Migrations** and **`collectstatic`** run before Gunicorn.
- Postgres data is stored in the **`postgres_data`** volume.

### 4.2 Local (without Docker)

1. Install **PostgreSQL** and create a database/user matching your `.env`.
2. Python **3.11+**:

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

3. Configure **`.env`** in the repo root or **`backend/.env`** (see `.env.example`):

- `DJANGO_SECRET_KEY`, `DJANGO_DEBUG=True` for dev
- `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT`

4. Run:

```bash
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

5. Optional admin: `python manage.py createsuperuser` → `/admin/`.

With **`DEBUG=False`**, run **`python manage.py collectstatic --noinput`** before Gunicorn (Docker does this automatically).

---

## 5. Android setup

1. Open **`android-app/`** in **Android Studio** (JDK **17**).
2. **Base URL** (Retrofit): `BuildConfig.API_BASE_URL` defaults to **`http://10.0.2.2:8000/api/`**  
   - **Emulator:** `10.0.2.2` maps to the host machine’s `localhost` (port **8000** where Django runs).  
   - **Physical device:** set your PC’s LAN IP in **`android-app/local.properties`**:

```properties
smarttasker.api.baseUrl=http://192.168.x.x:8000/api/
```

3. Sync Gradle, run on an emulator or device.

Cleartext HTTP is allowed for local dev (`usesCleartextTraffic` + `network_security_config`). Use **HTTPS** behind a reverse proxy in production.

---

## 6. API endpoints

Base path: **`/api/`** (trailing slashes as shown).

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/register/` | No | `username`, `password` (min 8), optional `email` |
| `POST` | `/api/login/` | No | `username`, `password` → **`access`**, **`refresh`** |
| `POST` | `/api/token/refresh/` | No | `refresh` → new `access` (rotation + blacklist enabled) |
| `GET` | `/api/tasks/` | Bearer | List current user’s tasks |
| `POST` | `/api/tasks/` | Bearer | Create: `title`, `description`, `is_completed` |
| `PUT` | `/api/tasks/{id}/` | Bearer | Full update |
| `PATCH` | `/api/tasks/{id}/` | Bearer | Partial update |
| `DELETE` | `/api/tasks/{id}/` | Bearer | Delete |
| `GET` | `/api/health/` | No | `{"status":"ok"}` for load balancers |

**Header:** `Authorization: Bearer <access_token>`

---

## 7. AWS deployment (EC2)

1. **Launch** an EC2 instance (e.g. Amazon Linux 2 / Ubuntu with security group allowing **22** and **80** or **443** or your app port).
2. **Install Docker** (Docker Engine + Compose plugin) per AWS/Docker docs.
3. **Clone** the repo and **configure `.env`**:
   - `DJANGO_DEBUG=False`
   - Strong `DJANGO_SECRET_KEY`
   - `DJANGO_ALLOWED_HOSTS` = your public DNS or IP
   - `DB_*` pointing at your Postgres (RDS or container on same host)
4. **Run:**

```bash
docker compose up -d --build
```

5. **TLS:** Put **nginx** or **AWS ALB** in front; set `SECURE_PROXY_SSL_HEADER` / `USE_X_FORWARDED_HOST` (already enabled when `DEBUG=False`).
6. **Firewall:** Restrict inbound ports; prefer **HTTPS** on **443** and proxy to Gunicorn.

---

## 8. Screenshots (placeholders)

Add images under `docs/screenshots/`:

| Placeholder | Suggested file |
|-------------|----------------|
| Login | `docs/screenshots/login.png` |
| Task list | `docs/screenshots/tasks.png` |
| Edit task | `docs/screenshots/edit.png` |

---

## 9. Repository layout

```
SmartTasker/
├── backend/                 # Django project + tasks app
├── android-app/             # SmartTaskerApp
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## License

Sample project; adapt licensing for your product.
