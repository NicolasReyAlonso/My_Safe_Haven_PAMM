# app.py
import os
from flask import Flask, jsonify, request, send_from_directory
from datetime import timedelta
import uuid
from werkzeug.utils import secure_filename

from flask_jwt_extended import (
    create_access_token, jwt_required, get_jwt_identity, JWTManager
)
from flask_migrate import Migrate
from flask_socketio import SocketIO, join_room, emit
import math
from extensions import db
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgres:Nicololo@db:5432/mysafehaven"
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = 'Nicolololololololololololo'
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = timedelta(days=30)
UPLOAD_FOLDER = 'uploads'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'}

def allowed_file(filename: str) -> bool:    
    return '.' in filename and filename.rsplit('.', 1)[1].lower

# Inicializa extensiones con ESTA app
db.init_app(app)
migrate = Migrate(app, db)
jwt = JWTManager(app)
socketio = SocketIO(app, cors_allowed_origins="*")

# Importa los modelos DESPUÉS de init_app
from models import User, Haven, HavenPost, ChatMessage, Subscription

# Si no usas 'flask db upgrade' aún, crea tablas (útil en desarrollo)
with app.app_context():
    db.create_all()

# Almacenar usuarios conectados: {user_id: [session_ids]}
active_connections = {}

# ====================== UTILIDADES ==============================
def haversine_distance(lat1, lon1, lat2, lon2):
    """Devuelve la distancia en metros entre dos coordenadas"""
    R = 6371000  # Radio de la Tierra en metros
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)

    a = math.sin(dphi/2)**2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))

    return R * c

# ==================== RUTAS DE AUTENTICACIÓN ====================

@app.route('/')
def home():
    return jsonify({"message": "Backend My Safe Haven funcionando!"})


@app.route('/register', methods=['POST'])
def register():
    # Detectamos si viene como multipart/form-data (con archivo) o JSON
    is_multipart = request.content_type and request.content_type.startswith('multipart/form-data')

    if is_multipart:
        form = request.form
        username = form.get('username')
        mail = form.get('mail')
        password = form.get('password')
        image = request.files.get('profile_image')  # <-- nombre del campo de archivo

        # Validaciones mínimas (igual que antes)
        if not username or not mail or not password:
            return jsonify({"error": "Faltan campos requeridos"}), 400

        # Chequeo de unicidad
        if User.query.filter_by(username=username).first():
            return jsonify({"error": "El username ya existe"}), 409
        if User.query.filter_by(mail=mail).first():
            return jsonify({"error": "El email ya está registrado"}), 409

        # Guardado de imagen (si viene)
        profile_image_path = None
        if image:
            if not image.filename:
                return jsonify({"error": "El archivo de imagen no tiene nombre"}), 400
            if not allowed_file(image.filename):
                return jsonify({"error": "Formato de imagen no permitido"}), 400

            # Nombre seguro + UUID para evitar colisiones
            filename = secure_filename(image.filename)
            ext = filename.rsplit('.', 1)[1].lower()
            unique_name = f"{uuid.uuid4().hex}.{ext}"
            save_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_name)
            image.save(save_path)

            # Guardamos ruta relativa (ej. 'uploads/uuid.jpg')
            profile_image_path = f"{app.config['UPLOAD_FOLDER']}/{unique_name}"

        # Creamos usuario
        user = User(
            username=username,
            mail=mail,
            profile_image_path=profile_image_path
        )
        user.set_password(password)
        db.session.add(user)
        db.session.commit()

        access_token = create_access_token(identity=str(user.id))
        return jsonify({
            "message": "Usuario registrado exitosamente",
            "access_token": access_token,
            "user": user.to_dict()
        }), 201

    # -------- JSON (comportamiento previo) --------
    data = request.get_json()
    if not data or not data.get('username') or not data.get('mail') or not data.get('password'):
        return jsonify({"error": "Faltan campos requeridos"}), 400

    if User.query.filter_by(username=data['username']).first():
        return jsonify({"error": "El username ya existe"}), 409
    if User.query.filter_by(mail=data['mail']).first():
        return jsonify({"error": "El email ya está registrado"}), 409

    user = User(
        username=data['username'],
        mail=data['mail'],
        profile_image_path=data.get('profile_image_path')  # mantiene compatibilidad
    )
    user.set_password(data['password'])
    db.session.add(user)
    db.session.commit()

    access_token = create_access_token(identity=str(user.id))
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
    
    # ✅ CAMBIO AQUÍ: Convertir a string
    access_token = create_access_token(identity=str(user.id))
    
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
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
    user = User.query.get(current_user_id)
    
    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404
    
    return jsonify(user.to_dict()), 200

@app.route('/users/<int:user_id>', methods=['PUT'])
@jwt_required()
def update_user(user_id):
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
    
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
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
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
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
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
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
    havens = Haven.query.filter_by(user_id=current_user_id).all()
    
    return jsonify([h.to_dict() for h in havens]), 200

@app.route('/havens/<int:haven_id>', methods=['PUT'])
@jwt_required()
def update_haven(haven_id):
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
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
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
    haven = Haven.query.get(haven_id)
    
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    if haven.user_id != current_user_id:
        return jsonify({"error": "No autorizado"}), 403
    
    db.session.delete(haven)
    db.session.commit()
    
    return jsonify({"message": "Haven eliminado"}), 200

@app.route('/havens/nearby', methods=['POST'])
@jwt_required()
def get_nearby_havens():
    data = request.get_json()

    if not data.get('latitude') or not data.get('longitude'):
        return jsonify({"error": "Se requiere latitude y longitude"}), 400

    user_lat = float(data['latitude'])
    user_lon = float(data['longitude'])

    # Obtener todos los havens
    havens = Haven.query.all()
    inside_havens = []

    for haven in havens:
        dist = haversine_distance(user_lat, user_lon, haven.latitude, haven.longitude)

        if dist <= haven.radius:
            inside_havens.append({
                **haven.to_dict(),
                "distance_meters": round(dist, 2)
            })

    return jsonify({
        "count": len(inside_havens),
        "havens": inside_havens
    }), 200


# ==================== POSTS ====================

@app.route('/havens/<int:haven_id>/posts', methods=['POST'])
@jwt_required()
def create_post(haven_id):
    current_user_id = int(get_jwt_identity())
    haven = Haven.query.get(haven_id)
    
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404
    
    if haven.user_id != current_user_id:
        return jsonify({"error": "No autorizado"}), 403
    
    # Detectar si viene como multipart (con imagen) o JSON
    is_multipart = request.content_type and request.content_type.startswith('multipart/form-data')
    
    if is_multipart:
        # POST CON IMAGEN
        form = request.form
        content = form.get('content')
        image = request.files.get('post_image')
        
        if not content:
            return jsonify({"error": "Se requiere contenido"}), 400
        
        # Guardar imagen si existe
        image_path = None
        if image:
            if not image.filename:
                return jsonify({"error": "El archivo de imagen no tiene nombre"}), 400
            if not allowed_file(image.filename):
                return jsonify({"error": "Formato de imagen no permitido"}), 400
            
            filename = secure_filename(image.filename)
            ext = filename.rsplit('.', 1)[1].lower()
            unique_name = f"{uuid.uuid4().hex}.{ext}"
            save_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_name)
            image.save(save_path)
            image_path = f"{app.config['UPLOAD_FOLDER']}/{unique_name}"
        
        post = HavenPost(
            haven_id=haven_id,
            content=content,
            image_path=image_path
        )
        
        db.session.add(post)
        db.session.commit()
        
        return jsonify({"message": "Post creado", "post": post.to_dict()}), 201
    
    else:
        # POST SIN IMAGEN (JSON)
        data = request.get_json(silent=True)
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

# ==================== subscripciones ====================
@app.route('/havens/<int:haven_id>/subscribe', methods=['POST'])
@jwt_required()
def subscribe_to_haven(haven_id):
    current_user_id = int(get_jwt_identity())

    haven = Haven.query.get(haven_id)
    if not haven:
        return jsonify({"error": "Haven no encontrado"}), 404

    # Verificar si ya existe la suscripción
    existing = Subscription.query.filter_by(user_id=current_user_id, haven_id=haven_id).first()
    if existing:
        return jsonify({"message": "Ya estás suscrito a este Haven"}), 200

    subscription = Subscription(
        user_id=current_user_id,
        haven_id=haven_id
    )

    db.session.add(subscription)
    db.session.commit()

    return jsonify({
        "message": "Suscripción realizada con éxito",
        "subscription": {
            "user_id": current_user_id,
            "haven_id": haven_id
        }
    }), 201

@app.route('/havens/<int:haven_id>/unsubscribe', methods=['DELETE'])
@jwt_required()
def unsubscribe_from_haven(haven_id):
    current_user_id = int(get_jwt_identity())

    subscription = Subscription.query.filter_by(
        user_id=current_user_id,
        haven_id=haven_id
    ).first()

    if not subscription:
        return jsonify({"error": "No estás suscrito a este Haven"}), 404

    db.session.delete(subscription)
    db.session.commit()

    return jsonify({"message": "Desuscripción exitosa"}), 200

@app.route('/havens/subscribed', methods=['GET'])
@jwt_required()
def get_subscribed_havens():
    current_user_id = int(get_jwt_identity())

    subscriptions = (
        db.session.query(Haven)
        .join(Subscription, Subscription.haven_id == Haven.haven_id)
        .filter(Subscription.user_id == current_user_id)
        .all()
    )

    return jsonify([
        {
            **haven.to_dict(),
            "is_subscribed": True
        }
        for haven in subscriptions
    ]), 200

# ==================== CHAT MESSAGES ====================

@app.route('/havens/<int:haven_id>/messages', methods=['POST'])
@jwt_required()
def create_message(haven_id):
    # ✅ CAMBIO AQUÍ: Convertir a int
    current_user_id = int(get_jwt_identity())
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

# ==================== SERVE FILES ====================

# Ruta para servir imágenes de perfil (no interfiere con nada más)
@app.route('/uploads/<path:filename>', methods=['GET'])
def serve_upload(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)


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

if __name__ == '__main__':
    socketio.run(app, debug=True, host="0.0.0.0", port=5050)