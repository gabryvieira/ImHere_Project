import requests
import json

class urbandic:


    #def __init__(self):
     #   self.get_definition('meme')

    def request (self,query):
        headers = {"X-Mashape-Key": "H1LAX6jsAImshOXNXqbV9tRQuLIzp1PDo3Ujsn6tQunYjvGiNx", "Accept": "text/plain"}
        r = requests.get('https://mashape-community-urban-dictionary.p.mashape.com/define?term='+query, headers=headers)

        return r

    def get_definition (self,query):
        r = self.request(query)

        j = json.loads(str(r.text))

        t = j['list']

        print (t[0]['definition'])
        return(str(t[0]))


x = urbandic()
#print(str(x.get_definition('deloitte')))





