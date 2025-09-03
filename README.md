# Gas Payer Service

A Spring Boot service that processes signed blockchain transactions with automatic gas management. The service checks if a wallet has sufficient gas and adds it if necessary before forwarding the transaction to the blockchain.

## Features

- **Automatic Gas Management**: Checks wallet balance and adds gas if needed
- **Transaction Validation**: Validates gas limits and transaction format
- **Blockchain Integration**: Uses the blockchain-relay-utility library for transaction processing
- **Docker Support**: Containerized deployment with health checks
- **CI/CD Ready**: GitHub Actions workflow for automated builds and deployment

## API Endpoints

### POST /api/v1/signed-transaction

Processes a signed transaction with automatic gas management.

**Request Body:**
```json
{
  "userWalletAddress": "0x742b35Cc6834C0532Fee23f35E4cdb41c176fBc2",
  "signedTransactionHex": "0xf86c2a8504a817c80082520894742b35cc6834c0532fee23f35e4cdb41c176fbc2880de0b6b3a764000080820a95a0..."
}
```

**Response:**
```json
{
  "success": true,
  "transactionHash": "0x1234567890abcdef...",
  "contractAddress": "0x...",
  "error": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/signed-transaction \
  -H "Content-Type: application/json" \
  -d '{
    "userWalletAddress": "0x742b35Cc6834C0532Fee23f35E4cdb41c176fBc2",
    "signedTransactionHex": "0xf86c2a8504a817c80082520894742b35cc6834c0532fee23f35e4cdb41c176fbc2880de0b6b3a764000080820a95a0..."
  }'
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `BLOCKCHAIN_RPC_URL` | Blockchain RPC endpoint | `https://api.avax-test.network/ext/bc/C/rpc` |
| `BLOCKCHAIN_CHAIN_ID` | Chain ID | `43113` |
| `RELAYER_PRIVATE_KEY` | Private key for gas funding | Required |
| `GAS_PAYER_CONTRACT_ADDRESS` | Gas payer contract address | Required |
| `SECURITY_CONFIG_FILE` | Path to security config | `/home/gituser/chainservice/security-config.json` |
| `GAS_PRICE_MULTIPLIER` | Gas price buffer multiplier | `1.2` |
| `MAX_GAS_LIMIT` | Maximum allowed gas limit | `500000` |
| `MAX_GAS_COST_WEI` | Maximum gas cost in wei | `1000000000000000000` |
| `MAX_GAS_PRICE_MULTIPLIER` | Max gas price multiplier | `3.2` |

### Security Configuration

The service requires a security configuration file mounted at `/home/gituser/chainservice/security-config.json`. This directory should be mounted read-only in the Docker container.

## Deployment

### Docker Compose

```bash
# Set required environment variables
export BLOCKCHAIN_RPC_URL="your-rpc-url"
export RELAYER_PRIVATE_KEY="your-private-key"
export GAS_PAYER_CONTRACT_ADDRESS="your-contract-address"

# Start the service
docker-compose up -d
```

### Docker Run

```bash
docker run -d \
  --name gas-payer-service \
  -p 8080:8080 \
  -v /home/gituser/chainservice:/home/gituser/chainservice:ro \
  -e BLOCKCHAIN_RPC_URL="your-rpc-url" \
  -e RELAYER_PRIVATE_KEY="your-private-key" \
  -e GAS_PAYER_CONTRACT_ADDRESS="your-contract-address" \
  ghcr.io/your-username/gas-payer-service:latest
```

## Development

### Prerequisites

- JDK 17
- Docker (for containerized deployment)

### Building

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Running Locally

```bash
./gradlew bootRun
```

## API Documentation

Once running, visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## Health Checks

- Health endpoint: `http://localhost:8080/actuator/health`
- Other actuator endpoints: `http://localhost:8080/actuator/`

## Architecture

The service is built using:
- **Spring Boot 3.4.1** - Web framework
- **Kotlin 1.9.25** - Programming language
- **blockchain-relay-utility v5.0.0** - Core transaction processing
- **Web3j** - Blockchain interaction (via utility library)
- **Docker** - Containerization