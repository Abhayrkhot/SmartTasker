"""
Django settings for SmartTasker backend.
Uses environment variables for secrets (12-factor / AWS EC2 friendly).
"""
from __future__ import annotations

import os
from datetime import timedelta
from pathlib import Path
from typing import Optional

from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent

# Load .env from backend/ first, then repo root (Docker / local / AWS).
load_dotenv(BASE_DIR / ".env")
load_dotenv(BASE_DIR.parent / ".env")


def _env_first(*keys: str, default: Optional[str] = None) -> Optional[str]:
    """Return the first non-empty environment value for the given keys."""
    for key in keys:
        val = os.environ.get(key)
        if val is not None and str(val).strip() != "":
            return val
    return default


# Never commit real secrets. Use SECRET_KEY or DJANGO_SECRET_KEY in .env / the host environment.
SECRET_KEY = _env_first("SECRET_KEY", "DJANGO_SECRET_KEY", default="dev-only-change-before-production")

# Production: DJANGO_DEBUG=False (see .env.example). Local dev: set DJANGO_DEBUG=True.
DEBUG = os.environ.get("DJANGO_DEBUG", "False").lower() in ("1", "true", "yes")

# Behind AWS ALB / nginx with TLS termination, trust forwarded headers when DEBUG is False.
if not DEBUG:
    SECURE_PROXY_SSL_HEADER = ("HTTP_X_FORWARDED_PROTO", "https")
    USE_X_FORWARDED_HOST = True

# EC2 / Docker: set DJANGO_ALLOWED_HOSTS=* for broad access during demos, or list explicit hosts for production.
_allowed_raw = os.environ.get("DJANGO_ALLOWED_HOSTS", "*").strip()
if _allowed_raw == "*":
    ALLOWED_HOSTS = ["*"]
else:
    ALLOWED_HOSTS = [h.strip() for h in _allowed_raw.split(",") if h.strip()]

INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "rest_framework",
    "rest_framework_simplejwt",
    "rest_framework_simplejwt.token_blacklist",
    "tasks",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "whitenoise.middleware.WhiteNoiseMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "backend.urls"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.debug",
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

WSGI_APPLICATION = "backend.wsgi.application"

# PostgreSQL only (production / Docker / local). Use DB_* or legacy POSTGRES_* vars.
DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": _env_first("DB_NAME", "POSTGRES_DB", default="smarttasker"),
        "USER": _env_first("DB_USER", "POSTGRES_USER", default="smarttasker"),
        "PASSWORD": _env_first("DB_PASSWORD", "POSTGRES_PASSWORD", default="smarttasker"),
        "HOST": _env_first("DB_HOST", "POSTGRES_HOST", default="localhost"),
        "PORT": _env_first("DB_PORT", "POSTGRES_PORT", default="5432"),
    }
}

AUTH_PASSWORD_VALIDATORS = [
    {"NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator"},
    {"NAME": "django.contrib.auth.password_validation.MinimumLengthValidator"},
    {"NAME": "django.contrib.auth.password_validation.CommonPasswordValidator"},
    {"NAME": "django.contrib.auth.password_validation.NumericPasswordValidator"},
]

LANGUAGE_CODE = "en-us"
TIME_ZONE = "UTC"
USE_I18N = True
USE_TZ = True

STATIC_URL = "static/"
STATIC_ROOT = BASE_DIR / "staticfiles"

# In production, serve collected static files via WhiteNoise (admin CSS/JS without nginx).
if not DEBUG:
    STORAGES = {
        "default": {
            "BACKEND": "django.core.files.storage.FileSystemStorage",
        },
        "staticfiles": {
            "BACKEND": "whitenoise.storage.CompressedManifestStaticFilesStorage",
        },
    }

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": (
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ),
    "DEFAULT_PERMISSION_CLASSES": ("rest_framework.permissions.IsAuthenticated",),
}

SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(minutes=int(os.environ.get("JWT_ACCESS_MINUTES", "60"))),
    "REFRESH_TOKEN_LIFETIME": timedelta(days=int(os.environ.get("JWT_REFRESH_DAYS", "7"))),
    "ROTATE_REFRESH_TOKENS": True,
    "BLACKLIST_AFTER_ROTATION": True,
}
