<template>
  <div id="ChatPage">
    <div class="chatbox">
      <h2>📢 交流大厅</h2>
      <div class="message-box">
      <div class="bubble"
           v-for="(msg, index) in messages"
           :class="{ 'right-align': msg.username === username}"
           :key="index">
        <p>{{msg.time}}</p>{{msg.username}} : {{ msg.text }}</div>
<!--        <div class="my-bubble" v-for="(msg, index) in myMessages" :key="index">-->
<!--          <p>111</p>{{ msg }}</div>-->
      </div>
      <input class="my-input" v-model="messag" placeholder="输入消息..." @keyup.enter="sendMessage(messag)" />
      <button class="send" @click="closeWs">关闭ws连接</button>
    </div>
  </div>
</template>

<style scoped>
.right-align {
  margin-left: auto;
}
#ChatPage{
  padding: 1px;
}
.chatbox{
  background-image: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
  width: 100%;
  height: 100vh;
  z-index: 0;
  padding: 20px;
}
.message-box{
background-color: #ededed;
  z-index: 1;
  width: 70%;
  height: 100%;
  max-height: 400px;
  overflow-y: auto;
  scroll-behavior: smooth;
  display: flex;
  flex-direction: column;
  border-radius: 5px;
  padding: 5px;
}
.bubble{
  width: 25vw;
  min-height: 40px;
  z-index: 9;
  background-color: #ffffff;
  margin-top: 30px;
  border-radius: 10px;
  padding-left: 20px;
  line-height: 20px;
}
.my-input{
  width: 70%;
  min-height: 60px;
  z-index: 9;
  background-color: #e4e4e4;
  margin-top: 30px;
  border-radius: 10px;
  border: none;
}
.bubble p{
  font-size: 11px;
  height: 18px;
  margin-bottom: 0;
}

</style>

<script setup >
import { nextTick, ref } from "vue";

const messag = ref('');
const username = "路飞";
const messages = ref([]);
// const myMessages = ref([]);
let restart;
let ws = new WebSocket(`ws://localhost:8080/api/ws/${username}`);
let isHandle = false;

const openHandle =() => {
  console.log("ws 连接成功！！")
}

const messageHandle = (event) => {
  console.log("接收到消息！！");
  console.log(event);
  const data = JSON.parse(event.data);
  messages.value.push(data);
  nextTick(()=>{
    const elem = document.getElementsByClassName("message-box")[0];
    elem.scrollTop = elem.scrollHeight;
  })
}

const closeHandle = () => {
  console.log("ws 连接关闭！！")
  if(!isHandle){
    restart = setInterval(() =>{
      console.log("与服务器断开连接，正在尝试重新连接...");
      ws = new WebSocket(`ws://localhost:8080/api/ws/${username}`);
      if(ws.readyState === 0) {
        clearInterval(restart);
        console.log("连接成功，定时器已关闭");
        ws.addEventListener("message", messageHandle);
        ws.addEventListener("open", openHandle);
        ws.addEventListener("close", closeHandle);
        ws.addEventListener("error", errorHandle);
      }
    },2000)
  }

}

const errorHandle =() => {
  console.log("发生错误")
}

ws.addEventListener("message", messageHandle);
ws.addEventListener("open", openHandle);
ws.addEventListener("close", closeHandle);
ws.addEventListener("error", errorHandle);

const sendMessage =(message)=>{
  const jmessage = {
    username: username,
    text: message,
    time: new Date().getHours()+":" +new Date().getMinutes(),
  }
  ws.send(JSON.stringify(jmessage));
  messag.value = '';
}
const closeWs = () => {
  ws.close();
  isHandle = true;
}

</script>