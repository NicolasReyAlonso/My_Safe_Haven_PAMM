
# app.py
from flask import Flask, jsonify, request
from datetime import timedelta

from flask_jwt_extended import (
    create_access_token, jwt_required, get_jwt_identity, JWTManager
)
from flask_migrate import Migrate
from flask_socketio import SocketIO, join_room, emit  # <-- IMPORTA join_room y emit

from extensions import db  # <-- USA la MISMA instancia de extensions.py

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgres:Nicololo@db:5432/mysafehaven"
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = 'tu-clave-secreta-super-segura-cambiala'
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(days=30)

# Inicializa extensiones con ESTA app
db.init_app(app)
migrate = Migrate(app, db)
jwt = JWTManager(app)
socketio = SocketIO(app, cors_allowed_origins="*")

# Importa los modelos DESPUÉS de init_app
from models import User, Haven, HavenPost, ChatMessage

# Si no usas 'flask db upgrade' aún, crea tablas (útil en desarrollo)
with app.app_context():
    db.create_all()

# Almacenar usuarios conectados: {user_id: [session_ids]}
active_connections = {}

# ==================== RUTAS DE AUTENTICACIÓN ====================

@app.route('/')
def home():
    return jsonify({"message": "Backend My Safe Haven funcionando!"})

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    
    if not data.get('username') or not data.get('mail') or not data.get('password'):
        return jsonify({"error": "Faltan campos requeridos"}), 400
    
    if User.query.filter_by(username=data['username']).first():
        return jsonify({"error": "El username ya existe"}), 409
    
    if User.query.filter_by(mail=data['mail']).first():
        return jsonify({"error": "El email ya está registrado"}), 409
    
    user = User(
        username=data['username'],
        mail=data['mail'],
        profile_image_path=data.get('profile_image_path')
    )
    user.set_password(data['password'])
    
    db.session.add(user)
    db.session.commit()
    
    access_token = create_access_token(identity=user.id)
    
    return jsonify({
        "message": "Usuario registrado exitosamente",
        "access_token": access_token,
        "user": user.to_dict()
    }), 201

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    
    if not data.get('username') and not data.get('mail'):
        return jsonify({"error": "Se requiere username o email"}), 400
    
    if not data.get('password'):
        return jsonify({"error": "Se requiere password"}), 400
    
    user = None
    if data.get('username'):
        user = User.query.filter_by(username=data['username']).first()
    else:
        user = User.query.filter_by(mail=data['mail']).first()
    
    if not user or not user.check_password(data['password']):
        return jsonify({"error": "Credenciales inválidas"}), 401
    
    access_token = create_access_token(identity=user.id)
    
    return jsonify({
        "message": "Login exitoso",
        "access_token": access_token,
        "user": user.to_dict()
    }), 200

# ==================== RUTAS DE USUARIOS ====================

@app.route('/users/<int:user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404
    
    return jsonify(user.to_dict()), 200

@app.route('/users/me', methods=['GET'])
@jwt_required()
def get_current_user():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404
    
    return jsonify(user.to_dict()), 200

@app.route('/users/<int:user_id>', methods=['PUT'])
@jwt_required()
def update_user(user_id):
    current_user_id = get_jwt_identity()
    
    if current_user_id != user_id:
        return jsonify({"error": "No autorizado"}), 403
    
    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404
    
    data = request.get_json()
    
    if data.get('username'):
        existing = User.query.filter_by(username=data['username']).first()
        if existing and existing.id != user_id:
            return jsonify({"error": "El username ya está en uso"}), 409
        user.username = data['username']
    
    if data.get('mail'):
        existing = User.query.filter_by(mail=data['mail']).first()
        if existing and existing.id != user_id:
            return jsonify({"error": "El email ya está en uso"}), 409
        user.mail = data['mail']
    
    if data.get('profile_image_path'):
        user.profile_image_path = data['profile_image_path']
    
    if data.get('password'):
        user.set_password(data['password'])
    
    if 'pro' in data:
        user.pro = data['pro']
    
    db.session.commit()
    
    return jsonify({"message": "Usuario actualizado", "user": user.to_dict()}), 200

# ==================== RUTAS DE HAVENS ====================

@app.route('/havens/can-create', methods=['GET'])
@jwt_required()
def can_create_haven():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    return jsonify({
        "can_create": user.can_create_haven(),
        "is_pro": user.pro,
        "current_havens": len(user.havens),
        "max_havens": "ilimitado" if user.pro else 3,
        "remaining_havens": "ilimitado" if user.pro else (3 - len(user.havens))
    }), 200

@app.route('/havens', methods=['POST'])
@jwt_required()
def create_haven():
    current_user_id = get_jwt_identity()
    data = request.get_json()
    
    if not all(k in data for k in ['name', 'latitude', 'longitude', 'radius']):
        return jsonify({"error": "Faltan campos requeridos"}), 400
    
    user = User.query.get(current_user_id)
    if not user.can_create_haven():
        return jsonify({
            "error": "Has alcanzado el límite de havens gratuitos",
            "max_havens": 3
        }), 403
    
    haven = Haven(
        user_id=current_user_id,
        name=data['name'],
        latitude=data['latitude'],
        longitude=data['longitude'],
        radius=data['radius']
    )
    
    db.session.add(haven)
    db.session.commit()
    
    return jsonify({
        "message": "Haven creado", 
        "haven": haven.to_dict(),
        "remaining_havens": 3 - len(user.havens) if not user.pro else "ilimitado"
    }), 201

@app.route('/havens/<int:haven_id>', methods=['GET'])
@jwt_required()
def get_haven(haven_id):
    haven = Haven.query.get(haven_id)
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    return jsonify(haven.to_dict()), 200

@app.route('/havens', methods=['GET'])
@jwt_required()
def get_all_havens():
    current_user_id = get_jwt_identity()
    havens = Haven.query.filter_by(user_id=current_user_id).all()
    
    return jsonify([h.to_dict() for h in havens]), 200

@app.route('/havens/<int:haven_id>', methods=['PUT'])
@jwt_required()
def update_haven(haven_id):
    current_user_id = get_jwt_identity()
    haven = Haven.query.get(haven_id)
    
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    if haven.user_id != current_user_id:
        return jsonify({"error": "No autorizado"}), 403
    
    data = request.get_json()
    
    if data.get('name'): haven.name = data['name']
    if data.get('latitude'): haven.latitude = data['latitude']
    if data.get('longitude'): haven.longitude = data['longitude']
    if data.get('radius'): haven.radius = data['radius']
    
    db.session.commit()
    
    return jsonify({"message": "Haven actualizado", "haven": haven.to_dict()}), 200

@app.route('/havens/<int:haven_id>', methods=['DELETE'])
@jwt_required()
def delete_haven(haven_id):
    current_user_id = get_jwt_identity()
    haven = Haven.query.get(haven_id)
    
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    if haven.user_id != current_user_id:
        return jsonify({"error": "No autorizado"}), 403
    
    db.session.delete(haven)
    db.session.commit()
    
    return jsonify({"message": "Haven eliminado"}), 200

# ==================== POSTS ====================

@app.route('/havens/<int:haven_id>/posts', methods=['POST'])
@jwt_required()
def create_post(haven_id):
    current_user_id = get_jwt_identity()
    haven = Haven.query.get(haven_id)
    
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    if haven.user_id != current_user_id:
        return jsonify({"error": "No autorizado"}), 403
    
    data = request.get_json()
    if not data.get('content'):
        return jsonify({"error": "Se requiere contenido"}), 400
    
    post = HavenPost(haven_id=haven_id, content=data['content'])
    
    db.session.add(post)
    db.session.commit()
    
    return jsonify({"message": "Post creado", "post": post.to_dict()}), 201

@app.route('/havens/<int:haven_id>/posts', methods=['GET'])
@jwt_required()
def get_posts(haven_id):
    haven = Haven.query.get(haven_id)
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    posts = HavenPost.query.filter_by(haven_id=haven_id).order_by(HavenPost.date.desc()).all()
    
    return jsonify([p.to_dict() for p in posts]), 200

# ==================== MENSAJES ====================

@app.route('/havens/<int:haven_id>/messages', methods=['POST'])
@jwt_required()
def create_message(haven_id):
    current_user_id = get_jwt_identity()
    haven = Haven.query.get(haven_id)
    
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    data = request.get_json()
    if not data.get('content'):
        return jsonify({"error": "Se requiere contenido"}), 400
    
    message = ChatMessage(
        haven_id=haven_id,
        user_id=current_user_id,
        content=data['content']
    )
    
    db.session.add(message)
    db.session.commit()
    
    socketio.emit('new_message', message.to_dict(), room=f'haven_{haven_id}')
    
    return jsonify({"message": "Mensaje enviado", "chat_message": message.to_dict()}), 201

@app.route('/havens/<int:haven_id>/messages', methods=['GET'])
@jwt_required()
def get_messages(haven_id):
    haven = Haven.query.get(haven_id)
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    messages = ChatMessage.query.filter_by(haven_id=haven_id).order_by(ChatMessage.date.asc()).all()
    
    return jsonify([m.to_dict() for m in messages]), 200

# ==================== WEBSOCKET EVENTS ====================

@socketio.on('connect')
def handle_connect():
    print('Cliente conectado')

@socketio.on('disconnect')
def handle_disconnect():
    print('Cliente desconectado')

@socketio.on('join_haven')
def handle_join_haven(data):
    haven_id = data.get('haven_id')
    if haven_id:
        join_room(f'haven_{haven_id}')
        emit('joined', {'haven_id': haven_id})
        print(f'Cliente unido a haven_{haven_id}')

# ... (el resto de tus rutas y handlers tal como los tienes)
# Al final:
if __name__ == '__main__':
    socketio.run(app, debug=True, host="0.0.0.0", port=5050)
