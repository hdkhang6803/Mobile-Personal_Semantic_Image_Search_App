from flask import Flask, request, jsonify
from helper.embedding_helper import calculate_txt_embeddings, calculate_img_embeddings
from PIL import Image
import os
from globals import CACHE_FOLDER_NAME, CSV_FILE_NAME
import faiss 
import pandas as pd
from io import BytesIO
import base64


def txt_query_search_route(app, model, index):
    @app.route('/txt_query', methods=['POST'])
    def txt_query_return_result():
        # print("Content-Type:", request.content_type)
        # print("Headers:", request.headers)
        # print("Data:", request.data)

        # text_query = request.data.decode('utf-8')
        # print("TEXT: ", text_query)
        text_query = request.get_data(as_text=True)
        print("TEXT: ", text_query)
        
        txt_embedding = calculate_txt_embeddings(model=model, text_query=text_query)
        image_uri_list = get_matched_image_paths(index, txt_embedding, num_results=100)

        return jsonify({
            'type': 'text_query_uri_list',
            'status': 'Text query uploaded successfully', 
            'text_query': text_query,
            'image_uris': image_uri_list
            })
    
def img_query_search_route(app, model, index, preprocess):
    @app.route('/img_query', methods=['POST'])
    def img_query_return_result():
        # print("GEt here", request.form['file'])
        try:
            # Get the base64-encoded image data from the request
            encoded_image = request.form['file']

            # Decode base64 to binary
            binary_data = base64.b64decode(encoded_image)

            # Create a BytesIO object
            image_stream = BytesIO(binary_data)

            # Open the image using PIL
            img_query = Image.open(image_stream)
        
            img_embedding = calculate_img_embeddings(model=model, preprocess=preprocess, raw_image=img_query, device='cpu')
            image_uri_list = get_matched_image_paths(index, img_embedding, num_results=100)
        
            return jsonify({
                'type': 'image_query_uri_list',
                'status': 'Image query uploaded successfully', 
                'image_uris': image_uri_list
                })
    
        except Exception as e:
            return jsonify({'error': str(e)})
    
def get_matched_image_paths(index, embedding, num_results):
    # Search for the top n images that are similar to the text_embedding
    similarities, indices = index.search(embedding.reshape(1, -1), num_results)    #2 represent top n results required
    indices_similarities = list(zip(indices[0], similarities[0]))
    indices_similarities.sort(key=lambda x: x[1], reverse=True)                            # Sort based on the distances

    # Get the image paths for the top n images from csv file
    return get_image_paths_from_csv(indices_similarities)  

def get_image_paths_from_csv(indices_similarities):
    # Read the csv file
    df = pd.read_csv(CSV_FILE_NAME, header=True, names=['image_path'])

    # Get the image paths for the top n images
    matched_image_paths = []
    for index, _ in indices_similarities:
        matched_image_paths.append(df.iloc[index]['image_path'])

    return matched_image_paths
