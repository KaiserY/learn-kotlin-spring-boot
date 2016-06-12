var audioPlayer;
var videoPlayer;

var uploader = new qq.FineUploader({
    debug: true,
    element: document.getElementById("fine-uploader"),
    fileTemplate: "qq-template",
    request: {
        endpoint: "/api/upload",
        inputName: "file",
        filenameParam: "filename",
        totalFileSizeName: "totalfilesize",
        uuidName: "uuid"
    },
    chunking: {
        enabled: true,
        mandatory: true,
        partSize: 1024 * 1024,
        paramNames: {
            chunkSize: "chunksize",
            partByteOffset: "partbyteoffset",
            partIndex: "partindex",
            totalParts: "totalparts",
        }
    },
    resume: {
        enabled: true,
        resuming: "resume"
    },
    retry: {
       enableAuto: true
    }
});

$(document).ready(function() {
    videojs("video-js-audio").ready(function() {
        audioPlayer = this;
    });
    videojs("video-js-video").ready(function() {
        videoPlayer = this;
    });
});

$('#greeting_button').click(function() {
    $.get('/greeting', function(data) {
        $('#greeting_text').html(JSON.stringify(data, null, 4));
    });
});

$('#login_button').click(function() {
    $.ajax({
        url: '/login',
        type:"POST",
        data: JSON.stringify({
            name: $('#login_name').val(),
            password: $('#login_password').val()
        }),
        contentType:"application/json; charset=utf-8",
        dataType:"json",
        success: function(data, status, xhr) {
            $('#login_text').html(JSON.stringify(data, null, 4));
            $('#login_text').css('color', 'green');
        },
        error: function(xhr, status, error) {
            var response = JSON.parse(xhr.responseText);
            $('#login_text').html(response.message);
            $('#login_text').css('color', 'red');
        }
    });
});

$('#video-js-audio-play').click(function() {
    audioPlayer.src({"type":"audio/mp3", "src":"/api/audio"});
    audioPlayer.play();
});

$('#video-js-video-play').click(function() {
    videoPlayer.src({"type":"video/mp4", "src":"/api/video"});
    videoPlayer.play();
});
