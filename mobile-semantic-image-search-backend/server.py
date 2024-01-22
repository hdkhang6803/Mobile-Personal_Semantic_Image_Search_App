from flask import Flask, request, jsonify
import os
from routes.update_index_route import create_update_index_routes
from globals import CACHE_FOLDER_NAME, INDEX_FILE_NAME, USERID_FILE_NAME
from helper.faiss_helper import load_index
from routes.query_route import txt_query_search_route, img_query_search_route
import pandas as pd
import open_clip
device = "cpu"
model, _, preprocess = open_clip.create_model_and_transforms('ViT-B/16', pretrained='laion2b_s34b_b88k')

app = Flask(__name__)

# Set the path for the image cache folder
if not os.path.exists(CACHE_FOLDER_NAME):
    os.makedirs(CACHE_FOLDER_NAME)

# Init the index cache
index_cache = {}

# Load the list of user ID
userIDs_path = os.path.join('data', USERID_FILE_NAME)
if not os.path.exists(userIDs_path):
    os.makedirs(userIDs_path)
userIds = pd.read_csv(userIDs_path).to_numpy().flatten().tolist()



create_update_index_routes(app, model, index_cache, preprocess)
txt_query_search_route(app=app, model=model, index_cache=index_cache)
img_query_search_route(app=app, model=model, index_cache=index_cache, preprocess=preprocess)
    
# @app.route('/upload', methods=['POST'])
# def upload_image():
#     # Check if the post request has the file part
#     print(request.files)
#     if 'file' not in request.files:
#         return jsonify({'error': 'No file part'})

#     file = request.files['file']

#     # If the user submits an empty form
#     if file.filename == '':
#         return jsonify({'error': 'No selected file'})

#     # Save the file to the cache folder
#     # file_path = os.path.join(CACHE_FOLDER, file.filename)
#     file_path = os.path.join(CACHE_FOLDER_NAME, os.path.basename(file.filename))
#     file.save(file_path)

#     return jsonify({'message': 'File uploaded successfully', 'file_path': file_path})



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
