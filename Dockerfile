FROM golang
COPY . /app
WORKDIR /app
RUN go env -w GO111MODULE=on
RUN go env -w GOPROXY=https://goproxy.cn
RUN make tidy
RUN make build
CMD ./iot-backend