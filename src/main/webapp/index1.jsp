<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <style>
        video{
            width: 100%;
            height: 80%;
            margin-left: 8px;
            margin-top: 8px;
            margin-right: 8px;
            background-color: aquamarine;
            display: block;
        }
    </style>
</head>
<body>

<video onclick="getMousePos()" id="video"></video>

<!--拍照按钮-->
<div>
    <button id="snap">SNAP</button>
    X:
    <input id="xxx" type=text>
    Y:
    <input id="yyy" type=text>
    video width :
    <input id="width" type=text>
    wideo height :
    <input id="height" type=text>

</div>
<!--描绘video截图-->
<canvas id="canvas" width="480" height="320"></canvas>

<script src="static/js/testjs.js"></script>
</body>
</html>
