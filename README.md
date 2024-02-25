# Personal Semantic Image Search App
## **I. About this project:**

In an industrialized and technologically transformed society, the amount of data available around each user is endless. Regarding your small mobile device, most of the memory will be occupied by photos, videos or other applications. To optimize the time spent retrieving moments captured by users' cameras, **SnapSeek** - a powerful tool for managing and utilizing personal photo collections was born to solve that big problem. With unique search capabilities based on linguistic descriptions, the app opens up a new approach to connecting users with important images and memories in their lives.

The features of the app include:
* **Exploring Images Using Natural Language:**
With convenience and ease of use, users simply need to describe an image with phrases or sentences. For example, you can type "a boy in white shirt holding a microphone" and the app will automatically search through your personal photo collection to provide the most accurate results.
* **Searching Images By Another Image:**
Users can take a picture or select from personal album to search for similar images.The returned results can also be selected for further search.
* **Effective Collection Management:**
With hundreds or even thousands of images in a personal collection, managing them can become a challenge for users. The app solves this problem by displaying a list of results based on the description and making it easy for users to conveniently view, browse or share images.
* **Flexible Options for Sharing:**
One of the most important features of the app is its flexible options for sharing photos. When search results appear, users can share wonderful moments with friends and family right from the app, saving time and effort. To meet users' strong sharing needs, the application integrates smoothly with popular social networking platforms. Users can conveniently and quickly share one or more photos directly from the app to Facebook, Instagram, Twitter and many other platforms.

## **II. Architecture:**
* The system's server uses the **Flask** framework and is placed on a DigitalOcean's **Droplet** virtual machine.
* Client's app is coded with **Java**, uses **Firebase** for user authentication, **Picasso** library for image display, and **Retrofit** for client-server communication.
* **OpenCLIP ViT-B/16 model** is used for calculating the embeddings of images.

## **III. How to run:**
* Firstly, clone the repository to your local workspace.
* Run `bash run.sh` to run automatically the project's server (The command can be run on Droplet's console). The server is already running so you do not have to run this code.
* Run the app on your mobile physical device.
