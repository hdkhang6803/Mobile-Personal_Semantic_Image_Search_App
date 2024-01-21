import os
from flask import Flask, request, jsonify
import helper.embedding_helper as embedding_helper
from globals import CACHE_FOLDER_NAME, CSV_FILE_NAME
import helper.faiss_helper as faiss_helper
from globals import CACHE_FOLDER_NAME, CSV_FILE_NAME
import os
import faiss
from PIL import Image
def add_to_csv(orig_image_path):
    """
    Adds the image path to the csv file.

    Args:
        orig_image_path (str): The original image path.

    Returns:
        None

    Raises:
        None

    """
    if not os.path.exists(CSV_FILE_NAME):
        with open(CSV_FILE_NAME, 'w') as f:
            f.write("image_path\n")

    with open(CSV_FILE_NAME, 'a') as f:
        f.write(orig_image_path + '\n')



def add_to_faiss_index(index : faiss.IndexFlatIP, embedding):
    if (index == None):
        # create
        index = faiss.IndexFlatIP(embedding.shape[1])
    index.add(embedding)
    faiss.write_index(index, "clip.index")



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



def create_update_index_routes(app, model, index, preprocess):
    
    @app.route('/update_index', methods=['POST'])
    def update_index():
        orig_image_paths, cache_image_paths = get_image_path_from_request(request)

        if len(orig_image_paths) == 0:
            return jsonify({'error': 'No selected file'})
        
        # embedding = embedding_helper.calculate_img_embeddings(cache_image_path)
        # Create PIL Image from file path

        for orig_image_path, cache_image_path in zip(orig_image_paths, cache_image_paths):
            img_query = Image.open(cache_image_path)
            img_embedding = embedding_helper.calculate_img_embeddings(model=model, preprocess=preprocess, raw_image=img_query, device='cpu')
            
            # Delete the file at cache_image_path
            os.remove(cache_image_path)

            add_to_csv(orig_image_path)
            faiss_helper.add_to_faiss_index(index, img_embedding)

            print(orig_image_path, " added to index and csv.")
        
        return jsonify({'message': 'File uploaded successfully and created embedding', 'file_path': cache_image_path})
    
