package db

import (
	"context"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"time"
)

// mission 记录了无人机一次飞行任务的时间以及目标点

var colMission *mongo.Collection

func init() {
	colMission = dbConn.Database("iot").Collection("mission")
}

type Mission struct {
	Time      time.Time  `json:"time" bson:"time"`
	Locations []Location `json:"locations" bson:"locations"`
}

func (m *Mission) Insert() error {
	_, err := colMission.InsertOne(context.TODO(), m)
	return err
}

func GetAllMissions() ([]Mission, error) {
	cur, err := colMission.Find(context.TODO(), bson.D{{}}, nil)
	if err != nil {
		return nil, err
	}
	var result []Mission
	if err = cur.All(context.TODO(), &result); err != nil {
		return nil, err
	}

	return result, nil
}
