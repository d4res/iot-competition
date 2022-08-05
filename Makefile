.PHONY: build tidy

TARGET = iot-backend

build: tidy
	go build -o $(TARGET) -v main/main.go


tidy:
	@go mod tidy

run: tidy
	@go run main/main.go