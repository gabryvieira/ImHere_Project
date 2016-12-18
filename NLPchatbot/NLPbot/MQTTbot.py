import pika
import time
import sys

import json

credentials = ""
parameters = ""
connection = ""
channel = ""
chats = ""

def init_mqtt():

    from chatbot import chatbot
    chatbot = chatbot()
    credentials = pika.PlainCredentials('es', 'imhere')
    parameters = pika.ConnectionParameters('192.168.215.165',
                                           5012,
                                           '/',
                                           credentials)

    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    channel.queue_declare(queue='hello', durable=False)

    message = json.dumps({'op_id':0,'user_id':'chatbot','hash':'bot19','user_name':'bot19','device_token':''})
    #message = "Sou o bot"
    channel.basic_publish(exchange='',
                          routing_key='hello',
                          body=message,
                          properties=pika.BasicProperties(
                             delivery_mode = 2, # make message persistent
                          ))
    print(" [x] Sent %r" % message)
#    connection.close()

#    channel = connection.channel()

 #   channel.queue_declare(queue='bot19', durable=False,exclusive=True)
  #  print(' [*] Waiting for messages. To exit press CTRL+C')
  #  channel.basic_qos(prefetch_count=1)
  #  channel.basic_consume(callback,
   #                       queue='bot19')

   # channel.start_consuming()

    enter_allchats()

def callback(ch, method, properties, body):
    print(" [x] Received %r" % body)
    time.sleep(body.count(b'.'))
   # response_bot = chatbot.run_bot(body)
   # print(" [x] Response: "+str(response_bot))
    print(" [x] Done")
    ch.basic_ack(delivery_tag = method.delivery_tag)




def callback_enterchats(ch,method,properties,body):
    import json
    print(" [x] Received %r" % body)
    time.sleep(body.count(b'.'))
    print(" [x] Done")
    ch.basic_ack(delivery_tag = method.delivery_tag)
    tmp_chats = json.loads(body)

    print('chats data'+str(tmp_chats['data']))
    if(tmp_chats['data']):
    	global chats
        chats = tmp_chats['data']
    	ch.basic_cancel('ct')


def enter_allchats():
    print("enter all chats")
    credentials = pika.PlainCredentials('es', 'imhere')
    parameters = pika.ConnectionParameters('192.168.215.165',
                                           5012,
                                           '/',
                                           credentials)

    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    channel.queue_declare(queue='hello', durable=False)

    message = json.dumps({'op_id': 9, 'hash': 'bot19'})
    # message = "Sou o bot"
    channel.basic_publish(exchange='',
                          routing_key='hello',
                          body=message,
                          properties=pika.BasicProperties(
                              delivery_mode=2,  # make message persistent
                          ))
    print(" [x] Sent %r" % message)
    # connection.close()

    channel = connection.channel()

    channel.queue_declare(queue='bot19', durable=False)
    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(callback_enterchats,
                          queue='bot19',consumer_tag='ct')

    channel.start_consuming()

def get_chats():
    return chats



