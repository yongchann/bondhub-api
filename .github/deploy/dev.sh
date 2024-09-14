#!/bin/bash

log_with_timestamp() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log_with_timestamp "Starting deployment script"

# Check and stop existing process
PID=$(lsof -t -i:8080)
if [ -n "$PID" ]; then
    log_with_timestamp "Stopping process on port 8080 (PID: $PID)"
    kill -9 $PID
    log_with_timestamp "Process stopped"
else
    log_with_timestamp "No process running on port 8080"
fi

# Apply environment variables
log_with_timestamp "Applying environment variables"
if [ -f env_file.sh ]; then
    chmod +x env_file.sh
    source ./env_file.sh
    log_with_timestamp "Environment variables applied successfully"
else
    log_with_timestamp "Error: env_file.sh not found"
    exit 1
fi

# Start new application
log_with_timestamp "Starting new application..."
nohup java -jar /home/ec2-user/otc-bridge-api-0.0.1-SNAPSHOT.jar > /home/ec2-user/app.log 2>&1 &
