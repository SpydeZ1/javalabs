#!/bin/bash
set -e

echo "Starting Translation Project Management System..."
echo "Compiling project..."
mvn clean compile -q
echo "Compilation successful! Starting application..."
mvn spring-boot:run
