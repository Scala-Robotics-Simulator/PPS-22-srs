#!/usr/bin/env bash

# Configuration
PROTO_FILE="rl.proto"
PROTO_DIR="../protobuf/src/main/protobuf/"
OUTPUT_DIR="./src/proto/"

# Derived names
PROTO_NAME="${PROTO_FILE%.proto}"
MESSAGE_FILE="${PROTO_NAME}_pb2.py"
SERVICE_FILE="${PROTO_NAME}_pb2_grpc.py"

echo "Generating Python gRPC code from ${PROTO_FILE}..."
echo "------------------------------------------------"

# Run the protoc command
python -m grpc_tools.protoc -I"${PROTO_DIR}" --python_out="${OUTPUT_DIR}" --grpc_python_out="${OUTPUT_DIR}" "${PROTO_FILE}"

# Check if the command succeeded
if [ $? -eq 0 ]; then
    echo "✓ Successfully generated gRPC code!"
    echo ""
    echo "Generated files:"
    if [ -f "${OUTPUT_DIR}/${MESSAGE_FILE}" ]; then
        echo "  ✓ ${MESSAGE_FILE} (message classes)"
    else
        echo "  ✗ ${MESSAGE_FILE} (NOT FOUND)"
    fi
    if [ -f "${OUTPUT_DIR}/${SERVICE_FILE}" ]; then
        echo "  ✓ ${SERVICE_FILE} (service stub)"
    else
        echo "  ✗ ${SERVICE_FILE} (NOT FOUND)"
    fi
    echo ""
    echo "You can now run the client with: python ${PROTO_NAME}_client.py"
else
    echo "✗ Error: Failed to generate gRPC code"
    echo ""
    echo "Possible issues:"
    echo "  - grpcio-tools not installed (run: pip install grpcio-tools)"
    echo "  - ${PROTO_FILE} file not found in ${PROTO_DIR}"
    echo "  - Syntax errors in ${PROTO_FILE}"
    exit 1
fi
