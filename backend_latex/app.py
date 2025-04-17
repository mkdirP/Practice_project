import json
from flask import Flask, request, jsonify
from flask_cors import CORS
from tex_template import validate_tex

app = Flask(__name__)
CORS(app)


@app.route('/api/validate/latex', methods=['POST'])
def validate_latex():
    file = request.files.get('file')
    if not file or not file.filename.endswith('.tex'):
        return jsonify({"error": "Please upload a .tex file"}), 400

    content = file.read().decode('utf-8')

    response = validate_tex(content)
    return jsonify(json.loads(response.to_json()))


if __name__ == '__main__':
    app.run(debug=True, port=5000)
