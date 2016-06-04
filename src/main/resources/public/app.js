var player;

$(document).ready(function() {
    videojs("media-audio").ready(function() {
        player = this;
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

$('#media_play').click(function() {
    player.src({"type":"audio/mp3", "src":"/api/audio"});
    player.play();
});