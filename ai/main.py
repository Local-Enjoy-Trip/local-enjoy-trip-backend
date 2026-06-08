import uvicorn
import os
import sys

# Add the current directory to python path to resolve 'app' correctly
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.main import app
from app.config import settings

if __name__ == "__main__":
    uvicorn.run("app.main:app", host=settings.AI_HOST, port=settings.AI_PORT, reload=True)
