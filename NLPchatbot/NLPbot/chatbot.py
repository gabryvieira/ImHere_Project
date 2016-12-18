from sklearn.datasets import load_files
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.feature_extraction.text import TfidfTransformer
import nltk
from urbandic import urbandic
from reddit import reddit


class chatbot:




    def run_bot(self,message):
        categories = ['urban_dictionary', 'reddit_jokes', 'reddit_news', 'flight_prices']

        bot_train = load_files("/home/ubuntu/NLPchatbot/NLPbot/training_data", categories=categories, shuffle=True,
                               random_state=42)

        print(bot_train.target_names)

        count_vect = CountVectorizer()

        X_train_counts = count_vect.fit_transform(bot_train.data)

        X_train_counts.shape

        tf_transformer = TfidfTransformer(use_idf=False).fit(X_train_counts)
        X_train_tf = tf_transformer.transform(X_train_counts)
        X_train_tf.shape

        tfidf_transformer = TfidfTransformer()
        X_train_tfidf = tfidf_transformer.fit_transform(X_train_counts)
        X_train_tfidf.shape
        clf = MultinomialNB().fit(X_train_tfidf, bot_train.target)

        docs_new = [message]
        subjects = []
        for line in docs_new:
            tokens = nltk.word_tokenize(line)
            tagged = nltk.pos_tag(tokens)
            for tag in tagged:
                if (tag[1] == 'NN'):
                    print(tag[0])
                    subjects.append(tag[0])

        X_new_counts = count_vect.transform(docs_new)
        X_new_tfidf = tfidf_transformer.transform(X_new_counts)

        predicted = clf.predict(X_new_tfidf)

        api_search = zip(subjects, predicted)

        for doc, category in zip(docs_new, predicted):
            print('%r => %s' % (doc, bot_train.target_names[category]))
            # if(bot_train.target_names[category]=='urban_dictionary'):



        for search in api_search:
            if (bot_train.target_names[search[1]] == 'urban_dictionary'):
                print(urbandic.get_definition(search[0]))
                return urbandic.get_definition(search[0])
            if (bot_train.target_names[search[1]] == 'reddit_jokes'):
                print(reddit.get_joke())
                return reddit.get_joke()
            if (bot_train.target_names[search[1]] == 'reddit_news'):
                print(reddit.get_news())
                return reddit.get_news()
            if (bot_train.target_names[search[1]] == 'remindme'):
                print("remind me to ")
                return "remindme"


X = chatbot()
urbandic = urbandic()
reddit = reddit()

