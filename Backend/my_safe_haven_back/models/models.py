from app import db
from sqlalchemy.dialects.postgresql import JSONB
from datetime import datetime

class User(db.Model):
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), nullable=False, unique=True)
    email = db.Column(db.String(120), nullable=False, unique=True)

    havens = db.relationship("Haven", backref="user", cascade="all, delete-orphan")

class Haven(db.Model):
    __tablename__ = "havens"

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(120), nullable=False)
    description = db.Column(db.Text)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete="CASCADE"))

    feed = db.relationship("FeedItem", backref="haven", cascade="all, delete-orphan")

class FeedItem(db.Model):
    __tablename__ = "feed_items"

    id = db.Column(db.Integer, primary_key=True)
    haven_id = db.Column(db.Integer, db.ForeignKey('havens.id', ondelete="CASCADE"))
    payload = db.Column(JSONB)  # puedes guardar cualquier estructura
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
