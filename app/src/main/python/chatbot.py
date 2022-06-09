import numpy as np
import nltk
import random
import pickle
from nltk.stem.lancaster import LancasterStemmer
stemmer = LancasterStemmer()



from os.path import dirname, join

nltk.download('punkt')

# filename = join(dirname(__file__), "intents.json")
pickleFile = join(dirname(__file__), "data.pickle")



with open(pickleFile, "rb") as f:
    words, labels, training, output, saved_data = pickle.load(f)






def BagOfWords(s):
    bag = [0 for _ in range(len(words))]

    swords = nltk.word_tokenize(s)
    swords = [stemmer.stem(word.lower()) for word in swords]

    for wrd in swords:
        for i, w in enumerate(words):
            if w == wrd:
                bag[i] = 1
    return np.array(bag).tolist()


def convStringToNumpy(StringList):
    b = [int(x) for x in StringList if x=="0" or x=="1"]
    return np.array(b)


def Convert(string):
    b = string[1:(len(string)-1)]
    li = list(b.split(", "))
    return li



def chatRoom(result):
    results = np.array(Convert(result), dtype=np.float32)
    rsIndex = np.argmax(results)
    tag = labels[rsIndex]
    if results[rsIndex] > 0.7:
        for tg in saved_data["intents"]:
            if tg['tag'] == tag:
                responses = tg['responses']
        return "" + str(random.choice(responses))
    else:
        return "I didn't get it ,ask again"