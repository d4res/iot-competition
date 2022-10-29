// pages/Nav/nav.js
Page({

    /**
     * 页面的初始数据
     */
    data: {
        startPoint: {
            "latitude": 45.633802113298856,
            "longitude": 126.65042280296394, 
        }
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad: function (options) {
        let that = this;
        let markers = []
        let marker1 = {
            id: 1,
            latitude: this.data.startPoint.latitude, 
            longitude:  this.data.startPoint.longitude,
            iconPath: "/assets/loc.png",
        }
        markers.push(marker1)
        this.mapCtx = wx.createMapContext('map')
        console.log(markers)
        this.mapCtx.addMarkers({
            markers: markers,
            clear: false,
        });

        let socketTask = wx.connectSocket({
          url: 'wss://iot.dares.top/ws/weapp',
        })
        socketTask.onOpen((res)=>{
            console.log(res)
        })
        socketTask.onMessage((res)=>{
            let newLoc = JSON.parse(res.data)
            console.log(newLoc)
            this.mapCtx.translateMarker({
                markerId: 1,
                destination: {
                    latitude: newLoc.latitude,
                    longitude: newLoc.longitude
                },
                autoRotate: false,
                rotate: 0
            })
        })
        socketTask.onClose((res)=>{
            console.log(res)
        })
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady: function () {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow: function () {

    },

    /**
     * 生命周期函数--监听页面隐藏
     */
    onHide: function () {
    
    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload: function () {

    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh: function () {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom: function () {

    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage: function () {

    }
})