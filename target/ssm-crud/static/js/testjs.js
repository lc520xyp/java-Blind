
var video = document.getElementById('video');
var canvas = document.getElementById('canvas');
var context = canvas.getContext('2d');
var snap = document.getElementById('snap');


//使用后置摄像头：video : {facingMode: { exact: "environment" }}
if (navigator.mediaDevices.getUserMedia) {
    //最新的标准API
    navigator.mediaDevices.getUserMedia({
        video : {width: 1000, height: 1000}
    }).then(success).catch(error);
} else if (navigator.webkitGetUserMedia) {
    //webkit核心浏览器
    navigator.webkitGetUserMedia({video : {width: 1000, height: 1000}},success, error)
} else if (navigator.mozGetUserMedia) {
    //firefox浏览器
    navigator.mozGetUserMedia({video : {width: 1000, height: 1000}}, success, error);
} else if (navigator.getUserMedia) {
    //旧版API
    navigator.getUserMedia({video : {width: 1000, height: 1000}}, success, error);
}

function success(stream) {
    //兼容webkit核心浏览器
    //let CompatibleURL = window.URL || window.webkitURL;
    //console.log(stream.getTracks());
    //video.src = CompatibleURL.createObjectURL(stream);

    //将视频流设置为video元素的源
    video.srcObject = stream;
    video.play();
}

function error(error) {
    console.log(`访问用户媒体设备失败${error.name}, ${error.message}`);
}

// 截取图像
snap.addEventListener('click', function() {
    context.drawImage(video, 0, 0, 200, 150);
}, false);

function getMousePos(event) {
    var e = event || window.event;
    document.getElementById("xxx").value = e.pageX - 8;
    document.getElementById("yyy").value = e.pageY - 8;
    document.getElementById("width").value = e.target.clientWidth;
    document.getElementById("height").value = e.target.clientHeight;
}
