import os
import faiss

def load_index(userId, index_file_name):
    """
    Loads the faiss index from the file.

    Args:
        index_file_name (str): The name of the file containing the faiss index.

    Returns:
        faiss index object.

    Raises:
        None

    """

    if (not os.path.exists(index_file_name)):
        return None

    index = faiss.read_index(index_file_name)
    return index

def add_to_faiss_index(userId, index : faiss.IndexFlatIP, embedding):
    if (index == None):
        # create
        index = faiss.IndexFlatIP(embedding.shape[1])
    index.add(embedding)
    faiss.write_index(index, os.path.join('data', userId, INDEX_FILE_NAME))