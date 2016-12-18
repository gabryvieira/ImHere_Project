# NLPchatbot

Natural Language Processor based chatbot that connects to every chatroom on an OpenFire XMPP server.
It listens to every sent message on a MultiUser Chat and evaluates the message's body. It uses a naive Bayes classifier to categorize the received message. We consider the topic of the message the service requested to the bot.
In the case of a joke, both "tell me something funny" or "say a joke" will be evaluated as belonging to the "jokes" service.
The input message is also tokenized and the first noun encountered is saved. This is an imperfect way to deal with the urban dictionary service. Most of the time, the word to be defined is the only noun in the sentence. E.g: "tell me what software is", it will correctly extract "software" as the word to be searched. 

Running the xmppBot.py script will attempt to connect itself to the server used in the Services Engineering class and, if successful, will start listening on all chats created.Every 30 seconds the bot will survey the server looking for new chats to join.
