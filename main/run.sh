#!/bin/bash

echo "Starting Translation Project Management System..."
echo "=============================================="

# Compile the project
echo "Compiling project..."
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""

    # Run the application
    echo "Starting application..."
    mvn spring-boot:run
else
    echo "Compilation failed!"
    exit 1
fi