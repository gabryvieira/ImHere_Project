from flask import Flask
from flask import request
from flask import jsonify
from chatbot import chatbot

app = Flask(__name__)


@app.route('/')
def hello_world():
    return 'Hello World!'

@app.route('/runService')
def run_serv():
    msg = request.args.get('message')

    return jsonify(chatbot.run_bot(msg))



if __name__ == '__main__':
    chatbot = chatbot()
    app.run(host='192.168.215.163 ', port=5010)
