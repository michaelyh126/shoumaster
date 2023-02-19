// pages/list/list.js

// const locationUtils = require('../../utils/location')
Page({

  /**
   * 页面的初始数据
   */
  data: {
    checked:true,
    distance:'',
    state:'1',
    openid:'',
    no:'',
    Sname:'',
    phone:'',
    nickName:'',
    avatarUrl:'',
     MyLatitude:'',
     MyLongitude:'',
     TargetLatitude:30.947501,
     TargetLongitude:121.287832,
     location:'',
     distance:'',
  },

  loc(e){
this.setData({hello:'gg'})
console.log('1')
  },
  onChange(event) {
    this.setData({
      state: event.detail
    });
  },
  onChange1(e){
    this.setData({Sname:e.detail})
  },
  onChange2(e){
    this.setData({no:e.detail})
  },
  onChange3(e){
    this.setData({phone:e.detail})
  },
  onChange4(e){
    this.setData({community:e.detail})
  },

  inputHandler(e){
this.setData({msg: e.detail.value})
  },
  Rad(d) {
		return d * Math.PI / 180.0;
	},
	getDistance(lat1, lng1, lat2, lng2) {
    var that=this
  var lat1=that.data.MyLatitude
 var lng1=that.data.MyLongitude
 var lat2=that.data.TargetLatitude
 var lng2=that.data.TargetLongitude
		console.log(lat1, lng1, lat2, lng2)
		var radLat1 = this.Rad(lat1);
		var radLat2 = this.Rad(lat2);
		var a = radLat1 - radLat2;
		var b = this.Rad(lng1) - this.Rad(lng2);
		var s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * 6378.137;
		s = Math.round(s * 10000) / 10000;
		if (s < 1) {
			s = s.toFixed(3) * 1000  //保留小数
      console.log('经纬度计算的距离:' + s)
     this.setData({distance:s})
			return s
		} else {
			s = s.toFixed(3)*1000  //保留小数
      console.log('经纬度计算的距离:' + s)
      this.setData({distance:s})
			return s
    }
  },
redirect(){
  wx.redirectTo({
    url: '../success/success'
  })
},
onCheck(event) {
  this.setData({
    checked: event.detail
  });
},
 

commit(){

if(this.data.Sname==''){
  wx.showToast({
    title: "请输入姓名", // 提示的内容
    icon: "none", // 图标，默认success
    image: "", // 自定义图标的本地路径，image 的优先级高于 icon
    duration: 3000, // 提示的延迟时间，默认1500
    mask: false, // 是否显示透明蒙层，防止触摸穿透
  }
  );
  return
};
if(this.data.no==''){
  wx.showToast({
    title: "请输入学号", // 提示的内容
    icon: "none", // 图标，默认success
    image: "", // 自定义图标的本地路径，image 的优先级高于 icon
    duration: 3000, // 提示的延迟时间，默认1500
    mask: false, // 是否显示透明蒙层，防止触摸穿透
  }
  );
  return
};
if(this.data.phone==''){
  wx.showToast({
    title: "请输入手机号", // 提示的内容
    icon: "none", // 图标，默认success
    image: "", // 自定义图标的本地路径，image 的优先级高于 icon
    duration: 3000, // 提示的延迟时间，默认1500
    mask: false, // 是否显示透明蒙层，防止触摸穿透
  }
  );
  return
};

  wx.request({
    // url:'https://ael126.top/jeecg-boot/openid/InsertStudentCheck', 
    url:'http://localhost:8080/jeecg-boot/openid/InsertStudentCheck', 
    data: {
       name:this.data.Sname,
       no:this.data.no,
       phone:this.data.phone,
       openid:this.data.openid,
       state: this.data.state,
       MyLatitude:this.data.MyLatitude,
       MyLongitude:this.data.MyLongitude,
       distance:this.data.distance,
       community:this.data.community
     },
     method: 'get',
     success: function (res) {
        if(res.data.success==true){
          wx.redirectTo({
            url: '../success/success'
          })
        }
        else{
          wx.redirectTo({
            url: '../fail/fail'
          })
        }
     },
     fail:(res)=>{  
      wx.redirectTo({
        url: '../fail/fail'
      })
  }
})
},



  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
   

var app = getApp()
//  if(app.globalData.hasSigned==false)
// {
     var that=this
    
      wx.getLocation({
        type: "wgs84",
        success (res) {
            console.log('当前所在位置的经纬度为：')
            that.setData({MyLatitude: res.latitude ,MyLongitude:res.longitude})
            console.log(res.latitude,res.longitude)
            that.getDistance()
        }
    })


          // 获取openid或跳转条件
          wx.login({

          
            success (res) {
          console.log("将要获取openid")
              if (res.code) {
                //发起网络请求
                wx.request({
                  url:'http://localhost:8080/jeecg-boot/openid/testopenid', 
                  data: {
                     code:res.code,
                     MyLongitude:that.data.MyLongitude,
                     MyLatitude:that.data.MyLatitude,
                     distance:that.data.distance
                   },
                   method: 'get',
                   success: (res)=> {
                     
                      that.setData({openid:res.data})
                        console.log(res.data+"   获得openid");
                        if(res.data==true){
                          app.globalData.hasSigned=true
                          wx.redirectTo({
                            url: '../success/success'
                          })
                        }
                        
                   },
                   fail:(res)=>{  
                    wx.redirectTo({
                      url: '../fail/fail'
                    })
                }
              })
              } else {
                console.log('失败！' + res.errMsg)
              }
            
            }
          })
//  }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    wx.hideHomeButton();
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }
})