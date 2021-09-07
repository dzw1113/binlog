<template>
    <div></div>
</template>

<script>
    module.exports = {
        name: "WebSocket",
        props: {
            // 最大连接次数
            limitCnt: {
                type: Number,
                default: 3
            },
            isDelayConnect: {
                type: Boolean,
                default: false
            },
            uniqueId: {
                type: String,
                required: false
            }
        },
        data() {
            return {
                timer: '',
                delayTimer: '',
                connectCnt: 0,
                stompClient: null,
                connected: false
            };
        },
        mounted() {
            if ('WebSocket' in window) {
                this.initWebSocket();
            } else {
                console.log('当前浏览器 Not support websocket');
            }
        },
        created() {
        },
        methods: {
            initWebSocket() {
                this.connection();
            },
            connection() {
                let wsUrl = '/binlogWs';
                const socket = new SockJS(wsUrl);
                this.stompClient = webstomp.over(socket);
                const id = 'bl-zzcl';
                this.stompClient.hasDebug = false;
                this.stompClient.connect({'id': id}, (frame) => {
                    console.log('已连接...');
                    // 订阅消息
                    this.stompClient.subscribe('/topic/message', (rs) => {
                        // let httpResult = JSON.parse(rs.body);
                        // if (httpResult.status == '1') {
                        //     this.$emit('onMessage', httpResult.rs);
                        // }
                        this.$emit('onmessage', rs);
                    });
                    this.connected = true;
                    this.connectCnt = 0;
                }, (error) => {
                    console.log('连接失败');
                    console.log(error);
                    this.connected = false;
                    this.connectCnt += 1;
                    this.reconnect();
                });
            },
            reconnect() {
                if (this.connectCnt < this.limitCnt) {
                    clearTimeout(this.timer);
                    this.timer = setTimeout(() => {
                        // 没有连上
                        if (!this.connected) {
                            this.connection();
                        }
                    }, 60 * 1000);
                } else {
                    clearTimeout(this.timer);
                    clearTimeout(this.delayTimer);
                    // 连接失败 60分钟连接一次
                    if (this.isDelayConnect) {
                        this.delayTimer = setTimeout(() => {
                            // 没有连上
                            if (!this.connected) {
                                this.connection();
                            }
                        }, 60 * 60 * 1000);
                    }
                }
            },
            sendMsg(msg) {
                this.stompClient.send("/app/getCmd", {}, msg);
            },
            // 断开连接
            disconnect() {
                if (this.stompClient) {
                    this.stompClient.disconnect();
                }
            }
        },
        beforeDestroy: function () {
            // 页面离开时断开连接,清除定时器
            this.disconnect();
            clearTimeout(this.timer);
            clearTimeout(this.delayTimer);
            this.connectCnt = 0;
        }
    }
</script>

<style scoped>

</style>