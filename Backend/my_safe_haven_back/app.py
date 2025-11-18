from flask import Flask, jsonify, request
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgres:Nicololo@db:5432/mysafehaven"
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)
migrate = Migrate(app, db)
# Ruta de prueba
@app.route('/')
def home():
    return jsonify({"message": "Backend funcionando!"})

# Ruta para recibir datos de tu app
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    # Aquí iría tu lógica de autenticación
    return jsonify({"username": username, "status": "success"})

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5050)
