from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView

from .views import HealthView, LoginView, RegisterView, TaskDetailView, TaskListCreateView

urlpatterns = [
    path("register/", RegisterView.as_view(), name="register"),
    path("login/", LoginView.as_view(), name="login"),
    path("token/refresh/", TokenRefreshView.as_view(), name="token-refresh"),
    path("tasks/", TaskListCreateView.as_view(), name="task-list-create"),
    path("tasks/<uuid:id>/", TaskDetailView.as_view(), name="task-detail"),
    path("health/", HealthView.as_view(), name="health"),
]
