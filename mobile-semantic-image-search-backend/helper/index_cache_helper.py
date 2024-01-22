from globals import CACHE_FOLDER_NAME, INDEX_FILE_NAME, USERID_FILE_NAME
import os
import pandas as pd
import faiss

MAX_CACHE_SIZE = 5

# index_cache = {
#     userId: {
#         'index': index[userId],
#         'frequency': 0
#     }
# }

def load_index(userId, index_cache):
    if userId in index_cache.keys():
        index_cache[userId]['frequency'] += 1
        return index_cache[userId]['index']
    else:
        if len(index_cache.keys()) > MAX_CACHE_SIZE:
            # remove the least frequently use       
            min_freq = min(index_cache, key=index_cache.get)
            del index_cache[min_freq]
        index = faiss.read_index(os.path.join('data', userId, INDEX_FILE_NAME))
        index_cache[userId] = {
            'index': index,
            'frequency': 1
        }
        return index_cache[userId]['index']