import os
from dotenv import load_dotenv

# Load root .env
load_dotenv(dotenv_path=os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), ".env"))

class Settings:
    AI_PORT: int = int(os.getenv("AI_PORT", "8000"))
    AI_HOST: str = os.getenv("AI_HOST", "0.0.0.0")

settings = Settings()
