package main

import "iot-backend/gps"

func main() {
	s := gps.NewServer()
	s.Start()
}
