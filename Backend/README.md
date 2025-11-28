# API Documentation - My Safe Haven Backend

## 🔐 Autenticación

Todas las rutas protegidas requieren el header:
```
Authorization: Bearer <token_jwt>
```

### POST `/register`
Registra un nuevo usuario.

**Request Body:**
```json
{
  "username": "usuario123",
  "mail": "usuario@email.com",
  "password": "password123",
  "profile_image_path": "opcional/path/imagen.jpg"
}
```

**Response (201):**
```json
{
  "message": "Usuario registrado exitosamente",
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "user": {
    "id": 1,
    "username": "usuario123",
    "mail": "usuario@email.com",
    "profile_image_path": null
  }
}
```

### POST `/login`
Inicia sesión con username o email.

**Request Body:**
```json
{
  "username": "usuario123",
  "password": "password123"
}
```
O con email:
```json
{
  "mail": "usuario@email.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "message": "Login exitoso",
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "user": {
    "id": 1,
    "username": "usuario123",
    "mail": "usuario@email.com",
    "profile_image_path": null
  }
}
```

---

## 👤 Usuarios

### GET `/users/me` 🔒
Obtiene información del usuario autenticado.

**Response (200):**
```json
{
  "id": 1,
  "username": "usuario123",
  "mail": "usuario@email.com",
  "profile_image_path": null
}
```

### GET `/users/<user_id>` 🔒
Obtiene información de un usuario por ID.

**Response (200):**
```json
{
  "id": 1,
  "username": "usuario123",
  "mail": "usuario@email.com",
  "profile_image_path": null
}
```

### PUT `/users/<user_id>` 🔒
Actualiza información del usuario (solo el propio usuario).

**Request Body:**
```json
{
  "username": "nuevo_nombre",
  "mail": "nuevo@email.com",
  "profile_image_path": "path/a/imagen.jpg",
  "password": "nueva_contraseña",
  "pro": true
}
```

**Response (200):**
```json
{
  "message": "Usuario actualizado",
  "user": {
    "id": 1,
    "username": "nuevo_nombre",
    "mail": "nuevo@email.com",
    "profile_image_path": "path/a/imagen.jpg",
    "pro": true,
    "havens_count": 2
  }
}
```

---

## 🏠 Havens

### GET `/havens/can-create` 🔒
Verifica si el usuario puede crear más havens.

**Response (200):**
```json
{
  "can_create": true,
  "is_pro": false,
  "current_havens": 2,
  "max_havens": 3,
  "remaining_havens": 1
}
```

O si es usuario Pro:
```json
{
  "can_create": true,
  "is_pro": true,
  "current_havens": 10,
  "max_havens": "ilimitado",
  "remaining_havens": "ilimitado"
}
```

### POST `/havens` 🔒
Crea un nuevo haven.

**Restricciones:**
- Usuarios gratuitos: máximo 3 havens
- Usuarios Pro: havens ilimitados

**Request Body:**
```json
{
  "name": "Mi Casa",
  "latitude": 28.123456,
  "longitude": -15.654321,
  "radius": 100.0
}
```

**Response (201):**
```json
{
  "message": "Haven creado",
  "haven": {
    "haven_id": 1,
    "user_id": 1,
    "name": "Mi Casa",
    "latitude": 28.123456,
    "longitude": -15.654321,
    "radius": 100.0
  },
  "remaining_havens": 2
}
```

**Error (403) - Límite alcanzado:**
```json
{
  "error": "Has alcanzado el límite de havens gratuitos (3 máximo)",
  "message": "Actualiza a Pro para crear havens ilimitados",
  "current_havens": 3,
  "max_havens": 3,
  "is_pro": false
}
```

### GET `/havens` 🔒
Obtiene todos los havens del usuario autenticado.

**Response (200):**
```json
[
  {
    "haven_id": 1,
    "user_id": 1,
    "name": "Mi Casa",
    "latitude": 28.123456,
    "longitude": -15.654321,
    "radius": 100.0
  }
]
```

### GET `/havens/<haven_id>` 🔒
Obtiene un haven específico.

**Response (200):**
```json
{
  "haven_id": 1,
  "user_id": 1,
  "name": "Mi Casa",
  "latitude": 28.123456,
  "longitude": -15.654321,
  "radius": 100.0
}
```

### PUT `/havens/<haven_id>` 🔒
Actualiza un haven (solo el dueño).

**Request Body:**
```json
{
  "name": "Casa Actualizada",
  "latitude": 28.111111,
  "longitude": -15.222222,
  "radius": 150.0
}
```

### DELETE `/havens/<haven_id>` 🔒
Elimina un haven (solo el dueño).

**Response (200):**
```json
{
  "message": "Haven eliminado"
}
```

---

## 📝 Posts

### POST `/havens/<haven_id>/posts` 🔒
Crea un post en el feed de un haven.

**Request Body:**
```json
{
  "content": "Este es el contenido del post"
}
```

**Response (201):**
```json
{
  "message": "Post creado",
  "post": {
    "post_id": 1,
    "haven_id": 1,
    "content": "Este es el contenido del post",
    "date": "2025-01-15T10:30:00"
  }
}
```

### GET `/havens/<haven_id>/posts` 🔒
Obtiene todos los posts de un haven (ordenados por fecha descendente).

**Response (200):**
```json
[
  {
    "post_id": 1,
    "haven_id": 1,
    "content": "Este es el contenido del post",
    "date": "2025-01-15T10:30:00"
  }
]
```

---

## 💬 Chat

### POST `/havens/<haven_id>/messages` 🔒
Envía un mensaje al chat de un haven.

**Request Body:**
```json
{
  "content": "Hola a todos!"
}
```

**Response (201):**
```json
{
  "message": "Mensaje enviado",
  "chat_message": {
    "message_id": 1,
    "haven_id": 1,
    "user_id": 1,
    "content": "Hola a todos!",
    "date": "2025-01-15T10:30:00",
    "username": "usuario123"
  }
}
```

**Nota:** Este endpoint también emite una notificación WebSocket a todos los usuarios conectados a ese haven.

### GET `/havens/<haven_id>/messages` 🔒
Obtiene todos los mensajes de un haven (ordenados cronológicamente).

**Response (200):**
```json
[
  {
    "message_id": 1,
    "haven_id": 1,
    "user_id": 1,
    "content": "Hola a todos!",
    "date": "2025-01-15T10:30:00",
    "username": "usuario123"
  }
]
```

---

## 🔌 WebSocket

El servidor utiliza Socket.IO para notificaciones en tiempo real.

### Conexión
```javascript
const socket = io('http://tu-servidor:5050');
```

### Eventos

#### `connect`
Se dispara cuando el cliente se conecta al servidor.

#### `join_haven`
El cliente se une a una sala de haven para recibir notificaciones.

**Emit:**
```javascript
socket.emit('join_haven', { haven_id: 1 });
```

**Response:**
```javascript
socket.on('joined', (data) => {
  console.log('Unido al haven:', data.haven_id);
});
```

#### `new_message`
Se dispara cuando hay un nuevo mensaje en un haven al que estás suscrito.

**Listen:**
```javascript
socket.on('new_message', (message) => {
  console.log('Nuevo mensaje:', message);
  // Mostrar notificación
});
```

**Payload:**
```json
{
  "message_id": 1,
  "haven_id": 1,
  "user_id": 2,
  "content": "Mensaje nuevo",
  "date": "2025-01-15T10:30:00",
  "username": "otro_usuario"
}
```

---

## 🛡️ Códigos de Error

- **400**: Bad Request - Faltan campos requeridos
- **401**: Unauthorized - Credenciales inválidas o token expirado
- **403**: Forbidden - No tienes permisos para realizar esta acción
- **404**: Not Found - Recurso no encontrado
- **409**: Conflict - El recurso ya existe (username/email duplicado)

---

## 📱 Integración con Android

Ver archivo `android_example.kt` para un ejemplo completo de integración con Retrofit y Socket.IO.

### Pasos rápidos:
1. Registrar usuario o login
2. Guardar el token JWT en SharedPreferences
3. Conectar al WebSocket
4. Unirse a las salas de los havens del usuario
5. Escuchar eventos `new_message` para mostrar notificaciones

---

## 🚀 Despliegue con Docker

Asegúrate de cambiar `JWT_SECRET_KEY` en producción:

```python
app.config['JWT_SECRET_KEY'] = 'tu-clave-secreta-super-segura-cambiala'
```

Ejecuta con:
```bash
docker-compose up --build
```