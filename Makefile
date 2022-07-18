.PHONY: build tidy

TARGET = iot-backend

build:
	@go build -o $(TARGET) -v main/main.go


tidy:
	@go mod tidy