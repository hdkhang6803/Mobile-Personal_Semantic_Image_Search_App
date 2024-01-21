import os
from PIL import Image
import PIL
# import matplotlib.pyplot as plt
from tqdm import tqdm
# import numpy as np
# import pandas as pd
# import pickle
# import glob

import torch
import open_clip


def calculate_txt_embeddings(model, text_query):
    print(text_query)
    text_query_tokens = open_clip.tokenize(text_query)

    text_embedding = None
    with torch.no_grad(), torch.cuda.amp.autocast():
        text_embedding = model.encode_text(text_query_tokens)
    return text_embedding

def calculate_img_embeddings(model, preprocess, raw_image, device):
    image = preprocess(raw_image).unsqueeze(0).to(device)
    with torch.no_grad(), torch.cuda.amp.autocast():
        image_features = model.encode_image(image)
    return image_features
