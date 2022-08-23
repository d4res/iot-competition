package gps

import (
	"encoding/json"
	"fmt"
	"github.com/suifengtec/gocoord"
	"io"
	"iot-backend/db"
	"log"
	"net/http"
	"time"
)

func transGPS(location db.Location) db.Location {
	p := gocoord.WGS84ToGCJ02(gocoord.Position{
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
		var location db.Location
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

		go func(loc db.Location) {
			s.TaskLine <- []db.Location{loc}
		}(location)
	}
}

// GpsList 负责接收gps任务队列, 当收到GPS任务信息后,
// 会向无人机进行转发. 此外, 收到的gps列表以及时间都会被保存在数据库中.
func (s *Server) GpsList() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {

		body := r.Body
		defer body.Close()

		contents, err := io.ReadAll(body)
		if err != nil {
			log.Println(err)
		}
		var locations []db.Location
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
		m := &db.Mission{
			Time:      time.Now(),
			Locations: locations,
		}

		err = m.Insert()
		if err != nil {
			log.Println(err)
		}

		w.WriteHeader(http.StatusOK)
		w.Write([]byte("success"))

		go func(locs []db.Location) {
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
