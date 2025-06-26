#!/bin/bash
# Local Development Script for NAIS Auth API
# Works in Git Bash, WSL, Linux, and macOS

show_help() {
    echo ""
    echo "NAIS Auth API Local Development Script"
    echo "======================================"
    echo ""
    echo "Usage: ./dev.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build    - Build the SAM application"
    echo "  start    - Start the local API server"
    echo "  restart  - Rebuild and restart the server"
    echo "  clean    - Clean build artifacts"
    echo "  help     - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./dev.sh build     # Build the application"
    echo "  ./dev.sh start     # Start local server at http://127.0.0.1:8080"
    echo "  ./dev.sh restart   # Rebuild and restart"
    echo ""
}

build_app() {
    echo "Building SAM application for local development..."
    sam build --template-file template-local.yaml
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
    echo "Build completed successfully!"
}

start_server() {
    echo "Starting local API server on port 8080..."
    echo "API will be available at: http://127.0.0.1:8080"
    echo "Press Ctrl+C to stop the server"
    echo ""
    # Use built template and skip pulling Docker images to avoid network issues
    sam local start-api --skip-pull-image --env-vars env.json --port 8080
}

restart_server() {
    echo "Rebuilding and restarting local API server..."
    build_app
    if [ $? -eq 0 ]; then
        start_server
    fi
}

clean_build() {
    echo "Cleaning build artifacts..."
    if [ -d ".aws-sam" ]; then
        rm -rf .aws-sam
        echo "Cleaned .aws-sam directory"
    else
        echo "No build artifacts to clean"
    fi
}

# Main script logic
case "${1:-help}" in
    build)
        build_app
        ;;
    start)
        start_server
        ;;
    restart)
        restart_server
        ;;
    clean)
        clean_build
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "Unknown command: $1"
        show_help
        ;;
esac