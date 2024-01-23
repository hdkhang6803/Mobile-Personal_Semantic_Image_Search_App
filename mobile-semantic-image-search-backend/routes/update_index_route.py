import os
from flask import Flask, request, jsonify
import helper.embedding_helper as embedding_helper
from globals import CACHE_FOLDER_NAME
from helper.faiss_helper import add_to_faiss_index
from helper.csv_helper import add_to_csv, load_csv_paths
import os
import faiss
from PIL import Image
from helper.index_cache_helper import load_index



def get_image_path_from_request(request):
    files = request.files.getlist('files[]')

    orig_image_paths = []
    cache_image_paths = []

    for file in files:
        print(file.filename)

        if file.filename == '':
            continue

        cache_file_path = os.path.join(CACHE_FOLDER_NAME, os.path.basename(file.filename))
        file.save(cache_file_path)

        orig_image_paths.append(file.filename)
        cache_image_paths.append(cache_file_path)

    return orig_image_paths, cache_image_paths

        

    # If the user submits an empty form
    # if file.filename == '':
    #     return None, None

    # Save the file to the cache folder
    # file_path = os.path.join(CACHE_FOLDER, file.filename)


    return file.filename, cache_file_path



def create_update_index_routes(app, model, index_cache, csv_path_cache, userIds, preprocess):
    
    @app.route('/update_index', methods=['POST'])
    def update_index():
        orig_image_paths, cache_image_paths = get_image_path_from_request(request)
        userIds_temp = request.files.getlist('userId')
        userId = userIds_temp[0].filename

        print("userId: ", userId)

        if len(orig_image_paths) == 0:
            return jsonify({'error': 'No selected file'})
        
        # embedding = embedding_helper.calculate_img_embeddings(cache_image_path)
        # Create PIL Image from file path

        csv_params = []
        faiss_index_params = []
        for orig_image_path, cache_image_path in zip(orig_image_paths, cache_image_paths):
            img_query = Image.open(cache_image_path)
            img_embedding = embedding_helper.calculate_img_embeddings(model=model, preprocess=preprocess, raw_image=img_query, device='cpu')
            
            # Delete the file at cache_image_path
            os.remove(cache_image_path)

            index = load_index(userId, userIds, index_cache)
            csv_image_paths = load_csv_paths(userId, userIds, csv_path_cache);
            if (orig_image_path not in csv_image_paths):        
                add_to_csv(userId, userIds, orig_image_path)
                csv_image_paths.add(orig_image_path)
                # csv_params.append((userId, userIds, orig_image_path))
                add_to_faiss_index(userId, userIds, index, img_embedding)
                # faiss_index_params.append((userId, userIds, index, img_embedding))

            print(orig_image_path, " added to index and csv.")
        
        # for csv_param in csv_params:
        #     add_to_csv(*csv_param)
        # for faiss_index_param in faiss_index_params:
        #     add_to_faiss_index(*faiss_index_param)
            
        return jsonify({'message': 'File uploaded successfully and created embedding', 'file_paths': orig_image_paths})
    
