package db

import (
	"fmt"
	"log"
	"testing"
	"time"
)

func TestDbConn(t *testing.T) {
	db := GetDB()
	fmt.Println(db)
}

func TestInsertMission(t *testing.T) {
	var missions = &Mission{
		Time: time.Now(),
		Locations: []Location{
			{
				Latitude:  45.342,
				Longitude: 126.1235,
			},
			{
				Latitude:  45.6224,
				Longitude: 126.4234,
			},
		},
	}

	err := missions.Insert()
	if err != nil {
		log.Println(err)
	}

}

func TestMission_Get(t *testing.T) {
	res, err := GetAllMissions()
	if err != nil {
		t.Fatal(err)
	}
	fmt.Printf("%#v\n", res[0].Time.In(time.Local))
	t.Log(res)
}

func TestTime(t *testing.T) {
	now := time.Now()

	fmt.Printf("%#v\n", now)
}

func TestCloseDB(t *testing.T) {
	CloseDB()
}
