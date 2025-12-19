from extensions import db
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

class User(db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    mail = db.Column(db.String(255), unique=True, nullable=False)
    username = db.Column(db.String(100), unique=True, nullable=False)
    profile_image_path = db.Column(db.String(500))
    password_hash = db.Column(db.String(255), nullable=False)
    pro = db.Column(db.Boolean, default=False, nullable=False)
    
    havens = db.relationship('Haven', backref='user', lazy=True, cascade='all, delete-orphan')
    messages = db.relationship('ChatMessage', backref='user', lazy=True, cascade='all, delete-orphan')
    
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
    
    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
    
    def can_create_haven(self):
        """Verifica si el usuario puede crear más havens"""
        if self.pro:
            return True
        return len(self.havens) < 3
    
    def is_subscribed_to(self, haven_id):
        return any(sub.haven_id == haven_id for sub in self.subscriptions)

    
    def to_dict(self):
        return {
            'id': self.id,
            'mail': self.mail,
            'username': self.username,
            'profile_image_path': self.profile_image_path,
            'pro': self.pro,
            'havens_count': len(self.havens)
        }

class Haven(db.Model):
    __tablename__ = 'havens'
    
    haven_id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    name = db.Column(db.String(200), nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    radius = db.Column(db.Float, nullable=False)
    
    posts = db.relationship('HavenPost', backref='haven', lazy=True, cascade='all, delete-orphan')
    messages = db.relationship('ChatMessage', backref='haven', lazy=True, cascade='all, delete-orphan')

    def subscribers_count(self):
        return len(self.subscriptions)

    
    def to_dict(self):
        return {
            'haven_id': self.haven_id,
            'user_id': self.user_id,
            'name': self.name,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'radius': self.radius
        }

class HavenPost(db.Model):
    __tablename__ = 'haven_posts'
    
    post_id = db.Column(db.Integer, primary_key=True)
    haven_id = db.Column(db.Integer, db.ForeignKey('havens.haven_id', ondelete='CASCADE'), nullable=False)
    content = db.Column(db.Text, nullable=False)
    image_path = db.Column(db.String(500))  # ✅ NUEVO CAMPO
    date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'post_id': self.post_id,
            'haven_id': self.haven_id,
            'content': self.content,
            'image_path': self.image_path,  # ✅ INCLUIR EN RESPUESTA
            'date': self.date.isoformat()
        }

class ChatMessage(db.Model):
    __tablename__ = 'chat_messages'
    
    message_id = db.Column(db.Integer, primary_key=True)
    haven_id = db.Column(db.Integer, db.ForeignKey('havens.haven_id', ondelete='CASCADE'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    content = db.Column(db.Text, nullable=False)
    date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'message_id': self.message_id,
            'haven_id': self.haven_id,
            'user_id': self.user_id,
            'content': self.content,
            'date': self.date.isoformat(),
            'username': self.user.username
        }
    
class Subscription(db.Model):
    __tablename__ = 'subscriptions'

    subscription_id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    haven_id = db.Column(db.Integer, db.ForeignKey('havens.haven_id', ondelete='CASCADE'), nullable=False)
    subscribed_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)

    # Relaciones
    user = db.relationship('User', backref=db.backref('subscriptions', lazy=True, cascade='all, delete-orphan'))
    haven = db.relationship('Haven', backref=db.backref('subscriptions', lazy=True, cascade='all, delete-orphan'))

    __table_args__ = (
        db.UniqueConstraint('user_id', 'haven_id', name='unique_user_haven_subscription'),
    )

    def to_dict(self):
        return {
            'subscription_id': self.subscription_id,
            'user_id': self.user_id,
            'haven_id': self.haven_id,
            'subscribed_at': self.subscribed_at.isoformat(),
            'username': self.user.username,
            'haven_name': self.haven.name
        }
