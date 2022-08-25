// pages/cropStatus/cropStatus.js

const config = require('../../utils/obs/config.js');
const converter = require('xml-js');
const { fromUint8Array } = require('js-base64');

Page({

  GetPos: (s) => {
    let loc = s.split('-')
    
    return {
      "latitude": loc[0].split('/')[1], 
      "longitude": loc[1].split('.jpg')[0]
    }
  },
  /**
   * 页面的初始数据
   */
  data: {
    imageNames: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {},

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    wx.request({
      url: config.EndPoint,
      success: (res) => {
        let listObj = converter.xml2js(res.data, {
          compact: true
        }).ListBucketResult.Contents;
        let arr = []
        listObj.forEach((item) => {
          if (item.Key._text.indexOf("health") == -1) {
            arr.push({
              "url": config.EndPoint + "/" + item.Key._text, 
              "pos": this.GetPos(item.Key._text)
            })
          }
        })
        this.setData({
          "imageNames": arr
        })
      },

    })
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