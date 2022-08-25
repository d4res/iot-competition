// pages/log/log.js
Page({

    /**
     * 页面的初始数据
     */
    data: {
        missions: [], 
        activeNames: ['1']
    },
    onChange(event) {
    this.setData({
      activeNames: event.detail,
    });
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad: function (options) {
        wx.request({
          url: 'https://iot.dares.top/mission/log',
          success: (res)=>{
              if (res.statusCode != 200) {
                  console.log("HTTP GET " + res.statusCode)
                  return
              }
    
              let tmp = [];
              for ( let i = 0; i < res.data.length; i++) {
                  let time = new Date(Date.parse(res.data[i].time))
                  tmp[i] = {}
                  tmp[i]["time"] = time.getFullYear() + "/" + (time.getMonth() + 1) +  "/" + time.getDate() + " " + time.getHours() + ":" + time.getMinutes() 
                  tmp[i]["locations"] = JSON.stringify(res.data[i].locations)
              }
              console.log(tmp)
              console.log(res.statusCode)
              this.setData(
                  {
                      missions: tmp
                  }
              )
              this.data.missions = res.data
          }, 
          fail: (err)=>{
              console.log(err.errMsg)
          }
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