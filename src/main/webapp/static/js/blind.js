
var video = document.getElementById('video');
var canvas = document.getElementById('canvas');
var context = canvas.getContext('2d');
var snap = document.getElementById('snap');
var click_x =0;
var click_y =0;
var curMode = 1;

var map = new Map([
    ['F', 'no'],
    ['T', 'yes']
]);

let COCO_CLASSES = [
        "person",
        "bicycle",
        "car",
        "motorcycle",
        "airplane",
        "bus",
        "train",
        "truck",
        "boat",
        "traffic light",

        "fire hydrant",
        "stop sign",
        "parking meter",
        "bench",
        "bird",
        "cat",
        "dog",
        "horse",
        "sheep",
        "cow",

        "elephant",
        "bear",
        "zebra",
        "giraffe",
        "backpack",
        "umbrella",
        "handbag",
        "tie",
        "suitcase",
        "frisbee",

        "skis",
        "snowboard",
        "sports ball",
        "kite",
        "baseball bat",
        "baseball glove",
        "skateboard",
        "surfboard",
        "tennis racket",
        "bottle",

        "wine glass",
        "cup",
        "fork",
        "knife",
        "spoon",
        "bowl",
        "banana",
        "apple",
        "sandwich",
        "orange",

        "broccoli",
        "carrot",
        "hot dog",
        "pizza",
        "donut",
        "cake",
        "chair",
        "couch",
        "potted plant",
        "bed",

        "dining table",
        "toilet",
        "tv",
        "laptop",
        "mouse",
        "remote",
        "keyboard",
        "cell phone",
        "microwave",
        "oven",

        "toaster",
        "sink",
        "refrigerator",
        "book",
        "clock",
        "vase",
        "scissors",
        "teddy bear",
        "hair drier",
        "toothbrush"
]

setInterval(cutScreen, 333);


//使用后置摄像头：video : {facingMode: { exact: "environment" }}
if (navigator.mediaDevices.getUserMedia) {
    //最新的标准API
    navigator.mediaDevices.getUserMedia({
        video : {width: 640, height: 640}
    }).then(success).catch(error);
} else if (navigator.webkitGetUserMedia) {
    //webkit核心浏览器
    navigator.webkitGetUserMedia({video : {width: 640, height: 640}},success, error)
} else if (navigator.mozGetUserMedia) {
    //firefox浏览器
    navigator.mozGetUserMedia({video : {width: 640, height: 640}}, success, error);
} else if (navigator.getUserMedia) {
    //旧版API
    navigator.getUserMedia({video : {width: 640, height: 640}}, success, error);
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
    context.drawImage(video, 0, 0, 640, 640);
    SendPic();
}, false);

function cutScreen() {
    context.drawImage(video, 0, 0, 640, 640);
    SendPic();
}


function getMousePos(event) {
    var e = event || window.event;
    click_x = ((e.pageX - 8) / (e.target.clientWidth)) * 640;
    click_y = ((e.pageY - 8) / (e.target.clientHeight)) * 640;

    document.getElementById("xxx").value = e.pageX - 8;
    document.getElementById("yyy").value = e.pageY - 8;
    document.getElementById("width").value = e.target.clientWidth;
    document.getElementById("height").value = e.target.clientHeight;

    if(curMode == 1){ // 阅读模式
        readingMode();
    }else if(curMode == 2){ // 预警模式
        earlyWarningMode();
    }

}

//发送图片
function SendPic(){
    // var grade = document.getElementById("level").value;
    // var notebook = document.getElementById("notebook").value;
    var dataURL = canvas.toDataURL('image/png')


    $.ajax({

        url : 'send/picture',
        type : 'post',
        // data : formData,

        data : {base64Data:dataURL},
        dataType : 'json',
        cache : false,


        success : function(data) {
            // this.map = data.result;
            // map.forEach(function(value,key){
            //     console.log(value,key);
            // });
            // console.log(map);
            // console.log(data.result);

            map.clear();


            for (let [key, value] of Object.entries(data.result)) {
                // key = key.slice(key.charAt('_'));
                //    t = key.slice(key.indexOf('_')+1);
                map.set( key, value );
                // console.log(key + ':' + value);


                // context.font  = "20px sans-serif";
                // context.fillStyle = '#e22018';
                // context.fillText("添加文字", 125, 137);
                // context.strokeStyle = '#e22018';
                // context.strokeRect(125, 137, 115, 108);

                // context.fillStyle = "red";
                context.strokeStyle = "red";
                context.lineWidth = 5;
                // context.fillRect(value[0],value[1], value[2], value[3]);
                context.strokeRect(value[0],value[1], value[2], value[3]);


            }

            // console.log(map);
            // map.forEach(function(value,key){
            //     console.log(value,key);
            // });



        },
        error : function(json) {
            alert('2');
        }
    });
}

/**
 * 阅读模式
 * @returns {number}
 */
function readingMode (){
    minDistance = 100000;
    res = -1;
    console.log(click_x + ':' + click_y);


    map.forEach(function(value,key){

        // console.log((click_x - Number(value[0]))*(click_x - Number(value[0])) + (click_y - Number(value[1])) * (click_y - Number(value[1])));
        if( click_x<Number(value[0]) + Number(value[2]) && click_x > Number(value[0]) &&
            click_y<Number(value[1]) + Number(value[3]) && click_y > Number(value[1]) &&
            (click_x - (Number(value[0]) + Number(value[2])/2))*(click_x - (Number(value[0]) + Number(value[2])/2)) +
            (click_y - Number(value[1])+ Number(value[3])/2) * (click_y - Number(value[1])+ Number(value[3])/2) <= minDistance){
            minDistance = (click_x - (Number(value[0]) + Number(value[2])/2))*(click_x - (Number(value[0]) + Number(value[2])/2)) +
                (click_y - Number(value[1])+ Number(value[3])/2) * (click_y - Number(value[1])+ Number(value[3])/2);
            res = key.slice(key.indexOf('_')+1);
        }
    });
    if(res != -1){
        console.log(res);
        console.log(COCO_CLASSES[res]);
    }

}

/**
 * 预警模式
 */
function earlyWarningMode(){

    var len = 640;

    map.forEach(function(value,key){

        if( 0 <  click_x  && click_x < len/3 ){
            // return "1_" + key.slice(key.indexOf('_')+1);
        }else if(len/3 <  click_x  && click_x < (len/3)*2){
           //  return "2_" + key.slice(key.indexOf('_')+1);
        }
        else{
          //  return "3_" + key.slice(key.indexOf('_')+1);
        }
    });
}
