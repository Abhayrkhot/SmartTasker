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


# Secrets: set SECRET_KEY (or DJANGO_SECRET_KEY) in the environment / .env — never commit real values.
SECRET_KEY = _env_first("SECRET_KEY", "DJANGO_SECRET_KEY", default="dev-only-change-before-production")

# EC2 / Docker production: DEBUG must be False. For local dev only, set DJANGO_DEBUG=True in .env.
DEBUG = os.environ.get("DJANGO_DEBUG", "False").lower() in ("1", "true", "yes")

# Behind AWS ALB / nginx with TLS termination, trust forwarded headers when DEBUG is False.
if not DEBUG:
    SECURE_PROXY_SSL_HEADER = ("HTTP_X_FORWARDED_PROTO", "https")
    USE_X_FORWARDED_HOST = True

# AWS EC2 / Docker: allow all hosts (restrict via security groups + HTTPS in front for real production).
ALLOWED_HOSTS = ["*"]

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

# PostgreSQL — configure via DB_* (and POSTGRES_* fallbacks). Matches docker-compose `.env.example`.
DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": _env_first("DB_NAME", "POSTGRES_DB", default="postgres"),
        "USER": _env_first("DB_USER", "POSTGRES_USER", default="postgres"),
        "PASSWORD": _env_first("DB_PASSWORD", "POSTGRES_PASSWORD", default="postgres"),
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
STATIC_ROOT = os.path.join(BASE_DIR, "staticfiles")

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
