import requests
import json

def fetch_data(execution_params=None, url=""):

    # sample example how we can access java object at python runtime
    # execution_params - object as json string
    # parse it in python and use
    # here we take key token and can be used while making api call

    # json_data = json.loads(execution_params)
    # token = json_data.get('token', 'Token not found in the provided data')
    # print(token)

    response = requests.get(url)
    if response.status_code == 200:
        return process_data(response.json())
    else:
        return {"error": f"Failed to fetch data: {response.status_code}"}

print("Python module fetch_data_script.py loaded successfully")