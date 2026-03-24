from django.contrib.auth.models import User
from rest_framework import generics, status
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.views import TokenObtainPairView

from .models import Task
from .serializers import TaskSerializer, UserRegistrationSerializer


class RegisterView(generics.CreateAPIView):
    """POST /api/register/ — create a new user account."""

    queryset = User.objects.all()
    permission_classes = (AllowAny,)
    serializer_class = UserRegistrationSerializer


class LoginView(TokenObtainPairView):
    """POST /api/login/ — obtain JWT access + refresh tokens."""

    permission_classes = (AllowAny,)


class TaskListCreateView(generics.ListCreateAPIView):
    """GET/POST /api/tasks/ — list or create tasks for the current user."""

    serializer_class = TaskSerializer
    permission_classes = (IsAuthenticated,)

    def get_queryset(self):
        return Task.objects.filter(user=self.request.user)


class TaskDetailView(generics.RetrieveUpdateDestroyAPIView):
    """GET/PUT/PATCH/DELETE /api/tasks/<id>/ — single task (owner only)."""

    serializer_class = TaskSerializer
    permission_classes = (IsAuthenticated,)
    lookup_field = "id"

    def get_queryset(self):
        return Task.objects.filter(user=self.request.user)


class HealthView(APIView):
    """Optional liveness check for load balancers (not in user API list but useful for EC2)."""

    permission_classes = (AllowAny,)

    def get(self, request):
        return Response({"status": "ok"}, status=status.HTTP_200_OK)
