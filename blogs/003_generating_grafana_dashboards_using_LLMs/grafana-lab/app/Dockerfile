# Build stage
FROM golang:1.22-alpine AS builder
RUN apk add --no-cache git
WORKDIR /src

# Copy source
COPY main.go .

# Create a module on the fly and fetch deps
RUN go mod init grafana-lab/app \
 && go get github.com/prometheus/client_golang@v1.19.1 \
 && go mod tidy \
 && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o /out/demo-app .

# Runtime stage
FROM alpine:3.20
RUN adduser -D -u 10001 appuser
COPY --from=builder /out/demo-app /usr/local/bin/demo-app
EXPOSE 8080
USER appuser
ENTRYPOINT ["/usr/local/bin/demo-app"]