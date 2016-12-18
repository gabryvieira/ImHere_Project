#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
    SleekXMPP: The Sleek XMPP Library
    Copyright (C) 2010  Nathanael C. Fritz
    This file is part of SleekXMPP.

    See the file LICENSE for copying permission.
"""

import sys
import logging
import getpass
import pika
from optparse import OptionParser

from threading import Timer


import sleekxmpp
import schedule

from chatbot import chatbot

#from queue import Queue, Empty
from twisted.internet import task
from twisted.internet import reactor

# Python versions before 3.0 do not use UTF-8 encoding
# by default. To ensure that Unicode is handled properly
# throughout SleekXMPP, we will set the default encoding
# ourselves to UTF-8.
if sys.version_info < (3, 0):
    reload(sys)
    sys.setdefaultencoding('utf8')
else:
    raw_input = input


class EchoBot(sleekxmpp.ClientXMPP):

    """
    A simple SleekXMPP bot that will echo messages it
    receives, along with a short thank you message.
    """


    def __init__(self, jid, password,room,nick):
        sleekxmpp.ClientXMPP.__init__(self, jid, password)
        self.room = room
        self.nick = nick
        # The session_start event will be triggered when
        # the bot establishes its connection with the server
        # and the XML streams are ready for use. We want to
        # listen for this event so that we we can initialize
        # our roster.
        self.add_event_handler("session_start", self.start)

        # The message event is triggered whenever a message
        # stanza is received. Be aware that that includes
        # MUC messages and error messages.
        self.add_event_handler("message", self.message)

        # The groupchat_message event is triggered whenever a message
        # stanza is received from any chat room. If you also also
        # register a handler for the 'message' event, MUC messages
        # will be processed by both handlers.
        self.add_event_handler("groupchat_message", self.muc_message)

        # The groupchat_presence event is triggered whenever a
        # presence stanza is received from any chat room, including
        # any presences you send yourself. To limit event handling
        # to a single room, use the events muc::room@server::presence,
        # muc::room@server::got_online, or muc::room@server::got_offline.
 #       self.add_event_handler("muc::%s::got_online" % self.room,
  #                             self.muc_online)

    def start(self, event):
        
        import MQTTbot

        MQTTbot.init_mqtt()
        chats = MQTTbot.get_chats()
        print("chats: "+str(chats))
        self.send_presence()
        self.get_roster()
#        self.plugin['xep_0045'].joinMUC("DETI@conference.ubuntu",
 #                                       "bot"
                                        # If a room password is needed, use:
                                        # password=the_room_password,
  #                                      )
        #self.plugin['xep_0045'].joinMUC("test2@conference.andrelopes",
         #                               "bot"
                                        # If a room password is needed, use:
                                        # password=the_room_password,
          #                              )
        for c in chats:
            self.plugin['xep_0045'].joinMUC(c+'@conference.ubuntu','bot')

        Timer(30,lambda: self.joinAllChats(),()).start()

    def joinAllChats(self):

        import MQTTbot
        MQTTbot.enter_allchats()
        MQTTbot.get_chats()
        chats = MQTTbot.get_chats()
        print("refresh chats: " + str(chats))
        for c in chats:
            self.plugin['xep_0045'].joinMUC(c + '@conference.ubuntu', 'bot')
        Timer(30,lambda: self.joinAllChats(),()).start()


    def muc_message(self,msg):

        import json
        import pprint
        import numpy as np

        if msg['mucnick'] != 'bot':
           print('receiving MUC msg'+str(msg['body']))

           result = chatbot.run_bot(msg['body'])
           if "remindme" in result:
               
                Timer(10, lambda: self.remindMe("me", "go shopping"), ()).start()
  #              msg.reply("Thanks for sending\n%(body)s" % msg).send()

           mat_msg = np.matrix(result)
           pretty_msg = np.matrix([[str(ele) for ele in a] for a in np.array(mat_msg)])
           print(pretty_msg)
           self.send_message(mto=msg['from'].bare,
                             mbody="reply, %s." % str(pretty_msg).strip(),
                             mtype='groupchat')

    def message(self, msg):
        
        print('msg')
    #if msg['type'] in ('chat', 'normal'):
         #   print(msg['body'])
          #  result = chatbot.run_bot(msg['body'])
           # if "remindme" in result:
                #schedule.every(10).seconds.do(self.remindMe("quim","dar banho ao cao"))
            #    Timer(10, lambda:self.remindMe("quim","dar banho ao cao"), ()).start()
             #   msg.reply("Thanks for sending\n%(body)s" % msg).send()
            #msg.reply("bot_reply: %(body)s" % msg).send()




    def remindMe(s,who,what):
            print ("Hey %s, just to remind you: %s" % (who,what))

    def callback(ch, method, properties, body):
            print(" [x] Received %r" % body)




if __name__ == '__main__':
#    xmpp_queue = Queue()
    chatbot = chatbot()





    # Setup the command line arguments.
    optp = OptionParser()

    # Output verbosity options.
    optp.add_option('-q', '--quiet', help='set logging to ERROR',
                    action='store_const', dest='loglevel',
                    const=logging.ERROR, default=logging.INFO)
    optp.add_option('-d', '--debug', help='set logging to DEBUG',
                    action='store_const', dest='loglevel',
                    const=logging.DEBUG, default=logging.INFO)
    optp.add_option('-v', '--verbose', help='set logging to COMM',
                    action='store_const', dest='loglevel',
                    const=5, default=logging.INFO)

    # JID and password options.
    optp.add_option("-j", "--jid", dest="jid",
                    help="JID to use")
    optp.add_option("-p", "--password", dest="password",
                    help="password to use")
    optp.add_option("-r", "--room", dest="room",
                    help="MUC room to join")
    optp.add_option("-n", "--nick", dest="nick",
                    help="MUC nickname")

    opts, args = optp.parse_args()

    # Setup logging.
    logging.basicConfig(level=opts.loglevel,
                        format='%(levelname)-8s %(message)s')

    if opts.jid is None:
        opts.jid = raw_input("Username: ")
    if opts.password is None:
        opts.password = raw_input("Password: ")
    if opts.room is None:
        opts.room = raw_input("MUC room: ")

    if opts.nick is None:
        opts.nick = "chatbot"

    
    xmpp = EchoBot(opts.jid, opts.password,opts.room, opts.nick)
    xmpp.register_plugin('xep_0030') # Service Discovery
    xmpp.register_plugin('xep_0004') # Data Forms
    xmpp.register_plugin('xep_0060') # PubSub
    xmpp.register_plugin('xep_0199') # XMPP Ping
    xmpp.register_plugin('xep_0045')  # Multi-User Chat

   
    import ssl
    xmpp.ssl_version = ssl.PROTOCOL_SSLv23

   
    if xmpp.connect(('192.168.215.165', 5222)):
        xmpp.process(block=True)
        schedule.run_pending()
        print("Done")

    else:
        print("Unable to connect.")

