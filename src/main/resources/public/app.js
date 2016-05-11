$('#greeting_button').click(function() {
    $.get('/greeting', function(data) {
        $('#greeting_span').html(JSON.stringify(data));
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
            $('#login_span').html(xhr.getResponseHeader('Set-Cookie'));
        }
    });
});