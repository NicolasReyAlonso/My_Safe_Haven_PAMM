from flask import Flask, jsonify, request

app = Flask(__name__)

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
    app.run(debug=True, port=5050)
