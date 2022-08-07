package gps

import (
	"encoding/json"
	"fmt"
	"github.com/suifengtec/gocoord"
	"io"
	"log"
	"net/http"
)

func transGPS(location Location) Location {
	p := gocoord.BD09ToWGS84(gocoord.Position{
		Lon: location.Longitude,
		Lat: location.Latitude,
	})
	location.Longitude = p.Lon
	location.Latitude = p.Lat
	return location
}

func (s *Server) GpsReceiver() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		body := r.Body
		defer body.Close()
		contents, err := io.ReadAll(body)
		if err != nil {

			log.Println(err)
		}
		var location Location
		err = json.Unmarshal(contents, &location)
		if err != nil {
			log.Println(err)
		}

		switch r.URL.Query().Get("type") {
		case "raw":
		default:
			location = transGPS(location)
		}
		log.Printf("receive gps %v", location)
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("success"))

		go func(loc Location) {
			s.TaskLine <- []Location{loc}
		}(location)
	}
}

func (s *Server) GpsList() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {

		body := r.Body
		defer body.Close()

		contents, err := io.ReadAll(body)
		if err != nil {
			log.Println(err)
		}
		var locations []Location
		err = json.Unmarshal(contents, &locations)
		if err != nil {
			log.Println(err)
		}

		for i := range locations {
			switch r.URL.Query().Get("type") {
			case "raw":
			default:
				locations[i] = transGPS(locations[i])
			}
		}
		fmt.Println("recv: ", locations)

		w.WriteHeader(http.StatusOK)
		w.Write([]byte("success"))

		go func(locs []Location) {
			s.TaskLine <- locs
		}(locations)
	}
}

func (s *Server) OnArrive() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		log.Println("aircraft arrive")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("success"))

		// using this to avoid blocking when sending response
		go func() {
			s.Arrive <- struct{}{}
		}()
	}
}
