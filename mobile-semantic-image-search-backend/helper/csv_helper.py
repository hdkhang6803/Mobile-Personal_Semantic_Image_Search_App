from globals import CSV_FILE_NAME
import os
from globals import CACHE_FOLDER_NAME, INDEX_FILE_NAME, USERID_FILE_NAME
import pandas as pd

MAX_CACHE_SIZE = 5

def load_csv_paths(userId, userIds, csv_path_cache):
    # add new userId to userIds.csv if not exist
    if userId not in userIds:
        df = pd.read_csv(os.path.join('data', USERID_FILE_NAME))
        df.loc[len(df)] = {'userId': userId}
        df.to_csv(os.path.join('data', USERID_FILE_NAME), index=False)
        userIds.append(userId)

    if userId in csv_path_cache.keys():
        csv_path_cache[userId]['frequency'] += 1
        return csv_path_cache[userId]['paths']
    else:
        if len(csv_path_cache.keys()) > MAX_CACHE_SIZE:
            # remove the least frequently use       
            min_freq = min(csv_path_cache, key=csv_path_cache.get)
            del csv_path_cache[min_freq]
                
        csv_file_path = os.path.join('data', userId, CSV_FILE_NAME)
        if (not os.path.exists(csv_file_path)):
            return set()

        
        paths = set(pd.read_csv(csv_file_path).to_numpy().flatten().tolist())
        csv_path_cache[userId] = {
            'paths': paths,
            'frequency': 1
        }
        return csv_path_cache[userId]['paths']
        

def add_to_csv(userId, userIds, orig_image_path):
    """
    Adds the image path to the csv file.

    Args:
        orig_image_path (str): The original image path.

    Returns:
        None

    Raises:
        None

    """

    userIdPath = os.path.join('data', userId)
    if (not os.path.exists(userIdPath)):
        os.makedirs(userIdPath)
        
    csv_file_path = os.path.join('data', userId, CSV_FILE_NAME)
    if not os.path.exists(csv_file_path):
        with open(csv_file_path, 'w') as f:
            f.write("image_path\n")

    with open(csv_file_path, 'a') as f:
        f.write(orig_image_path + '\n')