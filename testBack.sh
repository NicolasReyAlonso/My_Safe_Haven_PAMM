curl http://localhost:5050/

#Crear Usuario

curl -X POST http://localhost:5050/register \
-H "Content-Type: application/json" \
-d '{
  "username": "usuario123",
  "mail": "usuario@email.com",
  "password": "password123",
  "profile_image_path": "opcional/path/imagen.jpg"
}'
