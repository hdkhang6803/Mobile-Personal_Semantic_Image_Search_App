import requests

from globals import serverIp

# Specify the server URL
server_url = f'http://{serverIp}:5000'

def upload_image(file_path):
    try:
        # Prepare the files parameter for the POST request
        with open(file_path, 'rb') as file:
            files = {'file': (file_path, file, 'image/jpeg')}
            
            # Send the POST request to the server
            response = requests.post(server_url + '/update_index', files=files)
            
            # Check if the request was successful (HTTP status code 2xx)
            response.raise_for_status()
            
            # Print the server response
            print(response.json())

    except requests.exceptions.RequestException as req_err:
        print(f'Request error occurred: {req_err}')

def send_txt_query(text_query):
    try:        
        server_url_for_txt_query = server_url + "/txt_query"
        # Send the GET request to the server
        response = requests.post(server_url_for_txt_query, data=text_query)
        
        # Check if the request was successful (HTTP status code 2xx)
        response.raise_for_status()
        
        # Print the server response
        print(response.json())

    except requests.exceptions.RequestException as req_err:
        print(f'Request error occurred: {req_err}')

def send_image_query(file_path):
    try:
        # Prepare the files parameter for the POST request
        with open(file_path, 'rb') as file:
            files = {'file': (file_path, file, 'image/jpeg')}
            
            # Send the POST request to the server
            server_url_query_img = server_url + "/img_query"
            response = requests.post(server_url_query_img, files=files)
            
            # Check if the request was successful (HTTP status code 2xx)
            response.raise_for_status()
            
            # Print the server response
            print(response.json())

    except requests.exceptions.RequestException as req_err:
        print(f'Request error occurred: {req_err}')

if __name__ == '__main__':
    # Replace 'path/to/your/image.jpg' with the actual path to your image file
    image_path = 'neon.jpg'
    upload_image(image_path)
    # text = "cake in table"
    # img_path = "D:/ONC Năm 2/230118 - Ảnh XTN/ONG CHIA MẬT - XTN2023/z5025209362808_cbc2b73516c2216ae0c582375cbebb4d.jpg"
    # send_txt_query(text_query=text)
    # send_image_query(img_path)
