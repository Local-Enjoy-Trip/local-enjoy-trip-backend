FROM python:3.11-slim

WORKDIR /workspace

# Install compilation build tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy only requirements to leverage Docker cache
COPY ./ai/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the entire directory structure (needs project root context)
COPY . .

# Move to python workspace
WORKDIR /workspace/ai

# Expose HTTP port 8000
EXPOSE 8000

# Start the unified server using the root python entrypoint
CMD ["python", "main.py"]
