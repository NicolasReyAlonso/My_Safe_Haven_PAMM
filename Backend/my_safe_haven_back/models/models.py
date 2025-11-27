from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()

class User(db.Model):
    __tablename__ = "users"
    
    id = db.Column(db.Integer, primary_key=True)
    mail = db.Column(db.String(255), unique=True, nullable=False)
    username = db.Column(db.String(100), unique=True, nullable=False)
    profile_image_path = db.Column(db.String(500))
    password_hash = db.Column(db.String(255), nullable=False)

    havens = db.relationship("Haven", backref="creator", cascade="all, delete-orphan")
    messages = db.relationship("ChatMessage", backref="author", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<User {self.username}>"


class Haven(db.Model):
    __tablename__ = "havens"
    
    haven_id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    name = db.Column(db.String(200), nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    radius = db.Column(db.Float, nullable=False)

    posts = db.relationship("HavenPost", backref="haven", cascade="all, delete-orphan")
    messages = db.relationship("ChatMessage", backref="haven_chat", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Haven {self.name}>"


class HavenPost(db.Model):
    __tablename__ = "haven_posts"
    
    post_id = db.Column(db.Integer, primary_key=True)
    haven_id = db.Column(db.Integer, db.ForeignKey("havens.haven_id", ondelete="CASCADE"), nullable=False)
    content = db.Column(db.Text, nullable=False)
    date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)

    def __repr__(self):
        return f"<HavenPost {self.post_id}>"


class ChatMessage(db.Model):
    __tablename__ = "chat_messages"
    
    message_id = db.Column(db.Integer, primary_key=True)
    haven_id = db.Column(db.Integer, db.ForeignKey("havens.haven_id", ondelete="CASCADE"), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    content = db.Column(db.Text, nullable=False)
    date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)

    def __repr__(self):
        return f"<ChatMessage {self.message_id}>"
