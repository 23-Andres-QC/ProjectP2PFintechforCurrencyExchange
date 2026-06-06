"""WSGI entry point"""
import os
from app import create_app

config_name = os.getenv('APP_ENV', 'development')
app = create_app(config_name)

#url = 'http://localhost:5000'

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
