<template>
  <div style="padding: 20px;">
    <h3>录音上传</h3>

    <div style="font-size:14px">
      <h3>录音时长：{{ recorder && recorder.duration.toFixed(4) }}</h3>
      <br />
      <a-button type="primary" @click="handleStart">开始录音</a-button>
<!--      <a-button type="info" @click="handlePause">暂停录音</a-button>-->
<!--      <a-button type="success" @click="handleResume">继续录音</a-button>-->
<!--      <a-button type="warning" @click="handleStop">停止录音</a-button>-->
      <br />
      <br />
<!--      <h3>-->
<!--        播放时长：{{-->
<!--          recorder &&-->
<!--          (playTime > recorder.duration-->
<!--            ? recorder.duration.toFixed(4)-->
<!--            : playTime.toFixed(4))-->
<!--        }}-->
<!--      </h3>-->
<!--      <br />-->
<!--      <a-button type="primary" @click="handlePlay">播放录音</a-button>-->
<!--      <a-button type="info" @click="handlePausePlay">暂停播放</a-button>-->
<!--      <a-button type="success" @click="handleResumePlay">继续播放</a-button>-->
<!--      <a-button type="warning" @click="handleStopPlay">停止播放</a-button>-->
      <a-button type="error" @click="handleDestroy">销毁录音</a-button>
      <a-button type="primary" @click="uploadRecord">上传</a-button>
    </div>
  </div>
</template>

<script>
import Recorder from 'js-audio-recorder'
import { message } from 'ant-design-vue';
import { postAction } from '@/api/manage'
import Vue from "vue";
import { ACCESS_TOKEN, USER_NAME,USER_INFO,USER_AUTH,SYS_BUTTON_AUTH,UI_CACHE_DB_DICT_DATA,TENANT_ID,CACHE_INCLUDED_ROUTES } from "@/store/mutation-types"

export default {
  data() {
    return {
      recorder: null,
      playTime: 0,
      timer: null,
      src: null,
      currdatetime: '',
      model:{
        username: '',
        password: '',
        inputCode: ''
      }
    }
  },
  created() {
    this.recorder = new Recorder({
      sampleBits: 16, // 采样位数，支持 8 或 16，默认是16
      sampleRate: 16000, // 采样率，支持 11025、16000、22050、24000、44100、48000，根据浏览器默认值，我的chrome是48000
      numChannels: 1 // 声道，支持 1 或 2， 默认是1

      // compiling: false,(0.x版本中生效,1.x增加中)  // 是否边录边转换，默认是false
    })

  },
  methods: {
    // 开始录音
    handleStart() {
      this.recorder = new Recorder()
      Recorder.getPermission().then(() => {
        console.log('开始录音')
        this.recorder.start() // 开始录音
      }, (error) => {
        this.$message({
          message: '请先允许该网页使用麦克风',
          type: 'info'
        })
        console.log(`${error.name} : ${error.message}`)
      })
    },
    handlePause() {
      console.log('暂停录音')
      this.recorder.pause() // 暂停录音
    },
    handleResume() {
      console.log('恢复录音')
      this.recorder.resume() // 恢复录音
    },
    handleStop() {
      console.log('停止录音')
      this.recorder.stop() // 停止录音
    },
    handlePlay() {
      console.log('播放录音')
      console.log(this.recorder)
      this.recorder.play() // 播放录音

      // 播放时长
      this.timer = setInterval(() => {
        try {
          this.playTime = this.recorder.getPlayTime()
        } catch (error) {
          this.timer = null
        }
      }, 100)
    },
    handlePausePlay() {
      console.log('暂停播放')
      this.recorder.pausePlay() // 暂停播放

      // 播放时长
      this.playTime = this.recorder.getPlayTime()
      this.time = null
    },
    handleResumePlay() {
      console.log('恢复播放')
      this.recorder.resumePlay() // 恢复播放

      // 播放时长
      this.timer = setInterval(() => {
        try {
          this.playTime = this.recorder.getPlayTime()
        } catch (error) {
          this.timer = null
        }
      }, 100)
    },
    handleStopPlay() {
      console.log('停止播放')
      this.recorder.stopPlay() // 停止播放

      // 播放时长
      this.playTime = this.recorder.getPlayTime()
      this.timer = null
    },
    handleDestroy() {
      console.log('销毁实例')
      this.recorder.destroy() // 毁实例
      this.timer = null
    },
    uploadRecord() {
      if (this.recorder == null || this.recorder.duration === 0) {
        message.error('请先录音');
        return false
      }
      this.recorder.pause() // 暂停录音
      this.timer = null
      console.log('上传录音')// 上传录音

      const formData = new FormData()
      const blob = this.recorder.getWAVBlob()// 获取wav格式音频数据
      // 此处获取到blob对象后需要设置fileName满足当前项目上传需求，其它项目可直接传把blob作为file塞入formData
      const newbolb = new Blob([blob], { type: 'audio/wav' })
      const fileOfBlob = new File([newbolb], 'my' + '.wav')
      formData.append('file', fileOfBlob)
      const url = window.URL.createObjectURL(fileOfBlob)
      this.src = url
      postAction("/openid/VoiceprintLogin",formData)
        .then(res => {
          if(!res.success){
              message.error(res.message)
          }
          if(res.code =='200'){
            const result = res.result
            const userInfo = result.userInfo
            Vue.ls.set(ACCESS_TOKEN, result.token, 7 * 24 * 60 * 60 * 1000)
            Vue.ls.set(USER_NAME, userInfo.username, 7 * 24 * 60 * 60 * 1000)
            Vue.ls.set(USER_INFO, userInfo, 7 * 24 * 60 * 60 * 1000)
            Vue.ls.set(UI_CACHE_DB_DICT_DATA, result.sysAllDictItems, 7 * 24 * 60 * 60 * 1000)
            // commit('SET_TOKEN', result.token)
            // commit('SET_INFO', userInfo)
            // commit('SET_NAME', { username: userInfo.username,realname: userInfo.realname, welcome: welcome() })
            // commit('SET_AVATAR', userInfo.avatar)
            // resolve(res)
            this.loginSuccess()
          }


          // let loginParams = {
          //   username: this.model.username,
          //   password: this.model.password,
          //   captcha: this.model.inputCode,
          //   checkKey: this.currdatetime,
          //
          // }
          // this.VoiceprintLogin(loginParams).then((res) => {
          //   this.$emit('success', res.result)
          //   if(res.success==true){
          //     console.log('前')
          //     this.loginSuccess()
          //     console.log('后')
          //   }
          // })

        })


    },
    loginSuccess () {
      this.$router.push({ path: "/dashboard/analysis" }).catch(()=>{
        console.log('登录跳转首页出错,这个错误从哪里来的')
      })
      this.$notification.success({
        message: '欢迎',
        description: `${timeFix()}，欢迎回来`,
      });
    },
  }
}
</script>



<style scoped>
.getCaptcha{
  display: block;
  width: 100%;
  height: 40px;
}
</style>