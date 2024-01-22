from globals import CSV_FILE_NAME
import os

def add_to_csv(userId, orig_image_path):
    """
    Adds the image path to the csv file.

    Args:
        orig_image_path (str): The original image path.

    Returns:
        None

    Raises:
        None

    """
    csv_file_path = os.path.join('data', userId, CSV_FILE_NAME)
    if not os.path.exists(csv_file_path):
        with open(csv_file_path, 'w') as f:
            f.write("image_path\n")

    with open(csv_file_path, 'a') as f:
        f.write(orig_image_path + '\n')