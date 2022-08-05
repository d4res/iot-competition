package gps

import (
	"encoding/json"
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

		location = transGPS(location)
		log.Printf("receive gps %v", location)
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("success"))

		go func(loc Location) {
			s.Task <- loc
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

		for i := range locations {
			locations[i] = transGPS(locations[i])
		}

		w.WriteHeader(http.StatusOK)
		w.Write([]byte("success"))

		go func(locs []Location) {
			s.TaskLine <- locs
		}(locations)
	}
}
