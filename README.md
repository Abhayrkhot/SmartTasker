# SmartTasker

**SmartTasker** is a full-stack task management demo: users **register**, **log in with JWT**, and **sync tasks** (create, read, update, delete, mark complete) between an **Android** app and a **Django REST** API backed by **PostgreSQL**, packaged with **Docker** and ready to deploy on **AWS EC2**.

---

## Demo flow

1. **Register** a user (`POST /api/register/`).
2. **Login** and receive **access** + **refresh** JWTs (`POST /api/login/`).
3. **Create tasks**, **list** them, **edit**, **toggle complete**, **delete** — all scoped to the logged-in user.
4. **Logout** clears tokens on the device.

**Quick API check:** `GET /api/health/` → `{"status":"ok"}`.

---

## Tech stack

| Area | Stack |
|------|--------|
| **Android** | Java 17, **MVVM** (ViewModel + LiveData), **Retrofit** + OkHttp + Gson, Material 3, RecyclerView, SharedPreferences for JWT |
| **Backend** | **Django**, **Django REST Framework**, **JWT** (Simple JWT, refresh rotation + blacklist) |
| **Database** | **PostgreSQL** |
| **Runtime** | **Gunicorn**, **Docker** / Docker Compose |
| **Cloud** | **AWS EC2** (Ubuntu + Docker): env-based config, TLS-friendly proxy headers |

---

## Architecture (high level)

```
┌─────────────────┐     HTTPS/HTTP      ┌──────────────────┐     ┌──────────────┐
│  Android app    │ ──────────────────► │  Django + DRF    │ ──► │ PostgreSQL   │
│  MVVM + Retrofit│   Authorization:    │  JWT + tasks API │     │ (Docker db)  │
│                 │   Bearer <access>   │  Gunicorn :8000  │     └──────────────┘
└─────────────────┘                     └──────────────────┘
```

- **Android:** UI → **ViewModels** → **Repositories** → **Retrofit** (`ApiService`). **`AuthInterceptor`** adds `Authorization: Bearer <token>` for protected routes; tokens live in **`SharedPreferences`** via **`TokenManager`**.
- **Backend:** URL routes under **`/api/`**. Tasks are filtered by **`request.user`**. **Static files** for admin in production are served via **WhiteNoise** after **`collectstatic`**.

---

## Backend setup (Docker — recommended)

**Prerequisites:** [Docker Engine](https://docs.docker.com/engine/install/) + Compose plugin.

```bash
git clone https://github.com/Abhayrkhot/SmartTasker.git
cd SmartTasker
cp .env.example .env
# Edit .env: set SECRET_KEY, DB passwords, etc.
docker compose up --build -d
```

- **API:** `http://localhost:8000/api/`
- On start, the **web** container runs **`migrate`**, **`collectstatic`**, then **Gunicorn** on **port 8000**.
- **PostgreSQL** persists in the **`postgres_data`** volume.

**Local development without Docker:** use `DJANGO_DEBUG=True` in `.env`, run Postgres locally, then:

```bash
cd backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

---

## Android setup

1. Open **`android-app/`** in **Android Studio** (bundled **JDK 17**).
2. **Base URL** is **`BuildConfig.API_BASE_URL`**:
   - **Emulator + backend on the same machine:** default **`http://10.0.2.2:8000/api/`** (`10.0.2.2` is the host loopback from the emulator).
   - **Physical device:** set your PC’s LAN IP in **`android-app/local.properties`** (see **`local.properties.example`**):

```properties
smarttasker.api.baseUrl=http://YOUR_LAN_OR_EC2_IP:8000/api/
```

3. **Run** the app on an emulator or device.

### Build a release APK (for demos / installs)

1. **Android Studio:** **Build → Generate Signed Bundle / APK → APK** (create or reuse a keystore).
2. Or **Build → Build Bundle(s) / APK(s) → Build APK(s)** (debug APK for quick sharing).

Output: **`android-app/app/build/outputs/apk/`** (variant folder).

**Optional:** Attach APKs to **[GitHub Releases](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases)** for reviewers (upload the signed APK as a release asset).

---

## API reference

**Base path:** `/api/` (trailing slashes as shown).  
**Auth header (protected routes):** `Authorization: Bearer <access_token>`

### Auth & health

| Method | Path | Auth | Request body | Success response |
|--------|------|------|--------------|------------------|
| `POST` | `/api/register/` | No | `{"username":"string","password":"string","email":"string (optional)"}` | `201` — `{"id":int,"username":"…","email":"…"}` |
| `POST` | `/api/login/` | No | `{"username":"string","password":"string"}` | `200` — `{"access":"jwt","refresh":"jwt"}` |
| `POST` | `/api/token/refresh/` | No | `{"refresh":"jwt"}` | `200` — new access (and refresh when rotating) |
| `GET` | `/api/health/` | No | — | `200` — `{"status":"ok"}` |

### Tasks (owner-only)

| Method | Path | Auth | Request body | Success response |
|--------|------|------|--------------|------------------|
| `GET` | `/api/tasks/` | Bearer | — | `200` — `[{ "id":"uuid","title":"…","description":"…","is_completed":bool,"created_at":"…","updated_at":"…" }, …]` |
| `POST` | `/api/tasks/` | Bearer | `{"title":"string","description":"string","is_completed":bool}` | `201` — task object |
| `PUT` | `/api/tasks/{id}/` | Bearer | Same as POST | `200` — task object |
| `PATCH` | `/api/tasks/{id}/` | Bearer | Partial fields | `200` — task object |
| `DELETE` | `/api/tasks/{id}/` | Bearer | — | `204` No Content |

Errors typically return JSON with `detail` or field errors (`400`/`401`/`403`/`404`).

---

## Screenshots (placeholders)

Add images under `docs/screenshots/` and link them here for portfolio/README polish.

| Screen | Placeholder path |
|--------|------------------|
| Login | `docs/screenshots/login.png` |
| Register | `docs/screenshots/register.png` |
| Task list | `docs/screenshots/tasks.png` |
| Add / edit task | `docs/screenshots/add-task.png` |

---

## Deployment on AWS EC2 (Ubuntu)

Use this for a **public demo** (HTTP on port **8000**). For production traffic, add **HTTPS** (nginx/Caddy) or an **Application Load Balancer**.

### 1. Launch EC2

- **AMI:** Ubuntu Server 22.04 LTS (or newer).
- **Instance type:** `t3.micro` or larger.
- **Security group:** allow **SSH (22)** from your IP; allow **Custom TCP 8000** from `0.0.0.0/0` for the demo API (or restrict to your IP).

### 2. Connect and install Docker

```bash
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker ubuntu
# Log out and back in, or: newgrp docker
```

### 3. Clone and configure

```bash
git clone https://github.com/Abhayrkhot/SmartTasker.git
cd SmartTasker
cp .env.example .env
nano .env   # set SECRET_KEY, passwords; keep DJANGO_DEBUG=False; DJANGO_ALLOWED_HOSTS=* for demo
```

### 4. Run

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f web
```

### 5. Access

- **API:** `http://<EC2_PUBLIC_IP>:8000/api/`
- **Health:** `http://<EC2_PUBLIC_IP>:8000/api/health/`

### 6. Android against EC2

Set **`smarttasker.api.baseUrl`** in **`android-app/local.properties`** to:

```properties
smarttasker.api.baseUrl=http://<EC2_PUBLIC_IP>:8000/api/
```

Rebuild and install the app.

---

## Production / security notes

- **Secrets:** Set **`SECRET_KEY`** (or **`DJANGO_SECRET_KEY`**) in `.env` — never commit real values.
- **`DJANGO_DEBUG`:** **`False`** in production (default in **`.env.example`** and Compose).
- **`DJANGO_ALLOWED_HOSTS`:** **`*`** is convenient for demos; tighten to your domain or EC2 DNS for real deployments.
- **HTTPS:** Terminate TLS at nginx/ALB; Django is configured to respect **`X-Forwarded-Proto`** / **`X-Forwarded-Host`** when **`DEBUG`** is off.

---

## Repository layout

```
SmartTasker/
├── backend/              # Django project (Docker build context)
├── android-app/          # Android Studio project
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## License

Sample / portfolio project — adjust licensing for your own use.
