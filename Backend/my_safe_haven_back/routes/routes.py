from flask import request, jsonify
from app import app, db
from models.models import User, Haven, FeedItem


# Crear usuario
@app.route("/users", methods=["POST"])
def create_user():
    data = request.json
    user = User(username=data["username"], email=data["email"])
    db.session.add(user)
    db.session.commit()
    return jsonify({"message": "User created", "id": user.id})


# Crear haven asociado a un usuario
@app.route("/users/<int:user_id>/havens", methods=["POST"])
def create_haven(user_id):
    data = request.json
    haven = Haven(name=data["name"], description=data.get("description"), user_id=user_id)
    db.session.add(haven)
    db.session.commit()
    return jsonify({"message": "Haven created", "id": haven.id})


# Agregar item al feed de un haven
@app.route("/havens/<int:haven_id>/feed", methods=["POST"])
def add_feed_item(haven_id):
    data = request.json  # puede ser cualquier objeto
    feed_item = FeedItem(haven_id=haven_id, payload=data)
    db.session.add(feed_item)
    db.session.commit()
    return jsonify({"message": "Feed item added", "id": feed_item.id})


# Obtener feed completo de un haven
@app.route("/havens/<int:haven_id>/feed", methods=["GET"])
def get_feed(haven_id):
    feed_items = FeedItem.query.filter_by(haven_id=haven_id).order_by(FeedItem.created_at.desc()).all()
    return jsonify([{
        "id": item.id,
        "payload": item.payload,
        "created_at": item.created_at.isoformat()
    } for item in feed_items])
